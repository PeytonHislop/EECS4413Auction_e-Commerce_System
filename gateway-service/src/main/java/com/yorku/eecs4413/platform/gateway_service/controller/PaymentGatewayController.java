package com.yorku.eecs4413.platform.gateway_service.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yorku.eecs4413.platform.gateway_service.client.AuctionClient;
import com.yorku.eecs4413.platform.gateway_service.client.CatalogueClient;
import com.yorku.eecs4413.platform.gateway_service.client.IamClient;
import com.yorku.eecs4413.platform.gateway_service.client.PaymentClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentGatewayController {

    private final IamClient iamClient;
    private final AuctionClient auctionClient;
    private final CatalogueClient catalogueClient;
    private final PaymentClient paymentClient;
    private final ObjectMapper objectMapper;

    public PaymentGatewayController(
            IamClient iamClient,
            AuctionClient auctionClient,
            CatalogueClient catalogueClient,
            PaymentClient paymentClient,
            ObjectMapper objectMapper
    ) {
        this.iamClient = iamClient;
        this.auctionClient = auctionClient;
        this.catalogueClient = catalogueClient;
        this.paymentClient = paymentClient;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/process")
    public ResponseEntity<String> processPayment(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody String body
    ) {
        try {
            JsonNode requestJson = objectMapper.readTree(body);
            String auctionId = requestJson.path("auctionId").asText();

            if (auctionId == null || auctionId.isBlank()) {
                return error(HttpStatus.BAD_REQUEST, "Bad Request", "auctionId is required");
            }

            // validate token
            JsonNode validateJson = getSuccessfulJson(
                    iamClient.postWithAuth("/auth/validate", authHeader));

            if (!validateJson.path("valid").asBoolean(false)) {
                return error(HttpStatus.UNAUTHORIZED, "Unauthorized", "Invalid or expired token");
            }

            String tokenUserId = validateJson.path("userId").asText();
            String username = validateJson.path("username").asText();
            String role = validateJson.path("role").asText();

            // load auction
            JsonNode auctionJson = getSuccessfulJson(
                    auctionClient.get("/api/auctions/" + auctionId));

            String winnerId = auctionJson.path("winnerId").asText();
            String itemId = auctionJson.path("itemId").asText();
            double soldPrice = auctionJson.path("currentHighestBid").asDouble();

            if (winnerId == null || winnerId.isBlank()) {
                return error(HttpStatus.BAD_REQUEST, "Bad Request", "Auction has no winner");
            }

            if (!tokenUserId.equals(winnerId)) {
                return error(HttpStatus.FORBIDDEN, "Forbidden", "Only the auction winner can pay");
            }

            if (!"BUYER".equalsIgnoreCase(role)) {
                return error(HttpStatus.FORBIDDEN, "Forbidden", "User does not have BUYER role");
            }

            // user profile
            JsonNode userJson = getSuccessfulJson(
                    iamClient.getWithAuth("/users/" + winnerId, authHeader));

            String shippingAddress = formatShippingAddress(userJson.path("shippingAddress"));

            // catalogue item
            JsonNode itemJson = getSuccessfulJson(
            		catalogueClient.get("/api/catalogue/items/" + itemId));

            double shippingCost = itemJson.path("shippingPrice").asDouble();

            return paymentClient.postProcessPayment(
                    "/payments/process",
                    authHeader,
                    winnerId,
                    username,
                    role,
                    shippingAddress,
                    shippingCost,
                    soldPrice,
                    body
            );

        } catch (GatewayDownstreamException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getBody());
        } catch (NumberFormatException ex) {
            return error(HttpStatus.BAD_REQUEST, "Bad Request", "Auction itemId is not numeric");
        } catch (Exception ex) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Gateway Error", "Failed to process payment request");
        }
    }

    private JsonNode getSuccessfulJson(ResponseEntity<String> response) throws Exception {
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new GatewayDownstreamException(response.getStatusCode().value(), response.getBody());
        }
        return objectMapper.readTree(response.getBody());
    }

    private String formatShippingAddress(JsonNode address) {
        return address.path("streetNumber").asText() + " "
                + address.path("streetName").asText() + ", "
                + address.path("city").asText() + ", "
                + address.path("country").asText() + ", "
                + address.path("postalCode").asText();
    }

    private ResponseEntity<String> error(HttpStatus status, String error, String message) {
        String json = String.format(
                "{\"error\":\"%s\",\"message\":\"%s\"}",
                escapeJson(error),
                escapeJson(message)
        );
        return ResponseEntity.status(status).body(json);
    }

    private String escapeJson(String value) {
        return value.replace("\"", "\\\"");
    }

    private static class GatewayDownstreamException extends RuntimeException {
        private final int statusCode;
        private final String body;

        public GatewayDownstreamException(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getBody() {
            return body;
        }
    }
}
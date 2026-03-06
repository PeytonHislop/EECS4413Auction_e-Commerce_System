package com.yorku.eecs4413.platform.gateway_service.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final PaymentClient paymentClient;
    private final ObjectMapper objectMapper;

    public PaymentGatewayController(
            IamClient iamClient,
            PaymentClient paymentClient,
            ObjectMapper objectMapper) {
        this.iamClient = iamClient;
        this.paymentClient = paymentClient;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/process")
    public ResponseEntity<String> processPayment(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody String body) {

        try {
            JsonNode validateJson = getSuccessfulJson(
                    iamClient.postWithAuth("/auth/validate", authHeader));

            if (!validateJson.path("valid").asBoolean(false)) {
                return error(HttpStatus.UNAUTHORIZED,
                        "Unauthorized",
                        "Invalid or expired token");
            }

            String userId = validateJson.path("userId").asText();
            String username = validateJson.path("username").asText();
            String role = validateJson.path("role").asText();

            JsonNode authorizeJson = getSuccessfulJson(
                    iamClient.getWithAuth("/auth/authorize?requiredRole=BUYER", authHeader));

            if (!authorizeJson.path("authorized").asBoolean(false)) {
                return error(HttpStatus.FORBIDDEN,
                        "Forbidden",
                        "User does not have BUYER role");
            }

            JsonNode userJson = getSuccessfulJson(
                    iamClient.getWithAuth("/users/" + userId, authHeader));

            String shippingAddress = formatShippingAddress(userJson.path("shippingAddress"));

            return paymentClient.postProcessPayment(
                    "/payments/process",
                    authHeader,
                    userId,
                    username,
                    role,
                    shippingAddress,
                    body
            );

        } catch (GatewayDownstreamException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getBody());
        } catch (Exception ex) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Gateway Error",
                    "Failed to process payment request");
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
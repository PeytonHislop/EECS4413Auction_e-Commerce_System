package com.yorku.eecs4413.platform.gateway_service.client;

import com.yorku.eecs4413.platform.gateway_service.config.DownstreamProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class PaymentClient {

    private final RestClient restClient;
    private final String baseUrl;

    public PaymentClient(RestClient restClient, DownstreamProperties props) {
        this.restClient = restClient;
        this.baseUrl = props.payment().baseUrl();
    }

    public ResponseEntity<String> postProcessPayment(
            String path,
            String authHeader,
            String userId,
            String username,
            String role,
            String shippingAddress,
            double shippingCost,
            double soldPrice,
            String jsonBody
    ) {
        try {
            String responseBody = restClient.post()
                    .uri(baseUrl + path)
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .header("X-User-Id", userId)
                    .header("X-Username", username)
                    .header("X-Role", role)
                    .header("X-Shipping-Address", shippingAddress)
                    .header("X-Shipping-Cost", String.valueOf(shippingCost))
                    .header("X-Sold-Price", String.valueOf(soldPrice))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonBody)
                    .retrieve()
                    .body(String.class);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(responseBody);

        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ex.getResponseBodyAsString());
        }
    }
}

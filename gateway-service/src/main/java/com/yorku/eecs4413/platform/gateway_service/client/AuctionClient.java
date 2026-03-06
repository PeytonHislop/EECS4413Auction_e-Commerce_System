package com.yorku.eecs4413.platform.gateway_service.client;

import com.yorku.eecs4413.platform.gateway_service.config.DownstreamProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Client for Auction Service communication
 * Base URL: http://localhost:8082
 * @author Syed Mustafa Jamal - Auction Service
 */
@Component
public class AuctionClient {

    private final RestClient restClient;
    private final String baseUrl;

    public AuctionClient(RestClient restClient, DownstreamProperties props) {
        this.restClient = restClient;
        this.baseUrl = props.auction().baseUrl();
    }

    //POST request with JSON body
    public ResponseEntity<String> postJson(String path, String jsonBody) {
        try {
            String body = restClient.post()
                    .uri(baseUrl + path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonBody)
                    .retrieve()
                    .body(String.class);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body);

        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ex.getResponseBodyAsString());
        }
    }

    //POST request with Authorization header
    public ResponseEntity<String> postWithAuth(String path, String authHeader, String jsonBody) {
        try {
            String body = restClient.post()
                    .uri(baseUrl + path)
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonBody)
                    .retrieve()
                    .body(String.class);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body);

        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ex.getResponseBodyAsString());
        }
    }

    // PUT request with Authorization header
    public ResponseEntity<String> putWithAuth(String path, String authHeader) {
        try {
            String body = restClient.put()
                    .uri(baseUrl + path)
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .retrieve()
                    .body(String.class);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body);

        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ex.getResponseBodyAsString());
        }
    }

    // GET request with Authorization header
    public ResponseEntity<String> getWithAuth(String path, String authHeader) {
        try {
            String body = restClient.get()
                    .uri(baseUrl + path)
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .retrieve()
                    .body(String.class);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body);

        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ex.getResponseBodyAsString());
        }
    }

    // GET request without authorization (public endpoints)
    public ResponseEntity<String> get(String path) {
        try {
            String body = restClient.get()
                    .uri(baseUrl + path)
                    .retrieve()
                    .body(String.class);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body);

        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ex.getResponseBodyAsString());
        }
    }
}

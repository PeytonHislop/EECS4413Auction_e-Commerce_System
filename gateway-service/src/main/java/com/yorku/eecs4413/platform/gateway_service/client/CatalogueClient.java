package com.yorku.eecs4413.platform.gateway_service.client;

import com.yorku.eecs4413.platform.gateway_service.config.DownstreamProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class CatalogueClient {

    private final RestClient restClient;
    private final String baseUrl;

    public CatalogueClient(RestClient restClient, DownstreamProperties props) {
        this.restClient = restClient;
        this.baseUrl = props.catalogue().baseUrl();
    }

    public ResponseEntity<String> postItemWithAuth(String path, String authHeader, String jsonBody) {
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

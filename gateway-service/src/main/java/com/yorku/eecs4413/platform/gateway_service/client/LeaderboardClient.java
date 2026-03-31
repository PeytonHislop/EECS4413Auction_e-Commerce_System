package com.yorku.eecs4413.platform.gateway_service.client;

import com.yorku.eecs4413.platform.gateway_service.config.DownstreamProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class LeaderboardClient {

    private final RestClient restClient;
    private final String baseUrl;

    public LeaderboardClient(RestClient restClient, DownstreamProperties props) {
        this.restClient = restClient;
        this.baseUrl = props.leaderboard().baseUrl();
    }

    public ResponseEntity<String> get(String path) {
        try {
            String body = restClient.get()
                    .uri(baseUrl + path)
                    .retrieve()
                    .body(String.class);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ex.getResponseBodyAsString());
        }
    }
}

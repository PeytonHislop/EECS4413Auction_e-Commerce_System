package com.auction.payment.client;

import com.auction.payment.dto.ValidateTokenResponse;
import com.auction.payment.dto.UserProfileResponse;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class IamServiceClient {

    private final RestTemplate restTemplate;

    private static final String IAM_BASE_URL = "http://localhost:8081";

    public IamServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Validate JWT token with IAM service
     */
    public ValidateTokenResponse validateToken(String authHeader) {

        System.out.println("DEBUG ---- CALLING IAM /auth/validate ----");
        System.out.println("Header sent: " + authHeader);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<ValidateTokenResponse> response =
                restTemplate.exchange(
                        IAM_BASE_URL + "/auth/validate",
                        HttpMethod.POST,
                        entity,
                        ValidateTokenResponse.class
                );

        System.out.println("DEBUG ---- IAM /auth/validate RESPONSE ----");
        System.out.println(response.getBody());

        return response.getBody();
    }

    /**
     * Check if user has required role
     */
    public boolean authorizeRole(String authHeader, String role) {

        System.out.println("DEBUG ---- CALLING IAM /auth/authorize ----");
        System.out.println("Role requested: " + role);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response =
                restTemplate.exchange(
                        IAM_BASE_URL + "/auth/authorize?requiredRole=" + role,
                        HttpMethod.GET,
                        entity,
                        Map.class
                );

        System.out.println("DEBUG ---- IAM /auth/authorize RESPONSE ----");
        System.out.println(response.getBody());

        Boolean authorized = (Boolean) response.getBody().get("authorized");

        return Boolean.TRUE.equals(authorized);
    }

    /**
     * Retrieve user profile
     */
    public UserProfileResponse getUserProfile(String authHeader, String userId) {

        System.out.println("DEBUG ---- CALLING IAM /users/{userId} ----");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<UserProfileResponse> response =
                restTemplate.exchange(
                        IAM_BASE_URL + "/users/" + userId,
                        HttpMethod.GET,
                        entity,
                        UserProfileResponse.class
                );

        System.out.println("DEBUG ---- IAM USER PROFILE RESPONSE ----");
        System.out.println(response.getBody());

        return response.getBody();
    }
}

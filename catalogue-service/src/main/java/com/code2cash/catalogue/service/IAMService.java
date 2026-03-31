package com.code2cash.catalogue.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.code2cash.catalogue.config.IAMServiceConfig;
import com.code2cash.catalogue.dto.AuthorizeResponse;
import com.code2cash.catalogue.dto.ValidateTokenResponse;

@Service
public class IAMService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private IAMServiceConfig iamServiceConfig;

    /**
     * Validates JWT token and returns user information
     * Calls: POST /auth/validate
     */
    public ValidateTokenResponse validateToken(String token) {
        try {
            String url = iamServiceConfig.getIamServiceUrl() + "/auth/validate";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<ValidateTokenResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                ValidateTokenResponse.class
            );
            
            return response.getBody();
        } catch (Exception e) {
            // Token validation failed
            return new ValidateTokenResponse(false, null, null, null);
        }
    }

    /**
     * Checks if user has specific role authorization
     * Calls: GET /auth/authorize?requiredRole={role}
     */
    public boolean authorizeRole(String token, String requiredRole) {
        try {
            String url = iamServiceConfig.getIamServiceUrl() + "/auth/authorize?requiredRole=" + requiredRole;
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<AuthorizeResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                AuthorizeResponse.class
            );
            
            AuthorizeResponse body = response.getBody();
            return body != null && body.isAuthorized();
        } catch (Exception e) {
            // Authorization failed
            return false;
        }
    }

    /**
     * Gets user information by userId
     * Calls: /users/{userId}
     */
    public Object getUserInfo(String userId) {
        try {
            String url = iamServiceConfig.getIamServiceUrl() + "/users/" + userId;
            
            ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
            return response.getBody();
        } catch (Exception e) {
            return null;
        }
    }
}

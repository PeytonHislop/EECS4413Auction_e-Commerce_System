package com.code2cash.catalogue.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.code2cash.catalogue.config.IAMServiceConfig;
import com.code2cash.catalogue.dto.AuthorizeRequest;
import com.code2cash.catalogue.dto.ValidateTokenResponse;

@Service
public class IAMService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private IAMServiceConfig iamServiceConfig;

    /**
     * Validates JWT token and returns user information
     * Calls: /auth/validate
     */
    public ValidateTokenResponse validateToken(String token) {
        try {
            String url = iamServiceConfig.getIamServiceUrl() + "/auth/validate";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<ValidateTokenResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
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
     * Calls: /auth/authorize
     */
    public boolean authorizeRole(String token, String requiredRole) {
        try {
            String url = iamServiceConfig.getIamServiceUrl() + "/auth/authorize";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            AuthorizeRequest request = new AuthorizeRequest(requiredRole);
            HttpEntity<AuthorizeRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Boolean> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Boolean.class
            );
            
            return Boolean.TRUE.equals(response.getBody());
        } catch (Exception e) {
            // Authorization failed
            return false;
        }
    }

    /**
     * Gets user information by userId
     * Calls: /users/{userId}
     */
    public Object getUserInfo(Long userId) {
        try {
            String url = iamServiceConfig.getIamServiceUrl() + "/users/" + userId;
            
            ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
            return response.getBody();
        } catch (Exception e) {
            return null;
        }
    }
}

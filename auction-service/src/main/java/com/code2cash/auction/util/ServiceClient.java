package com.code2cash.auction.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Map;

/**
 * Client for communicating with other microservices
 * Uses Spring WebClient for non-blocking HTTP calls
 * 
 * Integration with Ravneet's IAM Service:
 * - /auth/validate - Validates JWT token
 * - /auth/authorize - Checks user role permissions
 * - /users/{userId} - Gets user profile info
 */
@Component
public class ServiceClient {
    
    private final WebClient webClient;
    
    @Value("${service.iam.url}")
    private String iamServiceUrl;
    
    @Value("${service.catalogue.url}")
    private String catalogueServiceUrl;
    
    @Value("${service.payment.url}")
    private String paymentServiceUrl;
    
    @Value("${service.leaderboard.url}")
    private String leaderboardServiceUrl;
    
    // Toggle between mock and real service calls
    @Value("${service.iam.mock:true}")
    private boolean mockIAM;
    
    public ServiceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }
    
    /**
     * Validate JWT token with IAM service
     * Calls: POST /auth/validate
     * 
     * @param token The JWT authentication token
     * @return ValidationResponse containing userId, username, role, and validity
     */
    public ValidationResponse validateToken(String token) {
        if (mockIAM) {
            // MOCK MODE: Return dummy data for testing
            System.out.println("MOCK: Validating token with IAM service");
            ValidationResponse mock = new ValidationResponse();
            mock.setValid(token != null && !token.isEmpty());
            mock.setUserId("USER" + Math.abs(token.hashCode() % 1000));
            mock.setUsername("testuser");
            mock.setRole("BUYER");
            return mock;
        }
        
        try {
            // REAL MODE: Call Ravneet's IAM service
            Map<String, Object> response = webClient.post()
                .uri(iamServiceUrl + "/auth/validate")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
            
            ValidationResponse validationResponse = new ValidationResponse();
            if (response != null) {
                validationResponse.setValid((Boolean) response.get("valid"));
                validationResponse.setUserId((String) response.get("userId"));
                validationResponse.setUsername((String) response.get("username"));
                validationResponse.setRole((String) response.get("role"));
            } else {
                validationResponse.setValid(false);
            }
            
            return validationResponse;
            
        } catch (WebClientResponseException e) {
            System.err.println("Error validating token: " + e.getStatusCode() + " - " + e.getMessage());
            ValidationResponse error = new ValidationResponse();
            error.setValid(false);
            return error;
        } catch (Exception e) {
            System.err.println("Unexpected error validating token: " + e.getMessage());
            ValidationResponse error = new ValidationResponse();
            error.setValid(false);
            return error;
        }
    }
    
    /**
     * Check if user has specific role permission
     * Calls: POST /auth/authorize
     * 
     * @param token The JWT token
     * @param role The required role (BUYER, SELLER, ADMIN)
     * @return true if user has the required role
     */
    public boolean authorizeRole(String token, String role) {
        if (mockIAM) {
            // MOCK MODE: Allow all for testing
            System.out.println("MOCK: Authorizing role " + role);
            return true;
        }
        
        try {
            // REAL MODE: Call Ravneet's IAM service
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("role", role);
            
            Map<String, Object> response = webClient.post()
                .uri(iamServiceUrl + "/auth/authorize")
                .header("Authorization", "Bearer " + token)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
            
            return response != null && (Boolean) response.getOrDefault("authorized", false);
            
        } catch (WebClientResponseException e) {
            System.err.println("Error authorizing role: " + e.getStatusCode() + " - " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Unexpected error authorizing: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get user profile information
     * Calls: GET /users/{userId}
     * 
     * @param userId The user ID
     * @return User profile with shipping address
     */
    public Map<String, Object> getUserProfile(String userId) {
        if (mockIAM) {
            // MOCK MODE: Return dummy profile
            System.out.println("MOCK: Getting user profile for " + userId);
            Map<String, Object> mockProfile = new HashMap<>();
            mockProfile.put("userId", userId);
            mockProfile.put("username", "testuser");
            mockProfile.put("firstName", "Test");
            mockProfile.put("lastName", "User");
            mockProfile.put("address", Map.of(
                "street", "123 Test St",
                "city", "Toronto",
                "province", "ON",
                "postalCode", "M1M 1M1"
            ));
            return mockProfile;
        }
        
        try {
            // REAL MODE: Call Ravneet's IAM service
            Map<String, Object> response = webClient.get()
                .uri(iamServiceUrl + "/users/" + userId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
            
            return response;
            
        } catch (WebClientResponseException e) {
            System.err.println("Error getting user profile: " + e.getStatusCode() + " - " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("Unexpected error getting profile: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Verify that an item exists in the catalogue
     * 
     * @param itemId The item ID
     * @return true if item exists, false otherwise
     */
    public boolean verifyItemExists(String itemId) {
        try {
            // Call Catalogue service to verify item exists
            // GET /api/catalogue/items/{id} - returns 404 if not found
            webClient.get()
                .uri(catalogueServiceUrl + "/api/catalogue/items/" + itemId)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
            
            System.out.println("Item verified: " + itemId);
            return true;
            
        } catch (WebClientResponseException.NotFound e) {
            System.out.println("Item not found: " + itemId);
            return false;
        } catch (Exception e) {
            System.err.println("Error verifying item: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Initiate payment for auction winner
     * Note: Payment is actually processed through the gateway when the winner
     * explicitly calls POST /api/payments/process. This method is kept for
     * potential future notification or logging purposes.
     * 
     * @param auctionId The auction ID
     * @param winnerId The winner's user ID
     * @param amount The payment amount
     * @return true if payment notification sent successfully
     */
    public boolean initiatePayment(String auctionId, String winnerId, double amount) {
        try {
            // Payment is processed through gateway by the winner
            // This is just a placeholder for potential future notification system
            System.out.println("Auction closed - Payment pending for auction " + auctionId + 
                             " (Winner: " + winnerId + ", Amount: $" + amount + ")");
            return true;
            
        } catch (Exception e) {
            System.err.println("Error logging payment notification: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Add bid entry to leaderboard service
     * Calls: POST /api/leaderboard/bids
     * 
     * @param auctionId The auction ID
     * @param itemId The item ID
     * @param bidderId The bidder's user ID
     * @param bidderName The bidder's name
     * @param bidAmount The bid amount
     * @param sellerId The seller's user ID
     * @param sellerName The seller's name
     * @return true if entry added successfully
     */
    public boolean addLeaderboardEntry(String auctionId, String itemId, String bidderId, 
                                      String bidderName, java.math.BigDecimal bidAmount, 
                                      String sellerId, String sellerName) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("auctionId", auctionId);
            requestBody.put("itemId", itemId);
            requestBody.put("bidderId", bidderId);
            requestBody.put("bidderName", bidderName);
            requestBody.put("bidAmount", bidAmount);
            requestBody.put("sellerId", sellerId);
            requestBody.put("sellerName", sellerName);
            
            webClient.post()
                .uri(leaderboardServiceUrl + "/api/leaderboard/bids")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
            
            System.out.println("Added bid to leaderboard: " + auctionId + " - $" + bidAmount);
            return true;
            
        } catch (WebClientResponseException e) {
            System.err.println("Error adding leaderboard entry: " + e.getStatusCode() + " - " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Unexpected error adding leaderboard entry: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Inner class for validation response from IAM service
     */
    public static class ValidationResponse {
        private boolean valid;
        private String userId;
        private String username;
        private String role;
        
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}

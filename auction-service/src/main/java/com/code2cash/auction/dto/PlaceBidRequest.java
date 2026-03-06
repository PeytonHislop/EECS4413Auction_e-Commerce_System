package com.code2cash.auction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;

/**
 * DTO for placing a bid on an auction
 */
public class PlaceBidRequest {
    
    @NotBlank(message = "Bidder ID is required")
    private String bidderId;
    
    @NotNull(message = "Bid amount is required")
    @Positive(message = "Bid amount must be positive")
    private BigDecimal bidAmount;
    
    // Token for authentication (passed from Gateway)
    private String token;
    
    // Constructors
    public PlaceBidRequest() {}
    
    public PlaceBidRequest(String bidderId, BigDecimal bidAmount) {
        this.bidderId = bidderId;
        this.bidAmount = bidAmount;
    }
    
    // Getters and Setters
    public String getBidderId() {
        return bidderId;
    }
    
    public void setBidderId(String bidderId) {
        this.bidderId = bidderId;
    }
    
    public BigDecimal getBidAmount() {
        return bidAmount;
    }
    
    public void setBidAmount(BigDecimal bidAmount) {
        this.bidAmount = bidAmount;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
}

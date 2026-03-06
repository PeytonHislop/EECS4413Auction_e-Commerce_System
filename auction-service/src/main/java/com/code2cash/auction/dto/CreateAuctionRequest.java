package com.code2cash.auction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.DecimalMin;

/**
 * DTO for creating a new auction
 */
public class CreateAuctionRequest {
    
    @NotBlank(message = "Item ID is required")
    private String itemId;
    
    @NotBlank(message = "Seller ID is required")
    private String sellerId;
    
    @NotNull(message = "Duration in hours is required")
    @Positive(message = "Duration must be positive")
    private Integer durationHours;
    
    @NotNull(message = "Reserve price is required")
    @Positive(message = "Reserve price must be positive")
    private BigDecimal reservePrice;
    
    // Constructors
    public CreateAuctionRequest() {}
    
    public CreateAuctionRequest(String itemId, String sellerId, Integer durationHours, BigDecimal reservePrice) {
        this.itemId = itemId;
        this.sellerId = sellerId;
        this.durationHours = durationHours;
        this.reservePrice = reservePrice;
    }
    
    // Getters and Setters
    public String getItemId() {
        return itemId;
    }
    
    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
    
    public String getSellerId() {
        return sellerId;
    }
    
    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }
    
    public Integer getDurationHours() {
        return durationHours;
    }
    
    public void setDurationHours(Integer durationHours) {
        this.durationHours = durationHours;
    }
    
    public BigDecimal getReservePrice() {
        return reservePrice;
    }
    
    public void setReservePrice(BigDecimal reservePrice) {
        this.reservePrice = reservePrice;
    }
}

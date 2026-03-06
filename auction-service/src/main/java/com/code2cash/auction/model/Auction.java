package com.code2cash.auction.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Auction Model
 * Represents an auction in the system
 */
public class Auction {
    
    private String auctionId;
    private String itemId;
    private String sellerId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private AuctionStatus status;
    private BigDecimal reservePrice;
    private BigDecimal currentHighestBid;
    private String currentHighestBidderId;
    private String winnerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public Auction() {}
    
    public Auction(String auctionId, String itemId, String sellerId, 
                   LocalDateTime startTime, LocalDateTime endTime, 
                   AuctionStatus status, BigDecimal reservePrice) {
        this.auctionId = auctionId;
        this.itemId = itemId;
        this.sellerId = sellerId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.reservePrice = reservePrice;
        this.currentHighestBid = BigDecimal.ZERO;
    }
    
    // Getters and Setters
    public String getAuctionId() {
        return auctionId;
    }
    
    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }
    
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
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public AuctionStatus getStatus() {
        return status;
    }
    
    public void setStatus(AuctionStatus status) {
        this.status = status;
    }
    
    public BigDecimal getReservePrice() {
        return reservePrice;
    }
    
    public void setReservePrice(BigDecimal reservePrice) {
        this.reservePrice = reservePrice;
    }
    
    public BigDecimal getCurrentHighestBid() {
        return currentHighestBid;
    }
    
    public void setCurrentHighestBid(BigDecimal currentHighestBid) {
        this.currentHighestBid = currentHighestBid;
    }
    
    public String getCurrentHighestBidderId() {
        return currentHighestBidderId;
    }
    
    public void setCurrentHighestBidderId(String currentHighestBidderId) {
        this.currentHighestBidderId = currentHighestBidderId;
    }
    
    public String getWinnerId() {
        return winnerId;
    }
    
    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * Check if auction is currently active
     */
    public boolean isActive() {
        return this.status == AuctionStatus.ACTIVE && 
               LocalDateTime.now().isBefore(this.endTime);
    }
    
    /**
     * Check if auction has ended
     */
    public boolean hasEnded() {
        return LocalDateTime.now().isAfter(this.endTime);
    }
    
    /**
     * Calculate time remaining in seconds
     */
    public long getTimeRemainingSeconds() {
        if (hasEnded()) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), this.endTime).getSeconds();
    }
    
    @Override
    public String toString() {
        return "Auction{" +
                "auctionId='" + auctionId + '\'' +
                ", itemId='" + itemId + '\'' +
                ", status=" + status +
                ", currentHighestBid=" + currentHighestBid +
                ", endTime=" + endTime +
                '}';
    }
}

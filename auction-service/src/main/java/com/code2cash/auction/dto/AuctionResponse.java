package com.code2cash.auction.dto;

import com.code2cash.auction.model.AuctionStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for auction response
 */
public class AuctionResponse {
    
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
    private long timeRemainingSeconds;
    
    // Constructors
    public AuctionResponse() {}
    
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
    
    public long getTimeRemainingSeconds() {
        return timeRemainingSeconds;
    }
    
    public void setTimeRemainingSeconds(long timeRemainingSeconds) {
        this.timeRemainingSeconds = timeRemainingSeconds;
    }
}

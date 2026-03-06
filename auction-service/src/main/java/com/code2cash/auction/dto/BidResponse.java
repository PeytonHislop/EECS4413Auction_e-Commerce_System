package com.code2cash.auction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for bid response
 */
public class BidResponse {
    
    private String bidId;
    private String auctionId;
    private String bidderId;
    private BigDecimal bidAmount;
    private LocalDateTime bidTimestamp;
    private String message;
    private boolean success;
    
    // Constructors
    public BidResponse() {}
    
    public BidResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    // Getters and Setters
    public String getBidId() {
        return bidId;
    }
    
    public void setBidId(String bidId) {
        this.bidId = bidId;
    }
    
    public String getAuctionId() {
        return auctionId;
    }
    
    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }
    
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
    
    public LocalDateTime getBidTimestamp() {
        return bidTimestamp;
    }
    
    public void setBidTimestamp(LocalDateTime bidTimestamp) {
        this.bidTimestamp = bidTimestamp;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
}

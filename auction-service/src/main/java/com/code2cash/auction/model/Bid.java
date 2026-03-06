package com.code2cash.auction.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Bid Model
 * Represents a bid placed on an auction
 */
public class Bid {
    
    private String bidId;
    private String auctionId;
    private String bidderId;
    private BigDecimal bidAmount;
    private LocalDateTime bidTimestamp;
    
    // Constructors
    public Bid() {}
    
    public Bid(String bidId, String auctionId, String bidderId, BigDecimal bidAmount) {
        this.bidId = bidId;
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.bidAmount = bidAmount;
        this.bidTimestamp = LocalDateTime.now();
    }
    
    public Bid(String auctionId, String bidderId, BigDecimal bidAmount) {
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.bidAmount = bidAmount;
        this.bidTimestamp = LocalDateTime.now();
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
    
    @Override
    public String toString() {
        return "Bid{" +
                "bidId='" + bidId + '\'' +
                ", auctionId='" + auctionId + '\'' +
                ", bidderId='" + bidderId + '\'' +
                ", bidAmount=" + bidAmount +
                ", bidTimestamp=" + bidTimestamp +
                '}';
    }
}

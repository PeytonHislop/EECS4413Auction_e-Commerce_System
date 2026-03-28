package com.code2cash.leaderboard.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Leaderboard Entry Entity
 * Tracks bid history for weekly leaderboard
 */
@Entity
@Table(name = "leaderboard_entries")
public class LeaderboardEntry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String auctionId;
    
    @Column(nullable = false)
    private String itemId;
    
    @Column(nullable = false)
    private String bidderId;
    
    @Column(nullable = false)
    private String bidderName;
    
    @Column(nullable = false)
    private BigDecimal bidAmount;
    
    @Column(nullable = false)
    private String sellerId;
    
    @Column(nullable = false)
    private String sellerName;
    
    @Column(nullable = false)
    private LocalDateTime bidTime;
    
    @Column(nullable = false)
    private LocalDateTime weekStart;
    
    @Column(nullable = false)
    private LocalDateTime weekEnd;

    // Constructors
    public LeaderboardEntry() {}

    public LeaderboardEntry(String auctionId, String itemId, String bidderId, String bidderName,
                           BigDecimal bidAmount, String sellerId, String sellerName,
                           LocalDateTime bidTime, LocalDateTime weekStart, LocalDateTime weekEnd) {
        this.auctionId = auctionId;
        this.itemId = itemId;
        this.bidderId = bidderId;
        this.bidderName = bidderName;
        this.bidAmount = bidAmount;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.bidTime = bidTime;
        this.weekStart = weekStart;
        this.weekEnd = weekEnd;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getBidderId() {
        return bidderId;
    }

    public void setBidderId(String bidderId) {
        this.bidderId = bidderId;
    }

    public String getBidderName() {
        return bidderName;
    }

    public void setBidderName(String bidderName) {
        this.bidderName = bidderName;
    }

    public BigDecimal getBidAmount() {
        return bidAmount;
    }

    public void setBidAmount(BigDecimal bidAmount) {
        this.bidAmount = bidAmount;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public LocalDateTime getBidTime() {
        return bidTime;
    }

    public void setBidTime(LocalDateTime bidTime) {
        this.bidTime = bidTime;
    }

    public LocalDateTime getWeekStart() {
        return weekStart;
    }

    public void setWeekStart(LocalDateTime weekStart) {
        this.weekStart = weekStart;
    }

    public LocalDateTime getWeekEnd() {
        return weekEnd;
    }

    public void setWeekEnd(LocalDateTime weekEnd) {
        this.weekEnd = weekEnd;
    }
}

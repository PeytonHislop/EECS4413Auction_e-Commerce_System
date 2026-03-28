package com.code2cash.leaderboard.dto;

import org.springframework.hateoas.RepresentationModel;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Leaderboard Entry Response with HATEOAS links
 */
public class LeaderboardEntryResponse extends RepresentationModel<LeaderboardEntryResponse> {
    private Long rank;
    private String auctionId;
    private String itemId;
    private String bidderId;
    private String bidderName;
    private BigDecimal bidAmount;
    private String sellerId;
    private String sellerName;
    private LocalDateTime bidTime;

    // Constructors
    public LeaderboardEntryResponse() {}

    public LeaderboardEntryResponse(Long rank, String auctionId, String itemId, String bidderId,
                                   String bidderName, BigDecimal bidAmount, String sellerId,
                                   String sellerName, LocalDateTime bidTime) {
        this.rank = rank;
        this.auctionId = auctionId;
        this.itemId = itemId;
        this.bidderId = bidderId;
        this.bidderName = bidderName;
        this.bidAmount = bidAmount;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.bidTime = bidTime;
    }

    // Getters and Setters
    public Long getRank() {
        return rank;
    }

    public void setRank(Long rank) {
        this.rank = rank;
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
}

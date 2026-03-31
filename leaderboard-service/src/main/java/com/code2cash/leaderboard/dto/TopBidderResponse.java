package com.code2cash.leaderboard.dto;

import org.springframework.hateoas.RepresentationModel;

import java.math.BigDecimal;

/**
 * DTO for aggregated top bidder leaderboard rows.
 */
public class TopBidderResponse extends RepresentationModel<TopBidderResponse> {
    private Long rank;
    private String bidderId;
    private String bidderName;
    private Long bidCount;
    private BigDecimal highestBid;
    private BigDecimal totalBidValue;

    public TopBidderResponse() {}

    public TopBidderResponse(Long rank, String bidderId, String bidderName, Long bidCount,
                             BigDecimal highestBid, BigDecimal totalBidValue) {
        this.rank = rank;
        this.bidderId = bidderId;
        this.bidderName = bidderName;
        this.bidCount = bidCount;
        this.highestBid = highestBid;
        this.totalBidValue = totalBidValue;
    }

    public Long getRank() { return rank; }
    public void setRank(Long rank) { this.rank = rank; }

    public String getBidderId() { return bidderId; }
    public void setBidderId(String bidderId) { this.bidderId = bidderId; }

    public String getBidderName() { return bidderName; }
    public void setBidderName(String bidderName) { this.bidderName = bidderName; }

    public Long getBidCount() { return bidCount; }
    public void setBidCount(Long bidCount) { this.bidCount = bidCount; }

    public BigDecimal getHighestBid() { return highestBid; }
    public void setHighestBid(BigDecimal highestBid) { this.highestBid = highestBid; }

    public BigDecimal getTotalBidValue() { return totalBidValue; }
    public void setTotalBidValue(BigDecimal totalBidValue) { this.totalBidValue = totalBidValue; }
}

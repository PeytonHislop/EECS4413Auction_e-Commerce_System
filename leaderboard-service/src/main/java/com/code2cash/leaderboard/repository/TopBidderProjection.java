package com.code2cash.leaderboard.repository;

import java.math.BigDecimal;

public interface TopBidderProjection {
    String getBidderId();
    String getBidderName();
    Long getBidCount();
    BigDecimal getHighestBid();
    BigDecimal getTotalBidValue();
}

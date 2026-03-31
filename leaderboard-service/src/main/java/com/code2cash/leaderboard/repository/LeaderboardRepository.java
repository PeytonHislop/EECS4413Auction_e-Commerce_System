package com.code2cash.leaderboard.repository;

import com.code2cash.leaderboard.model.LeaderboardEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Leaderboard Entries
 */
@Repository
public interface LeaderboardRepository extends JpaRepository<LeaderboardEntry, Long> {
    
    /**
     * Find top entries for a date range based on bid timestamp.
     * Using a range avoids precision mismatch on persisted timestamp fields.
     */
    @Query(value = "SELECT * FROM leaderboard_entries " +
                   "WHERE bid_time BETWEEN ?1 AND ?2 " +
                   "ORDER BY bid_amount DESC LIMIT ?3", nativeQuery = true)
    List<LeaderboardEntry> findTopEntriesByBidTimeRange(LocalDateTime weekStart, LocalDateTime weekEnd, int limit);

    /**
     * Find the single highest bid within a date range.
     */
    @Query(value = "SELECT * FROM leaderboard_entries " +
                   "WHERE bid_time BETWEEN ?1 AND ?2 " +
                   "ORDER BY bid_amount DESC LIMIT 1", nativeQuery = true)
    List<LeaderboardEntry> findHighestEntryByBidTimeRange(LocalDateTime start, LocalDateTime end);

    /**
     * Aggregate top bidders by period.
     */
    @Query(value = "SELECT bidder_id AS bidderId, MAX(bidder_name) AS bidderName, COUNT(*) AS bidCount, " +
            "MAX(bid_amount) AS highestBid, SUM(bid_amount) AS totalBidValue " +
            "FROM leaderboard_entries " +
            "WHERE bid_time BETWEEN ?1 AND ?2 " +
            "GROUP BY bidder_id " +
            "ORDER BY highestBid DESC, totalBidValue DESC " +
            "LIMIT ?3", nativeQuery = true)
    List<TopBidderProjection> findTopBiddersByBidTimeRange(LocalDateTime start, LocalDateTime end, int limit);

    /**
     * Find bidder bids for a date range based on bid timestamp.
     */
    List<LeaderboardEntry> findByBidderIdAndBidTimeBetweenOrderByBidAmountDesc(
            String bidderId, LocalDateTime weekStart, LocalDateTime weekEnd);

    /**
     * Count entries in a date range based on bid timestamp.
     */
    long countByBidTimeBetween(LocalDateTime weekStart, LocalDateTime weekEnd);
    
    /**
     * Clear old entries (older than specified date)
     */
    long deleteByWeekEndBefore(LocalDateTime date);
}

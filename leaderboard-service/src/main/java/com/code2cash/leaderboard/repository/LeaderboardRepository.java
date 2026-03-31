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
     * Find top N entries for current week ordered by bid amount (descending)
     */
    List<LeaderboardEntry> findByWeekStartAndWeekEndOrderByBidAmountDesc(
            LocalDateTime weekStart, LocalDateTime weekEnd);
    
    /**
     * Find entries for a specific week
     */
    List<LeaderboardEntry> findByWeekStartAndWeekEnd(
            LocalDateTime weekStart, LocalDateTime weekEnd);
    
    /**
     * Find top entries based on limit
     */
    @Query(value = "SELECT * FROM leaderboard_entries " +
                   "WHERE week_start = ?1 AND week_end = ?2 " +
                   "ORDER BY bid_amount DESC LIMIT ?3", nativeQuery = true)
    List<LeaderboardEntry> findTopEntriesForWeek(LocalDateTime weekStart, LocalDateTime weekEnd, int limit);
    
    /**
     * Find bids by bidder for current week
     */
    List<LeaderboardEntry> findByBidderIdAndWeekStartAndWeekEndOrderByBidAmountDesc(
            String bidderId, LocalDateTime weekStart, LocalDateTime weekEnd);
    
    /**
     * Count entries for current week
     */
    long countByWeekStartAndWeekEnd(LocalDateTime weekStart, LocalDateTime weekEnd);
    
    /**
     * Clear old entries (older than specified date)
     */
    long deleteByWeekEndBefore(LocalDateTime date);
}

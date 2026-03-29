package com.code2cash.leaderboard.service;

import com.code2cash.leaderboard.dto.LeaderboardEntryResponse;
import com.code2cash.leaderboard.model.LeaderboardEntry;
import com.code2cash.leaderboard.repository.LeaderboardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Leaderboard management
 * Tracks highest bids weekly and provides leaderboard data
 */
@Service
public class LeaderboardService {
    
    @Autowired
    private LeaderboardRepository leaderboardRepository;
    
    @Value("${leaderboard.top-entries:10}")
    private int topEntriesLimit;
    
    /**
     * Get current week's leaderboard (top N entries)
     */
    public List<LeaderboardEntryResponse> getWeeklyLeaderboard() {
        LocalDateTime[] weekRange = getCurrentWeekRange();
        List<LeaderboardEntry> entries = leaderboardRepository.findTopEntriesForWeek(
                weekRange[0], 
                weekRange[1], 
                topEntriesLimit
        );
        
        return entries.stream()
                .map((entry) -> new LeaderboardEntryResponse(
                        (long) (entries.indexOf(entry) + 1),
                        entry.getAuctionId(),
                        entry.getItemId(),
                        entry.getBidderId(),
                        entry.getBidderName(),
                        entry.getBidAmount(),
                        entry.getSellerId(),
                        entry.getSellerName(),
                        entry.getBidTime()
                ))
                .collect(Collectors.toList());
    }
    
    /**
     * Get leaderboard for a specific week
     */
    public List<LeaderboardEntryResponse> getWeeklyLeaderboard(LocalDateTime weekStart) {
        LocalDateTime[] weekRange = getWeekRange(weekStart);
        List<LeaderboardEntry> entries = leaderboardRepository.findTopEntriesForWeek(
                weekRange[0], 
                weekRange[1], 
                topEntriesLimit
        );
        
        return entries.stream()
                .map((entry) -> new LeaderboardEntryResponse(
                        (long) (entries.indexOf(entry) + 1),
                        entry.getAuctionId(),
                        entry.getItemId(),
                        entry.getBidderId(),
                        entry.getBidderName(),
                        entry.getBidAmount(),
                        entry.getSellerId(),
                        entry.getSellerName(),
                        entry.getBidTime()
                ))
                .collect(Collectors.toList());
    }
    
    /**
     * Add a bid entry to the leaderboard
     */
    public void addBidEntry(String auctionId, String itemId, String bidderId, String bidderName,
                           java.math.BigDecimal bidAmount, String sellerId, String sellerName) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime[] weekRange = getCurrentWeekRange();
        
        LeaderboardEntry entry = new LeaderboardEntry(
                auctionId, itemId, bidderId, bidderName, bidAmount,
                sellerId, sellerName, now, weekRange[0], weekRange[1]
        );
        
        leaderboardRepository.save(entry);
    }
    
    /**
     * Get bidder's current week stats
     */
    public List<LeaderboardEntryResponse> getBidderWeeklyStats(String bidderId) {
        LocalDateTime[] weekRange = getCurrentWeekRange();
        List<LeaderboardEntry> entries = leaderboardRepository
                .findByBidderIdAndWeekStartAndWeekEndOrderByBidAmountDesc(
                        bidderId, weekRange[0], weekRange[1]
                );
        
        return entries.stream()
                .map((entry) -> new LeaderboardEntryResponse(
                        null,
                        entry.getAuctionId(),
                        entry.getItemId(),
                        entry.getBidderId(),
                        entry.getBidderName(),
                        entry.getBidAmount(),
                        entry.getSellerId(),
                        entry.getSellerName(),
                        entry.getBidTime()
                ))
                .collect(Collectors.toList());
    }
    
    /**
     * Clear old leaderboard entries (weekly cleanup)
     * Scheduled to run every Monday at midnight
     */
    @Scheduled(cron = "0 0 0 * * MON")  // Every Monday at midnight
    public void cleanupOldEntries() {
        LocalDateTime fourWeeksAgo = LocalDateTime.now().minusWeeks(4);
        long deletedCount = leaderboardRepository.deleteByWeekEndBefore(fourWeeksAgo);
        System.out.println("Cleaned up " + deletedCount + " old leaderboard entries");
    }
    
    /**
     * Get current week's date range (Monday to Sunday)
     */
    private LocalDateTime[] getCurrentWeekRange() {
        return getWeekRange(LocalDateTime.now());
    }
    
    /**
     * Get week range for a given date (Monday to Sunday)
     */
    private LocalDateTime[] getWeekRange(LocalDateTime dateTime) {
        LocalDateTime monday = dateTime.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime sunday = monday.plusDays(6)
                .withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        
        return new LocalDateTime[]{monday, sunday};
    }
    
    /**
     * Get total bids for current week
     */
    public long getWeeklyBidCount() {
        LocalDateTime[] weekRange = getCurrentWeekRange();
        return leaderboardRepository.countByWeekStartAndWeekEnd(weekRange[0], weekRange[1]);
    }
}

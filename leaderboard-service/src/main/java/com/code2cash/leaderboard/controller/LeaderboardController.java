package com.code2cash.leaderboard.controller;

import com.code2cash.leaderboard.dto.LeaderboardEntryResponse;
import com.code2cash.leaderboard.service.LeaderboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Leaderboard endpoints
 * Provides weekly leaderboard data and bid statistics
 */
@RestController
@RequestMapping("/api/leaderboard")
@CrossOrigin(origins = "*")
public class LeaderboardController {
    
    @Autowired
    private LeaderboardService leaderboardService;
    
    /**
     * GET /api/leaderboard
     * Get current week's leaderboard (top 10 highest bids)
     */
    @GetMapping
    public ResponseEntity<?> getWeeklyLeaderboard() {
        try {
            List<LeaderboardEntryResponse> entries = leaderboardService.getWeeklyLeaderboard();
            
            Map<String, Object> response = new HashMap<>();
            response.put("count", entries.size());
            response.put("entries", entries);
            response.put("generatedAt", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * GET /api/leaderboard/week/{year}/{week}
     * Get leaderboard for specific week
     */
    @GetMapping("/week/{year}/{week}")
    public ResponseEntity<?> getWeeklyLeaderboard(
            @PathVariable int year,
            @PathVariable int week) {
        try {
            // Calculate date for specified year and week
            LocalDateTime weekStart = LocalDateTime.now()
                    .withYear(year)
                    .withDayOfYear(week * 7 - 6);  // Simplified week calculation
            
            List<LeaderboardEntryResponse> entries = leaderboardService.getWeeklyLeaderboard(weekStart);
            
            Map<String, Object> response = new HashMap<>();
            response.put("year", year);
            response.put("week", week);
            response.put("count", entries.size());
            response.put("entries", entries);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * GET /api/leaderboard/bidder/{bidderId}
     * Get bidder's stats for current week
     */
    @GetMapping("/bidder/{bidderId}")
    public ResponseEntity<?> getBidderWeeklyStats(
            @PathVariable String bidderId) {
        try {
            List<LeaderboardEntryResponse> entries = leaderboardService.getBidderWeeklyStats(bidderId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("bidderId", bidderId);
            response.put("bidCount", entries.size());
            response.put("bids", entries);
            
            if (!entries.isEmpty()) {
                // Calculate highest bid
                double highestBid = entries.stream()
                        .mapToDouble(e -> e.getBidAmount().doubleValue())
                        .max()
                        .orElse(0.0);
                response.put("highestBid", highestBid);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * GET /api/leaderboard/stats
     * Get weekly statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getWeeklyStats() {
        try {
            long bidCount = leaderboardService.getWeeklyBidCount();
            List<LeaderboardEntryResponse> topBids = leaderboardService.getWeeklyLeaderboard();
            
            Map<String, Object> response = new HashMap<>();
            response.put("weeklyBidCount", bidCount);
            response.put("topBidsCount", topBids.size());
            
            if (!topBids.isEmpty()) {
                double highestBid = topBids.get(0).getBidAmount().doubleValue();
                double totalBidValue = topBids.stream()
                        .mapToDouble(e -> e.getBidAmount().doubleValue())
                        .sum();
                
                response.put("highestBid", highestBid);
                response.put("totalBidValue", totalBidValue);
                response.put("averageBid", totalBidValue / topBids.size());
            }
            
            response.put("generatedAt", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Helper method to create error response
     */
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}

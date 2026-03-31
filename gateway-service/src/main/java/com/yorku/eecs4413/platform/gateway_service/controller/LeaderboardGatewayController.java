package com.yorku.eecs4413.platform.gateway_service.controller;

import com.yorku.eecs4413.platform.gateway_service.client.LeaderboardClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Gateway controller for Leaderboard service.
 */
@RestController
@RequestMapping("/api/leaderboard")
@CrossOrigin(origins = "*")
public class LeaderboardGatewayController {

    private final LeaderboardClient leaderboardClient;

    public LeaderboardGatewayController(LeaderboardClient leaderboardClient) {
        this.leaderboardClient = leaderboardClient;
    }

    @GetMapping
    public ResponseEntity<String> getWeeklyLeaderboard() {
        return leaderboardClient.get("/api/leaderboard");
    }

    @GetMapping("/week/{year}/{week}")
    public ResponseEntity<String> getWeekLeaderboard(@PathVariable int year, @PathVariable int week) {
        return leaderboardClient.get(String.format("/api/leaderboard/week/%d/%d", year, week));
    }

    @GetMapping("/bidder/{bidderId}")
    public ResponseEntity<String> getBidderStats(@PathVariable String bidderId) {
        return leaderboardClient.get("/api/leaderboard/bidder/" + bidderId);
    }

    @GetMapping("/stats")
    public ResponseEntity<String> getWeeklyStats() {
        return leaderboardClient.get("/api/leaderboard/stats");
    }
}

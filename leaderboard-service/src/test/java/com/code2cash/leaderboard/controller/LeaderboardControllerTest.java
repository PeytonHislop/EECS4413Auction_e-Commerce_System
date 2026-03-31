package com.code2cash.leaderboard.controller;

import com.code2cash.leaderboard.dto.LeaderboardEntryResponse;
import com.code2cash.leaderboard.dto.TopBidderResponse;
import com.code2cash.leaderboard.service.LeaderboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for LeaderboardController
 */
@WebMvcTest(LeaderboardController.class)
public class LeaderboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LeaderboardService leaderboardService;

    private LeaderboardEntryResponse mockEntry;
    private List<LeaderboardEntryResponse> mockLeaderboard;

    @BeforeEach
    void setUp() {
        mockEntry = new LeaderboardEntryResponse(
                1L,
                "AUC001",
                "ITEM001",
                "BUYER001",
                "John Bidder",
                BigDecimal.valueOf(500.0),
                "SELLER001",
                "Jane Seller",
                LocalDateTime.now()
        );
        
        mockLeaderboard = Arrays.asList(mockEntry);
    }

    @Test
    @DisplayName("GET /api/leaderboard - Get weekly leaderboard")
    void testGetWeeklyLeaderboard_ReturnsTopBids() throws Exception {
        // Arrange
        when(leaderboardService.getWeeklyLeaderboard()).thenReturn(mockLeaderboard);

        // Act & Assert
        mockMvc.perform(get("/api/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.entries[0].rank").value(1))
                .andExpect(jsonPath("$.entries[0].bidderId").value("BUYER001"))
                .andExpect(jsonPath("$.entries[0].bidAmount").value(500.0))
                .andExpect(jsonPath("$.generatedAt").exists());

        verify(leaderboardService).getWeeklyLeaderboard();
    }

    @Test
    @DisplayName("GET /api/leaderboard - Empty leaderboard")
    void testGetWeeklyLeaderboard_Empty_ReturnsEmptyList() throws Exception {
        // Arrange
        when(leaderboardService.getWeeklyLeaderboard()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0))
                .andExpect(jsonPath("$.entries").isArray());
    }

    @Test
    @DisplayName("GET /api/leaderboard/bidder/{bidderId} - Get bidder stats")
    void testGetBidderWeeklyStats_ReturnsBidderBids() throws Exception {
        // Arrange
        when(leaderboardService.getBidderWeeklyStats("BUYER001"))
                .thenReturn(mockLeaderboard);

        // Act & Assert
        mockMvc.perform(get("/api/leaderboard/bidder/BUYER001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bidderId").value("BUYER001"))
                .andExpect(jsonPath("$.bidCount").value(1))
                .andExpect(jsonPath("$.bids[0].bidAmount").value(500.0))
                .andExpect(jsonPath("$.highestBid").value(500.0));

        verify(leaderboardService).getBidderWeeklyStats("BUYER001");
    }

    @Test
    @DisplayName("GET /api/leaderboard/bidder/{bidderId} - No bids for bidder")
    void testGetBidderWeeklyStats_NoBids_ReturnsEmpty() throws Exception {
        // Arrange
        when(leaderboardService.getBidderWeeklyStats("BUYER999"))
                .thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/leaderboard/bidder/BUYER999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bidderId").value("BUYER999"))
                .andExpect(jsonPath("$.bidCount").value(0));
    }

    @Test
    @DisplayName("GET /api/leaderboard/stats - Get weekly statistics")
    void testGetWeeklyStats_ReturnsAggregatedStats() throws Exception {
        // Arrange
        when(leaderboardService.getWeeklyBidCount()).thenReturn(5L);
        when(leaderboardService.getWeeklyLeaderboard()).thenReturn(mockLeaderboard);

        // Act & Assert
        mockMvc.perform(get("/api/leaderboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weeklyBidCount").value(5))
                .andExpect(jsonPath("$.topBidsCount").value(1))
                .andExpect(jsonPath("$.highestBid").value(500.0))
                .andExpect(jsonPath("$.generatedAt").exists());

        verify(leaderboardService).getWeeklyBidCount();
        verify(leaderboardService).getWeeklyLeaderboard();
    }

    @Test
    @DisplayName("GET /api/leaderboard/week/{year}/{week} - Get historical week leaderboard")
    void testGetWeeklyLeaderboard_SpecificWeek_ReturnsWeekData() throws Exception {
        // Arrange
        when(leaderboardService.getWeeklyLeaderboard(any()))
                .thenReturn(mockLeaderboard);

        // Act & Assert
        mockMvc.perform(get("/api/leaderboard/week/2026/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.week").value(12))
                .andExpect(jsonPath("$.count").value(1));

        verify(leaderboardService).getWeeklyLeaderboard(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("GET /api/leaderboard/highest - Returns highest bid for period")
    void testGetHighestBidByPeriod_ReturnsEntry() throws Exception {
        when(leaderboardService.getHighestBidForPeriod("WEEK")).thenReturn(Optional.of(mockEntry));

        mockMvc.perform(get("/api/leaderboard/highest").param("period", "WEEK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.period").value("WEEK"))
                .andExpect(jsonPath("$.hasEntry").value(true))
                .andExpect(jsonPath("$.entry.bidAmount").value(500.0));

        verify(leaderboardService).getHighestBidForPeriod("WEEK");
    }

    @Test
    @DisplayName("GET /api/leaderboard/highest - Invalid period returns 400")
    void testGetHighestBidByPeriod_Invalid_ReturnsBadRequest() throws Exception {
        when(leaderboardService.getHighestBidForPeriod("MONTH"))
                .thenThrow(new IllegalArgumentException("Unsupported period"));

        mockMvc.perform(get("/api/leaderboard/highest").param("period", "MONTH"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("GET /api/leaderboard/top-bidders - Returns top bidder entries")
    void testGetTopBidders_ReturnsEntries() throws Exception {
        List<TopBidderResponse> entries = Arrays.asList(
                new TopBidderResponse(1L, "BUYER001", "John", 3L, BigDecimal.valueOf(500), BigDecimal.valueOf(1200))
        );
        when(leaderboardService.getTopBiddersForPeriod("WEEK", 5)).thenReturn(entries);

        mockMvc.perform(get("/api/leaderboard/top-bidders").param("period", "WEEK").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.entries[0].bidderId").value("BUYER001"))
                .andExpect(jsonPath("$.entries[0].highestBid").value(500));
    }

    @Test
    @DisplayName("GET /api/leaderboard/top-bidders - Invalid input returns 400")
    void testGetTopBidders_Invalid_ReturnsBadRequest() throws Exception {
        when(leaderboardService.getTopBiddersForPeriod("WEEK", 0))
                .thenThrow(new IllegalArgumentException("Limit must be greater than 0."));

        mockMvc.perform(get("/api/leaderboard/top-bidders").param("period", "WEEK").param("limit", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
}

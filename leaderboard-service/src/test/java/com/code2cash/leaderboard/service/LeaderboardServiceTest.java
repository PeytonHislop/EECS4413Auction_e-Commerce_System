package com.code2cash.leaderboard.service;

import com.code2cash.leaderboard.dto.LeaderboardEntryResponse;
import com.code2cash.leaderboard.model.LeaderboardEntry;
import com.code2cash.leaderboard.repository.LeaderboardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LeaderboardService
 */
@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {

    @Mock
    private LeaderboardRepository leaderboardRepository;

    @InjectMocks
    private LeaderboardService leaderboardService;

    private LeaderboardEntry mockEntry;

    @BeforeEach
    void setUp() {
        mockEntry = new LeaderboardEntry(
                "AUC001", "ITEM001", "BUYER001", "John Bidder",
                BigDecimal.valueOf(500.0), "SELLER001", "Jane Seller",
                LocalDateTime.now(),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1)
        );
    }

    @Test
    @DisplayName("getWeeklyLeaderboard - Returns top entries for current week")
    void testGetWeeklyLeaderboard_ReturnsTopEntries() {
        // Arrange
        List<LeaderboardEntry> entries = Arrays.asList(mockEntry);
        when(leaderboardRepository.findTopEntriesForWeek(any(), any(), anyInt()))
                .thenReturn(entries);

        // Act
        List<LeaderboardEntryResponse> result = leaderboardService.getWeeklyLeaderboard();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getRank());
        assertEquals("BUYER001", result.get(0).getBidderId());
        verify(leaderboardRepository).findTopEntriesForWeek(any(), any(), anyInt());
    }

    @Test
    @DisplayName("getWeeklyLeaderboard - Empty when no entries")
    void testGetWeeklyLeaderboard_NoEntries_ReturnsEmpty() {
        // Arrange
        when(leaderboardRepository.findTopEntriesForWeek(any(), any(), anyInt()))
                .thenReturn(Arrays.asList());

        // Act
        List<LeaderboardEntryResponse> result = leaderboardService.getWeeklyLeaderboard();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("addBidEntry - Creates new leaderboard entry")
    void testAddBidEntry_CreatesEntry() {
        // Act
        leaderboardService.addBidEntry(
                "AUC001", "ITEM001", "BUYER001", "John",
                BigDecimal.valueOf(500.0), "SELLER001", "Jane"
        );

        // Assert
        verify(leaderboardRepository).save(any(LeaderboardEntry.class));
    }

    @Test
    @DisplayName("getBidderWeeklyStats - Returns bidder's current week bids")
    void testGetBidderWeeklyStats_ReturnsBidderBids() {
        // Arrange
        List<LeaderboardEntry> entries = Arrays.asList(mockEntry);
        when(leaderboardRepository.findByBidderIdAndWeekStartAndWeekEndOrderByBidAmountDesc(
                anyString(), any(), any()))
                .thenReturn(entries);

        // Act
        List<LeaderboardEntryResponse> result = leaderboardService.getBidderWeeklyStats("BUYER001");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("BUYER001", result.get(0).getBidderId());
    }

    @Test
    @DisplayName("getWeeklyBidCount - Returns total bid count for week")
    void testGetWeeklyBidCount_ReturnsBidCount() {
        // Arrange
        when(leaderboardRepository.countByWeekStartAndWeekEnd(any(), any()))
                .thenReturn(5L);

        // Act
        long result = leaderboardService.getWeeklyBidCount();

        // Assert
        assertEquals(5L, result);
        verify(leaderboardRepository).countByWeekStartAndWeekEnd(any(), any());
    }

    @Test
    @DisplayName("cleanupOldEntries - Deletes entries older than 4 weeks")
    void testCleanupOldEntries_DeletesOldEntries() {
        // Arrange
        when(leaderboardRepository.deleteByWeekEndBefore(any()))
                .thenReturn(10L);

        // Act
        leaderboardService.cleanupOldEntries();

        // Assert
        verify(leaderboardRepository).deleteByWeekEndBefore(any());
    }
}

package com.code2cash.leaderboard.service;

import com.code2cash.leaderboard.dto.LeaderboardEntryResponse;
import com.code2cash.leaderboard.dto.TopBidderResponse;
import com.code2cash.leaderboard.model.LeaderboardEntry;
import com.code2cash.leaderboard.repository.LeaderboardRepository;
import com.code2cash.leaderboard.repository.TopBidderProjection;
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
import java.util.Optional;

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
        when(leaderboardRepository.findTopEntriesByBidTimeRange(any(), any(), anyInt()))
                .thenReturn(entries);

        // Act
        List<LeaderboardEntryResponse> result = leaderboardService.getWeeklyLeaderboard();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getRank());
        assertEquals("BUYER001", result.get(0).getBidderId());
        verify(leaderboardRepository).findTopEntriesByBidTimeRange(any(), any(), anyInt());
    }

    @Test
    @DisplayName("getWeeklyLeaderboard - Empty when no entries")
    void testGetWeeklyLeaderboard_NoEntries_ReturnsEmpty() {
        // Arrange
        when(leaderboardRepository.findTopEntriesByBidTimeRange(any(), any(), anyInt()))
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
        when(leaderboardRepository.findByBidderIdAndBidTimeBetweenOrderByBidAmountDesc(
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
        when(leaderboardRepository.countByBidTimeBetween(any(), any()))
                .thenReturn(5L);

        // Act
        long result = leaderboardService.getWeeklyBidCount();

        // Assert
        assertEquals(5L, result);
        verify(leaderboardRepository).countByBidTimeBetween(any(), any());
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

    @Test
    @DisplayName("getHighestBidForPeriod - Returns highest bid for valid period")
    void testGetHighestBidForPeriod_ReturnsHighestBid() {
        // Arrange
        when(leaderboardRepository.findHighestEntryByBidTimeRange(any(), any()))
                .thenReturn(Arrays.asList(mockEntry));

        // Act
        Optional<LeaderboardEntryResponse> result = leaderboardService.getHighestBidForPeriod("WEEK");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("BUYER001", result.get().getBidderId());
        verify(leaderboardRepository).findHighestEntryByBidTimeRange(any(), any());
    }

    @Test
    @DisplayName("getHighestBidForPeriod - Throws for invalid period")
    void testGetHighestBidForPeriod_InvalidPeriod_Throws() {
        assertThrows(IllegalArgumentException.class, () -> leaderboardService.getHighestBidForPeriod("MONTH"));
        verify(leaderboardRepository, never()).findHighestEntryByBidTimeRange(any(), any());
    }

    @Test
    @DisplayName("getTopBiddersForPeriod - Returns aggregated top bidders")
    void testGetTopBiddersForPeriod_ReturnsAggregates() {
        TopBidderProjection projection = mock(TopBidderProjection.class);
        when(projection.getBidderId()).thenReturn("BUYER001");
        when(projection.getBidderName()).thenReturn("John Bidder");
        when(projection.getBidCount()).thenReturn(3L);
        when(projection.getHighestBid()).thenReturn(BigDecimal.valueOf(500.0));
        when(projection.getTotalBidValue()).thenReturn(BigDecimal.valueOf(1200.0));
        when(leaderboardRepository.findTopBiddersByBidTimeRange(any(), any(), eq(5)))
                .thenReturn(Arrays.asList(projection));

        List<TopBidderResponse> result = leaderboardService.getTopBiddersForPeriod("WEEK", 5);

        assertEquals(1, result.size());
        assertEquals("BUYER001", result.get(0).getBidderId());
        assertEquals(1L, result.get(0).getRank());
        verify(leaderboardRepository).findTopBiddersByBidTimeRange(any(), any(), eq(5));
    }

    @Test
    @DisplayName("getTopBiddersForPeriod - Rejects invalid limit")
    void testGetTopBiddersForPeriod_InvalidLimit_Throws() {
        assertThrows(IllegalArgumentException.class, () -> leaderboardService.getTopBiddersForPeriod("WEEK", 0));
    }
}

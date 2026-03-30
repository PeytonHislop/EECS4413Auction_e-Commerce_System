package com.code2cash.auction.service;

import com.code2cash.auction.dao.AuctionDAO;
import com.code2cash.auction.dao.BidDAO;
import com.code2cash.auction.dto.BidResponse;
import com.code2cash.auction.dto.PlaceBidRequest;
import com.code2cash.auction.exception.AuctionClosedException;
import com.code2cash.auction.exception.AuctionNotFoundException;
import com.code2cash.auction.exception.InvalidBidException;
import com.code2cash.auction.model.Auction;
import com.code2cash.auction.model.AuctionStatus;
import com.code2cash.auction.model.Bid;
import com.code2cash.auction.util.ServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BidServiceTest {

    @Mock
    private BidDAO bidDAO;

    @Mock
    private AuctionDAO auctionDAO;

    @Mock
    private ServiceClient serviceClient;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private BidServiceImpl bidService;

    private ServiceClient.ValidationResponse validValidation;

    @BeforeEach
    void setUp() {
        validValidation = new ServiceClient.ValidationResponse();
        validValidation.setValid(true);
        validValidation.setUserId("BUYER_777");
        validValidation.setRole("BUYER");
    }

    @Test
    @DisplayName("placeBid - invalid token -> RuntimeException")
    void testPlaceBid_InvalidToken_Throws() {
        String token = "bad-token";
        ServiceClient.ValidationResponse invalid = new ServiceClient.ValidationResponse();
        invalid.setValid(false);

        when(serviceClient.validateToken(token)).thenReturn(invalid);

        PlaceBidRequest request = new PlaceBidRequest();
        request.setBidderId("BUYER_1");
        request.setBidAmount(BigDecimal.valueOf(150.00));

        assertThrows(RuntimeException.class, () -> bidService.placeBid("AUC_1", request, token));
    }

    @Test
    @DisplayName("placeBid - auction missing -> AuctionNotFoundException")
    void testPlaceBid_AuctionNotFound_Throws() {
        String token = "token";
        when(serviceClient.validateToken(token)).thenReturn(validValidation);
        when(serviceClient.authorizeRole(token, "BUYER")).thenReturn(true);
        when(auctionDAO.findById("AUC_MISSING")).thenReturn(Optional.empty());

        PlaceBidRequest request = new PlaceBidRequest();
        request.setBidderId("BUYER_1");
        request.setBidAmount(BigDecimal.valueOf(150.00));

        assertThrows(AuctionNotFoundException.class,
                () -> bidService.placeBid("AUC_MISSING", request, token));
    }

    @Test
    @DisplayName("placeBid - auction not active -> AuctionClosedException")
    void testPlaceBid_AuctionNotActive_Throws() {
        String token = "token";
        when(serviceClient.validateToken(token)).thenReturn(validValidation);
        when(serviceClient.authorizeRole(token, "BUYER")).thenReturn(true);

        Auction auction = new Auction();
        auction.setAuctionId("AUC_CLOSED");
        auction.setStatus(AuctionStatus.CLOSED);
        auction.setEndTime(LocalDateTime.now().minusHours(1));
        auction.setCurrentHighestBid(BigDecimal.valueOf(100.00));
        auction.setSellerId("SELLER_1");
        auction.setItemId("ITEM_1");

        when(auctionDAO.findById("AUC_CLOSED")).thenReturn(Optional.of(auction));

        PlaceBidRequest request = new PlaceBidRequest();
        request.setBidderId("BUYER_1");
        request.setBidAmount(BigDecimal.valueOf(150.00));

        assertThrows(AuctionClosedException.class,
                () -> bidService.placeBid("AUC_CLOSED", request, token));
    }

    @Test
    @DisplayName("placeBid - bid not higher than current -> InvalidBidException")
    void testPlaceBid_BidNotHigherThanCurrent_Throws() {
        String token = "token";
        when(serviceClient.validateToken(token)).thenReturn(validValidation);
        when(serviceClient.authorizeRole(token, "BUYER")).thenReturn(true);

        Auction auction = new Auction();
        auction.setAuctionId("AUC_BID_TOO_LOW");
        auction.setStatus(AuctionStatus.ACTIVE);
        auction.setEndTime(LocalDateTime.now().plusHours(1));
        auction.setCurrentHighestBid(BigDecimal.valueOf(200.00));
        auction.setSellerId("SELLER_1");
        auction.setItemId("ITEM_1");
        auction.setReservePrice(BigDecimal.valueOf(200.00));

        when(auctionDAO.findById("AUC_BID_TOO_LOW")).thenReturn(Optional.of(auction));

        PlaceBidRequest request = new PlaceBidRequest();
        request.setBidderId("BUYER_1");
        request.setBidAmount(BigDecimal.valueOf(200.00)); // equal -> invalid

        assertThrows(InvalidBidException.class,
                () -> bidService.placeBid("AUC_BID_TOO_LOW", request, token));
    }

    @Test
    @DisplayName("placeBid - success -> writes bid + updates highest bid + returns success response")
    void testPlaceBid_Success() {
        String token = "token";
        String auctionId = "AUC_OK";
        String bidderId = "BUYER_OK";

        when(serviceClient.validateToken(token)).thenReturn(validValidation);
        when(serviceClient.authorizeRole(token, "BUYER")).thenReturn(true);

        Auction auction = new Auction();
        auction.setAuctionId(auctionId);
        auction.setStatus(AuctionStatus.ACTIVE);
        auction.setEndTime(LocalDateTime.now().plusHours(2));
        auction.setCurrentHighestBid(BigDecimal.valueOf(100.00));
        auction.setSellerId("SELLER_OK");
        auction.setItemId("ITEM_OK");
        auction.setReservePrice(BigDecimal.valueOf(50.00));

        when(auctionDAO.findById(auctionId)).thenReturn(Optional.of(auction));

        BigDecimal bidAmount = BigDecimal.valueOf(150.00);
        PlaceBidRequest request = new PlaceBidRequest();
        request.setBidderId(bidderId);
        request.setBidAmount(bidAmount);

        Bid createdBid = new Bid();
        createdBid.setBidId("BID_OK");
        createdBid.setAuctionId(auctionId);
        createdBid.setBidderId(bidderId);
        createdBid.setBidAmount(bidAmount);
        createdBid.setBidTimestamp(LocalDateTime.now().minusMinutes(1));
        when(bidDAO.createBid(any(Bid.class))).thenReturn(createdBid);
        when(auctionDAO.updateHighestBid(auctionId, bidAmount, bidderId)).thenReturn(true);

        // Keep leaderboard lookup simple.
        when(serviceClient.getUserProfile(anyString())).thenReturn(null);
        when(serviceClient.addLeaderboardEntry(anyString(), anyString(), anyString(), anyString(), any(BigDecimal.class), anyString(), anyString()))
                .thenReturn(true);

        BidResponse response = bidService.placeBid(auctionId, request, token);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("BID_OK", response.getBidId());
        assertEquals(auctionId, response.getAuctionId());
        assertEquals(bidderId, response.getBidderId());
        assertEquals(bidAmount, response.getBidAmount());

        // Verify bidDAO receives the expected bid content.
        ArgumentCaptor<Bid> bidCaptor = ArgumentCaptor.forClass(Bid.class);
        verify(bidDAO).createBid(bidCaptor.capture());
        Bid bidSent = bidCaptor.getValue();
        assertEquals(auctionId, bidSent.getAuctionId());
        assertEquals(bidderId, bidSent.getBidderId());
        assertEquals(bidAmount, bidSent.getBidAmount());

        verify(auctionDAO).updateHighestBid(auctionId, bidAmount, bidderId);
        verify(messagingTemplate).convertAndSend(containsTopic(auctionId), response);
    }

    private static String containsTopic(String auctionId) {
        return "/topic/auction/" + auctionId + "/bids";
    }
}


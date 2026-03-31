package com.code2cash.auction.service;

import com.code2cash.auction.dao.AuctionDAO;
import com.code2cash.auction.dao.BidDAO;
import com.code2cash.auction.dto.AuctionResponse;
import com.code2cash.auction.dto.CreateAuctionRequest;
import com.code2cash.auction.dto.PlaceBidRequest;
import com.code2cash.auction.exception.AuctionNotFoundException;
import com.code2cash.auction.model.Auction;
import com.code2cash.auction.model.AuctionStatus;
import com.code2cash.auction.model.Bid;
import com.code2cash.auction.util.ServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionServiceTest {

    @Mock
    private AuctionDAO auctionDAO;

    @Mock
    private BidDAO bidDAO;

    @Mock
    private ServiceClient serviceClient;

    @InjectMocks
    private AuctionServiceImpl auctionService;

    private ServiceClient.ValidationResponse validBuyerSellerValidation;

    @BeforeEach
    void setUp() {
        validBuyerSellerValidation = new ServiceClient.ValidationResponse();
        validBuyerSellerValidation.setValid(true);
        validBuyerSellerValidation.setUserId("SELLER_1");
    }

    @Test
    @DisplayName("createAuction - returns ACTIVE auction response")
    void testCreateAuction_ReturnsActiveAuctionResponse() {
        String token = "token";

        CreateAuctionRequest request = new CreateAuctionRequest();
        request.setItemId("ITEM_1");
        request.setSellerId("IGNORED_IN_SERVICE"); // service derives sellerId from token
        request.setDurationHours(24);
        request.setReservePrice(BigDecimal.valueOf(123.45));

        when(serviceClient.validateToken(token)).thenReturn(validBuyerSellerValidation);
        when(serviceClient.authorizeRole(token, "SELLER")).thenReturn(true);
        when(serviceClient.verifyItemExists("ITEM_1")).thenReturn(true);

        // Return an Auction instance that matches what convertToResponse reads.
        when(auctionDAO.createAuction(any(Auction.class))).thenAnswer(inv -> {
            Auction a = inv.getArgument(0);
            a.setAuctionId("AUC_1");
            a.setCurrentHighestBid(BigDecimal.ZERO);
            return a;
        });

        AuctionResponse response = auctionService.createAuction(request, token);
        assertNotNull(response);
        assertEquals("AUC_1", response.getAuctionId());
        assertEquals("ITEM_1", response.getItemId());
        assertEquals("SELLER_1", response.getSellerId());
        assertEquals(AuctionStatus.ACTIVE, response.getStatus());
        assertEquals(BigDecimal.valueOf(123.45), response.getReservePrice());
        assertEquals(BigDecimal.ZERO, response.getCurrentHighestBid());
        assertNotNull(response.getStartTime());
        assertNotNull(response.getEndTime());
    }

    @Test
    @DisplayName("getAuction - missing auction -> AuctionNotFoundException")
    void testGetAuction_UnknownAuction_Throws() {
        when(auctionDAO.findById("MISSING")).thenReturn(Optional.empty());

        assertThrows(AuctionNotFoundException.class, () -> auctionService.getAuction("MISSING"));
    }

    @Test
    @DisplayName("closeAuction - winner found and reserve met -> status CLOSED + payment initiated")
    void testCloseAuction_ReserveMet_ClosesAuctionAndInitiatesPayment() {
        String auctionId = "AUC_CLOSE_1";
        String winnerId = "BUYER_1";

        Auction auction = new Auction();
        auction.setAuctionId(auctionId);
        auction.setReservePrice(BigDecimal.valueOf(100.00));
        auction.setItemId("ITEM_1");
        auction.setSellerId("SELLER_1");
        auction.setStatus(AuctionStatus.ACTIVE);
        auction.setStartTime(LocalDateTime.now().minusHours(1));
        auction.setEndTime(LocalDateTime.now().plusHours(1));

        Bid highestBid = new Bid();
        highestBid.setBidderId(winnerId);
        highestBid.setBidAmount(BigDecimal.valueOf(150.00));

        Auction updatedAuction = new Auction();
        updatedAuction.setAuctionId(auctionId);
        updatedAuction.setReservePrice(BigDecimal.valueOf(100.00));
        updatedAuction.setItemId("ITEM_1");
        updatedAuction.setSellerId("SELLER_1");
        updatedAuction.setStatus(AuctionStatus.CLOSED);
        updatedAuction.setWinnerId(winnerId);
        updatedAuction.setStartTime(auction.getStartTime());
        updatedAuction.setEndTime(auction.getEndTime());
        updatedAuction.setCurrentHighestBid(highestBid.getBidAmount());
        updatedAuction.setCurrentHighestBidderId(winnerId);

        when(auctionDAO.findById(auctionId)).thenReturn(Optional.of(auction), Optional.of(updatedAuction));
        when(bidDAO.findHighestBidByAuctionId(auctionId)).thenReturn(Optional.of(highestBid));
        when(auctionDAO.closeAuction(eq(auctionId), eq(winnerId), eq(AuctionStatus.CLOSED))).thenReturn(true);
        when(serviceClient.initiatePayment(eq(auctionId), eq(winnerId), eq(150.00))).thenReturn(true);

        AuctionResponse response = auctionService.closeAuction(auctionId);
        assertNotNull(response);
        assertEquals(AuctionStatus.CLOSED, response.getStatus());
        assertEquals(winnerId, response.getWinnerId());

        verify(serviceClient).initiatePayment(eq(auctionId), eq(winnerId), eq(150.00));
    }

    @Test
    @DisplayName("closeAuction - reserve not met -> NO_SALE + no payment")
    void testCloseAuction_ReserveNotMet_NoSale_NoPayment() {
        String auctionId = "AUC_CLOSE_2";

        Auction auction = new Auction();
        auction.setAuctionId(auctionId);
        auction.setReservePrice(BigDecimal.valueOf(200.00));
        auction.setItemId("ITEM_1");
        auction.setSellerId("SELLER_1");
        auction.setStatus(AuctionStatus.ACTIVE);
        auction.setStartTime(LocalDateTime.now().minusHours(1));
        auction.setEndTime(LocalDateTime.now().plusHours(1));

        Bid highestBid = new Bid();
        highestBid.setBidderId("BUYER_1");
        highestBid.setBidAmount(BigDecimal.valueOf(150.00)); // < reserve

        Auction updatedAuction = new Auction();
        updatedAuction.setAuctionId(auctionId);
        updatedAuction.setReservePrice(BigDecimal.valueOf(200.00));
        updatedAuction.setItemId("ITEM_1");
        updatedAuction.setSellerId("SELLER_1");
        updatedAuction.setStatus(AuctionStatus.NO_SALE);
        updatedAuction.setWinnerId(null);
        updatedAuction.setStartTime(auction.getStartTime());
        updatedAuction.setEndTime(auction.getEndTime());

        when(auctionDAO.findById(auctionId)).thenReturn(Optional.of(auction), Optional.of(updatedAuction));
        when(bidDAO.findHighestBidByAuctionId(auctionId)).thenReturn(Optional.of(highestBid));
        when(auctionDAO.closeAuction(eq(auctionId), isNull(), eq(AuctionStatus.NO_SALE))).thenReturn(true);

        AuctionResponse response = auctionService.closeAuction(auctionId);
        assertNotNull(response);
        assertEquals(AuctionStatus.NO_SALE, response.getStatus());
        assertNull(response.getWinnerId());

        verify(serviceClient, never()).initiatePayment(anyString(), anyString(), anyDouble());
    }

    @Test
    @DisplayName("getActiveAuctions - maps auction entities to AuctionResponse DTOs")
    void testGetActiveAuctions_MapsToResponses() {
        Auction a1 = new Auction();
        a1.setAuctionId("AUC_ACTIVE_1");
        a1.setItemId("ITEM_1");
        a1.setSellerId("SELLER_1");
        a1.setStatus(AuctionStatus.ACTIVE);
        a1.setStartTime(LocalDateTime.now().minusMinutes(10));
        a1.setEndTime(LocalDateTime.now().plusMinutes(30));
        a1.setReservePrice(BigDecimal.valueOf(10.00));
        a1.setCurrentHighestBid(BigDecimal.ZERO);

        when(auctionDAO.findActiveAuctions()).thenReturn(List.of(a1));

        List<AuctionResponse> result = auctionService.getActiveAuctions();
        assertEquals(1, result.size());
        assertEquals("AUC_ACTIVE_1", result.get(0).getAuctionId());
        assertEquals(AuctionStatus.ACTIVE, result.get(0).getStatus());
    }
}


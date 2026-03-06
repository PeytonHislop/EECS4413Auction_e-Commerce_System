package com.code2cash.auction.service;

import com.code2cash.auction.dao.AuctionDAO;
import com.code2cash.auction.dao.BidDAO;
import com.code2cash.auction.dto.AuctionResponse;
import com.code2cash.auction.dto.CreateAuctionRequest;
import com.code2cash.auction.exception.AuctionNotFoundException;
import com.code2cash.auction.model.Auction;
import com.code2cash.auction.model.AuctionStatus;
import com.code2cash.auction.model.Bid;
import com.code2cash.auction.util.ServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of AuctionService
 * Contains all business logic for auction operations
 */
@Service
public class AuctionServiceImpl implements AuctionService {
    
    @Autowired
    private AuctionDAO auctionDAO;
    
    @Autowired
    private BidDAO bidDAO;
    
    @Autowired
    private ServiceClient serviceClient;
    
    @Override
    public AuctionResponse createAuction(CreateAuctionRequest request, String token) {
        // 1. Validate token and get user info
        ServiceClient.ValidationResponse validation = serviceClient.validateToken(token);
        if (!validation.isValid()) {
            throw new RuntimeException("Invalid authentication token");
        }
        
        // 2. Get seller ID from validation response
        String sellerId = validation.getUserId();
        if (sellerId == null) {
            throw new RuntimeException("Could not determine seller ID");
        }
        
        // 3. Verify seller has SELLER role
        if (!serviceClient.authorizeRole(token, "SELLER")) {
            throw new RuntimeException("User does not have SELLER permissions");
        }
        
        // 4. Verify item exists in catalogue
        if (!serviceClient.verifyItemExists(request.getItemId())) {
            throw new RuntimeException("Item does not exist: " + request.getItemId());
        }
        
        // 5. Calculate end time
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(request.getDurationHours());
        
        // 6. Create auction object
        Auction auction = new Auction();
        auction.setItemId(request.getItemId());
        auction.setSellerId(sellerId);  // Use seller ID from IAM service
        auction.setStartTime(startTime);
        auction.setEndTime(endTime);
        auction.setStatus(AuctionStatus.ACTIVE);
        auction.setReservePrice(request.getReservePrice());
        auction.setCurrentHighestBid(BigDecimal.ZERO);
        
        // 7. Save to database
        Auction createdAuction = auctionDAO.createAuction(auction);
        
        // 8. Convert to response DTO
        return convertToResponse(createdAuction);
    }
    
    @Override
    public AuctionResponse getAuction(String auctionId) {
        Optional<Auction> auctionOpt = auctionDAO.findById(auctionId);
        
        if (auctionOpt.isEmpty()) {
            throw new AuctionNotFoundException("Auction not found: " + auctionId);
        }
        
        return convertToResponse(auctionOpt.get());
    }
    
    @Override
    public List<AuctionResponse> getActiveAuctions() {
        List<Auction> auctions = auctionDAO.findActiveAuctions();
        return auctions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<AuctionResponse> getSellerAuctions(String sellerId) {
        List<Auction> auctions = auctionDAO.findBySellerId(sellerId);
        return auctions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public AuctionResponse closeAuction(String auctionId) {
        // 1. Get auction
        Optional<Auction> auctionOpt = auctionDAO.findById(auctionId);
        if (auctionOpt.isEmpty()) {
            throw new AuctionNotFoundException("Auction not found: " + auctionId);
        }
        
        Auction auction = auctionOpt.get();
        
        // 2. Get highest bid
        Optional<Bid> highestBidOpt = bidDAO.findHighestBidByAuctionId(auctionId);
        
        // 3. Determine winner
        String winnerId = null;
        AuctionStatus finalStatus;
        
        if (highestBidOpt.isPresent()) {
            Bid highestBid = highestBidOpt.get();
            
            // Check if reserve price met
            if (highestBid.getBidAmount().compareTo(auction.getReservePrice()) >= 0) {
                // Winner!
                winnerId = highestBid.getBidderId();
                finalStatus = AuctionStatus.CLOSED;
                
                // Initiate payment
                serviceClient.initiatePayment(
                    auctionId, 
                    winnerId, 
                    highestBid.getBidAmount().doubleValue()
                );
            } else {
                // Reserve price not met
                finalStatus = AuctionStatus.NO_SALE;
            }
        } else {
            // No bids
            finalStatus = AuctionStatus.NO_SALE;
        }
        
        // 4. Close auction in database
        auctionDAO.closeAuction(auctionId, winnerId, finalStatus);
        
        // 5. Get updated auction and return
        return getAuction(auctionId);
    }
    
    @Override
    public int closeExpiredAuctions() {
        List<Auction> expiredAuctions = auctionDAO.findExpiredActiveAuctions();
        
        for (Auction auction : expiredAuctions) {
            try {
                closeAuction(auction.getAuctionId());
            } catch (Exception e) {
                System.err.println("Error closing auction " + auction.getAuctionId() + ": " + e.getMessage());
            }
        }
        
        return expiredAuctions.size();
    }
    
    /**
     * Convert Auction entity to AuctionResponse DTO
     */
    private AuctionResponse convertToResponse(Auction auction) {
        AuctionResponse response = new AuctionResponse();
        response.setAuctionId(auction.getAuctionId());
        response.setItemId(auction.getItemId());
        response.setSellerId(auction.getSellerId());
        response.setStartTime(auction.getStartTime());
        response.setEndTime(auction.getEndTime());
        response.setStatus(auction.getStatus());
        response.setReservePrice(auction.getReservePrice());
        response.setCurrentHighestBid(auction.getCurrentHighestBid());
        response.setCurrentHighestBidderId(auction.getCurrentHighestBidderId());
        response.setWinnerId(auction.getWinnerId());
        response.setTimeRemainingSeconds(auction.getTimeRemainingSeconds());
        return response;
    }
}

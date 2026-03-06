package com.code2cash.auction.service;

import com.code2cash.auction.dao.AuctionDAO;
import com.code2cash.auction.dao.BidDAO;
import com.code2cash.auction.dto.BidResponse;
import com.code2cash.auction.dto.PlaceBidRequest;
import com.code2cash.auction.exception.AuctionClosedException;
import com.code2cash.auction.exception.AuctionNotFoundException;
import com.code2cash.auction.exception.InvalidBidException;
import com.code2cash.auction.model.Auction;
import com.code2cash.auction.model.Bid;
import com.code2cash.auction.util.ServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of BidService
 * Contains all business logic for bid operations
 */
@Service
public class BidServiceImpl implements BidService {
    
    @Autowired
    private BidDAO bidDAO;
    
    @Autowired
    private AuctionDAO auctionDAO;
    
    @Autowired
    private ServiceClient serviceClient;
    
    @Override
    @Transactional
    public BidResponse placeBid(String auctionId, PlaceBidRequest request, String token) {
        // 1. Validate token and get user info
        ServiceClient.ValidationResponse validation = serviceClient.validateToken(token);
        if (!validation.isValid()) {
            throw new RuntimeException("Invalid authentication token");
        }
        
        // 2. Verify user has BUYER role
        if (!serviceClient.authorizeRole(token, "BUYER")) {
            throw new RuntimeException("User does not have BUYER permissions");
        }
        
        // 3. Get auction
        Optional<Auction> auctionOpt = auctionDAO.findById(auctionId);
        if (auctionOpt.isEmpty()) {
            throw new AuctionNotFoundException("Auction not found: " + auctionId);
        }
        
        Auction auction = auctionOpt.get();
        
        // 4. Validate auction is active
        if (!auction.isActive()) {
            throw new AuctionClosedException("Auction " + auctionId + " is not active");
        }
        
        // 5. Validate auction hasn't expired
        if (auction.hasEnded()) {
            throw new AuctionClosedException("Auction " + auctionId + " has ended");
        }
        
        // 6. Validate bid amount is higher than current highest
        BigDecimal currentHighest = auction.getCurrentHighestBid();
        if (request.getBidAmount().compareTo(currentHighest) <= 0) {
            throw new InvalidBidException(
                String.format("Bid amount $%.2f must be higher than current highest bid $%.2f",
                    request.getBidAmount(), currentHighest)
            );
        }
        
        // 7. Create bid
        Bid bid = new Bid();
        bid.setAuctionId(auctionId);
        bid.setBidderId(request.getBidderId());
        bid.setBidAmount(request.getBidAmount());
        
        // 8. Save bid to database
        Bid createdBid = bidDAO.createBid(bid);
        
        // 9. Update auction's highest bid
        boolean updated = auctionDAO.updateHighestBid(
            auctionId, 
            request.getBidAmount(), 
            request.getBidderId()
        );
        
        if (!updated) {
            throw new RuntimeException("Failed to update auction highest bid");
        }
        
        // 10. Create success response
        BidResponse response = new BidResponse(true, "Bid placed successfully");
        response.setBidId(createdBid.getBidId());
        response.setAuctionId(createdBid.getAuctionId());
        response.setBidderId(createdBid.getBidderId());
        response.setBidAmount(createdBid.getBidAmount());
        response.setBidTimestamp(createdBid.getBidTimestamp());
        
        return response;
    }
    
    @Override
    public List<Bid> getBidHistory(String auctionId) {
        // Verify auction exists
        Optional<Auction> auctionOpt = auctionDAO.findById(auctionId);
        if (auctionOpt.isEmpty()) {
            throw new AuctionNotFoundException("Auction not found: " + auctionId);
        }
        
        return bidDAO.findByAuctionId(auctionId);
    }
    
    @Override
    public Bid getHighestBid(String auctionId) {
        // Verify auction exists
        Optional<Auction> auctionOpt = auctionDAO.findById(auctionId);
        if (auctionOpt.isEmpty()) {
            throw new AuctionNotFoundException("Auction not found: " + auctionId);
        }
        
        Optional<Bid> highestBid = bidDAO.findHighestBidByAuctionId(auctionId);
        return highestBid.orElse(null);
    }
    
    @Override
    public List<Bid> getBidsByBidder(String bidderId) {
        return bidDAO.findByBidderId(bidderId);
    }
    
    @Override
    public int getBidCount(String auctionId) {
        // Verify auction exists
        Optional<Auction> auctionOpt = auctionDAO.findById(auctionId);
        if (auctionOpt.isEmpty()) {
            throw new AuctionNotFoundException("Auction not found: " + auctionId);
        }
        
        return bidDAO.getBidCount(auctionId);
    }
}

package com.code2cash.auction.service;

import com.code2cash.auction.dto.BidResponse;
import com.code2cash.auction.dto.PlaceBidRequest;
import com.code2cash.auction.model.Bid;
import java.util.List;

/**
 * Service interface for Bid business logic
 */
public interface BidService {
    
    /**
     * Place a bid on an auction
     * Validates:
     * - User authentication
     * - Auction exists and is active
     * - Bid amount is higher than current highest
     * 
     * @param auctionId The auction to bid on
     * @param request The bid request
     * @param token User authentication token
     * @return Bid response with success/failure
     */
    BidResponse placeBid(String auctionId, PlaceBidRequest request, String token);
    
    /**
     * Get all bids for an auction
     * 
     * @param auctionId The auction ID
     * @return List of bids ordered by timestamp (newest first)
     */
    List<Bid> getBidHistory(String auctionId);
    
    /**
     * Get the current highest bid for an auction
     * 
     * @param auctionId The auction ID
     * @return The highest bid, or null if no bids
     */
    Bid getHighestBid(String auctionId);
    
    /**
     * Get all bids placed by a specific bidder
     * 
     * @param bidderId The bidder's user ID
     * @return List of bids by this bidder
     */
    List<Bid> getBidsByBidder(String bidderId);
    
    /**
     * Get bid count for an auction
     * 
     * @param auctionId The auction ID
     * @return Number of bids
     */
    int getBidCount(String auctionId);
}

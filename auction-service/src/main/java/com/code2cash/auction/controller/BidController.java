package com.code2cash.auction.controller;

import com.code2cash.auction.dto.BidResponse;
import com.code2cash.auction.dto.PlaceBidRequest;
import com.code2cash.auction.model.Bid;
import com.code2cash.auction.service.BidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;

/**
 * REST Controller for Bid endpoints
 * Handles HTTP requests for bidding operations
 */
@RestController
@RequestMapping("/api/auctions")
@CrossOrigin(origins = "*")
public class BidController {
    
    @Autowired
    private BidService bidService;
    
    /**
     * Place a bid on an auction
     * POST /api/auctions/{auctionId}/bids
     */
    @PostMapping("/{auctionId}/bids")
    public ResponseEntity<BidResponse> placeBid(
            @PathVariable("auctionId") String auctionId,
            @Valid @RequestBody PlaceBidRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        // Extract token
        String token = extractToken(authHeader);
        
        BidResponse response = bidService.placeBid(auctionId, request, token);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    /**
     * Get bid history for an auction
     * GET /api/auctions/{auctionId}/bids
     */
    @GetMapping("/{auctionId}/bids")
    public ResponseEntity<List<Bid>> getBidHistory(@PathVariable("auctionId") String auctionId) {
        List<Bid> bids = bidService.getBidHistory(auctionId);
        return ResponseEntity.ok(bids);
    }
    
    /**
     * Get highest bid for an auction
     * GET /api/auctions/{auctionId}/highest-bid
     */
    @GetMapping("/{auctionId}/highest-bid")
    public ResponseEntity<Bid> getHighestBid(@PathVariable("auctionId") String auctionId) {
        Bid highestBid = bidService.getHighestBid(auctionId);
        
        if (highestBid == null) {
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.ok(highestBid);
    }
    
    /**
     * Get bid count for an auction
     * GET /api/auctions/{auctionId}/bid-count
     */
    @GetMapping("/{auctionId}/bid-count")
    public ResponseEntity<Map<String, Integer>> getBidCount(
            @PathVariable("auctionId") String auctionId) {
        int count = bidService.getBidCount(auctionId);
        
        Map<String, Integer> response = new HashMap<>();
        response.put("auctionId", auctionId.hashCode());
        response.put("bidCount", count);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all bids by a specific bidder
     * GET /api/bidders/{bidderId}/bids
     */
    @GetMapping("/bidders/{bidderId}/bids")
    public ResponseEntity<List<Bid>> getBidsByBidder(@PathVariable("bidderId") String bidderId) {
        List<Bid> bids = bidService.getBidsByBidder(bidderId);
        return ResponseEntity.ok(bids);
    }
    
    /**
     * Helper method to extract token from Authorization header
     */
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return authHeader;
    }
}

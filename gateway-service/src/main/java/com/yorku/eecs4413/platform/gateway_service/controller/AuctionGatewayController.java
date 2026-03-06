package com.yorku.eecs4413.platform.gateway_service.controller;

import com.yorku.eecs4413.platform.gateway_service.client.AuctionClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Gateway Controller for Auction Service
 * Routes requests from frontend to Auction Service (port 8082)
 * @author Syed Mustafa Jamal - Auction Service 
 */
@RestController
@RequestMapping("/api/auctions")
public class AuctionGatewayController {

    private final AuctionClient auctionClient;

    public AuctionGatewayController(AuctionClient auctionClient) {
        this.auctionClient = auctionClient;
    }

    // AUCTION ENDPOINTS

    /**
     * Create a new auction
     * POST /api/auctions
     * Requires: SELLER role
     */
    @PostMapping
    public ResponseEntity<String> createAuction(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody String body) {
        return auctionClient.postWithAuth("/api/auctions", authHeader, body);
    }

    /**
     * Get all active auctions
     * GET /api/auctions/active
     * Public endpoint
     */
    @GetMapping("/active")
    public ResponseEntity<String> getActiveAuctions() {
        return auctionClient.get("/api/auctions/active");
    }

    /**
     * Get auction by ID
     * GET /api/auctions/{id}
     * Public endpoint
     */
    @GetMapping("/{auctionId}")
    public ResponseEntity<String> getAuction(@PathVariable String auctionId) {
        return auctionClient.get("/api/auctions/" + auctionId);
    }

    /**
     * Get auctions by seller
     * GET /api/auctions/seller/{sellerId}
     * Public endpoint
     */
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<String> getSellerAuctions(@PathVariable String sellerId) {
        return auctionClient.get("/api/auctions/seller/" + sellerId);
    }

    /**
     * Close an auction
     * PUT /api/auctions/{id}/close
     * Requires: ADMIN role
     */
    @PutMapping("/{auctionId}/close")
    public ResponseEntity<String> closeAuction(
            @PathVariable String auctionId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        return auctionClient.putWithAuth("/api/auctions/" + auctionId + "/close", authHeader);
    }

    /**
     * Manually close all expired auctions
     * POST /api/auctions/close-expired
     * Requires: ADMIN role
     */
    @PostMapping("/close-expired")
    public ResponseEntity<String> closeExpiredAuctions(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        return auctionClient.postWithAuth("/api/auctions/close-expired", authHeader, "");
    }

    // BID ENDPOINTS

    /**
     * Place a bid on an auction
     * POST /api/auctions/{auctionId}/bids
     * Requires: BUYER role
     */
    @PostMapping("/{auctionId}/bids")
    public ResponseEntity<String> placeBid(
            @PathVariable String auctionId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody String body) {
        return auctionClient.postWithAuth("/api/auctions/" + auctionId + "/bids", authHeader, body);
    }

    /**
     * Get bid history for an auction
     * GET /api/auctions/{auctionId}/bids
     * Public endpoint
     */
    @GetMapping("/{auctionId}/bids")
    public ResponseEntity<String> getBidHistory(@PathVariable String auctionId) {
        return auctionClient.get("/api/auctions/" + auctionId + "/bids");
    }

    /**
     * Get highest bid for an auction
     * GET /api/auctions/{auctionId}/highest-bid
     * Public endpoint
     */
    @GetMapping("/{auctionId}/highest-bid")
    public ResponseEntity<String> getHighestBid(@PathVariable String auctionId) {
        return auctionClient.get("/api/auctions/" + auctionId + "/highest-bid");
    }

    /**
     * Get bid count for an auction
     * GET /api/auctions/{auctionId}/bid-count
     * Public endpoint
     */
    @GetMapping("/{auctionId}/bid-count")
    public ResponseEntity<String> getBidCount(@PathVariable String auctionId) {
        return auctionClient.get("/api/auctions/" + auctionId + "/bid-count");
    }

    /**
     * Get all bids by a specific bidder
     * GET /api/auctions/bidders/{bidderId}/bids
     * Public endpoint
     */
    @GetMapping("/bidders/{bidderId}/bids")
    public ResponseEntity<String> getBidsByBidder(@PathVariable String bidderId) {
        return auctionClient.get("/api/auctions/bidders/" + bidderId + "/bids");
    }
}
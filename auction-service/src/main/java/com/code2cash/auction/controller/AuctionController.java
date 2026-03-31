package com.code2cash.auction.controller;

import com.code2cash.auction.dto.AuctionResponse;
import com.code2cash.auction.dto.CreateAuctionRequest;
import com.code2cash.auction.service.AuctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * REST Controller for Auction endpoints
 * Handles HTTP requests for auction operations
 */
@RestController
@RequestMapping("/api/auctions")
@CrossOrigin(origins = "*") // Allow CORS for frontend
public class AuctionController {
    
    @Autowired
    private AuctionService auctionService;
    
    /**
     * Create a new auction
     * POST /api/auctions
     */
    @PostMapping
    public ResponseEntity<AuctionResponse> createAuction(
            @Valid @RequestBody CreateAuctionRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        // Extract token from "Bearer <token>" format
        String token = extractToken(authHeader);
        
        AuctionResponse response = auctionService.createAuction(request, token);
        addAuctionLinks(response);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    /**
     * Get auction by ID
     * GET /api/auctions/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<AuctionResponse> getAuction(@PathVariable("id") String auctionId) {
        AuctionResponse response = auctionService.getAuction(auctionId);
        addAuctionLinks(response);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all active auctions
     * GET /api/auctions/active
     */
    @GetMapping("/active")
    public ResponseEntity<List<AuctionResponse>> getActiveAuctions() {
        List<AuctionResponse> auctions = auctionService.getActiveAuctions();
        auctions.forEach(this::addAuctionLinks);
        return ResponseEntity.ok(auctions);
    }
    
    /**
     * Get auctions by seller ID
     * GET /api/auctions/seller/{sellerId}
     */
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<AuctionResponse>> getSellerAuctions(
            @PathVariable("sellerId") String sellerId) {
        List<AuctionResponse> auctions = auctionService.getSellerAuctions(sellerId);
        auctions.forEach(this::addAuctionLinks);
        return ResponseEntity.ok(auctions);
    }
    
    /**
     * Close an auction (admin/system only)
     * PUT /api/auctions/{id}/close
     */
    @PutMapping("/{id}/close")
    public ResponseEntity<AuctionResponse> closeAuction(@PathVariable("id") String auctionId) {
        AuctionResponse response = auctionService.closeAuction(auctionId);
        addAuctionLinks(response);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Manually trigger closure of expired auctions (admin/system only)
     * POST /api/auctions/close-expired
     */
    @PostMapping("/close-expired")
    public ResponseEntity<String> closeExpiredAuctions() {
        int count = auctionService.closeExpiredAuctions();
        return ResponseEntity.ok("Closed " + count + " expired auctions");
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

    private void addAuctionLinks(AuctionResponse response) {
        if (response == null || response.getAuctionId() == null) {
            return;
        }
        String auctionId = response.getAuctionId();
        response.add(
                linkTo(methodOn(AuctionController.class).getAuction(auctionId)).withSelfRel(),
                linkTo(methodOn(BidController.class).getBidHistory(auctionId)).withRel("bids"),
                linkTo(methodOn(BidController.class).getHighestBid(auctionId)).withRel("highestBid"),
                linkTo(methodOn(BidController.class).getBidCount(auctionId)).withRel("bidCount")
        );
    }
}

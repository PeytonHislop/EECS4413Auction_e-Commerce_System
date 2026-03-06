package com.code2cash.auction.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class for generating unique IDs for auctions and bids
 */
public class IdGenerator {
    
    private static final AtomicInteger auctionCounter = new AtomicInteger(0);
    private static final AtomicInteger bidCounter = new AtomicInteger(0);
    
    /**
     * Generate a unique auction ID
     * Format: AUC-YYYYMMDD-HHMMSS-XXX
     * Example: AUC-20260216-143022-001
     */
    public static String generateAuctionId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        int count = auctionCounter.incrementAndGet();
        return String.format("AUC-%s-%03d", timestamp, count % 1000);
    }
    
    /**
     * Generate a unique bid ID
     * Format: BID-YYYYMMDD-HHMMSS-XXX
     * Example: BID-20260216-143022-001
     */
    public static String generateBidId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        int count = bidCounter.incrementAndGet();
        return String.format("BID-%s-%03d", timestamp, count % 1000);
    }
    
    /**
     * Generate a simple UUID-based ID (alternative method)
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }
}

package com.code2cash.auction.scheduler;

import com.code2cash.auction.service.AuctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Scheduler for automatic auction closure
 * Runs periodically to close expired auctions
 */
@Component
public class AuctionScheduler {
    
    @Autowired
    private AuctionService auctionService;
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Check for expired auctions every 60 seconds
     * cron format: second minute hour day month weekday
     * fixedRate: Run every 60000 milliseconds (60 seconds)
     */
    @Scheduled(fixedRate = 60000) // Every 60 seconds
    public void checkAndCloseExpiredAuctions() {
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.println("[" + timestamp + "] Scheduler: Checking for expired auctions...");
        
        try {
            int closedCount = auctionService.closeExpiredAuctions();
            
            if (closedCount > 0) {
                System.out.println("[" + timestamp + "] Scheduler: Closed " + closedCount + " expired auctions");
            } else {
                System.out.println("[" + timestamp + "] Scheduler: No expired auctions found");
            }
            
        } catch (Exception e) {
            System.err.println("[" + timestamp + "] Scheduler: Error closing expired auctions: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Alternative scheduler using cron expression
     * Runs at the start of every minute
     * Uncomment to use instead of fixedRate
     */
    // @Scheduled(cron = "0 * * * * *") // Every minute at :00 seconds
    // public void checkExpiredAuctionsCron() {
    //     checkAndCloseExpiredAuctions();
    // }
}

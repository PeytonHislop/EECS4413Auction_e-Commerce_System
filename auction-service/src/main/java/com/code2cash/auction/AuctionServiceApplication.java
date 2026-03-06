package com.code2cash.auction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot Application for Auction Service
 * 
 * @author Syed Mustafa Jamal
 * @version 1.0.0
 * @since 2026-02-16
 */
@SpringBootApplication
@EnableScheduling  // Enable scheduled tasks for auction closure
public class AuctionServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(AuctionServiceApplication.class, args);
        System.out.println("\n" +
                "    Auction Service Started Successfully!\n" +
                "    Port: 8082\n" +
                "    Database: SQLite (auction-service.db)\n");
    }
}

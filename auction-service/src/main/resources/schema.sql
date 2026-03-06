-- ========================================
-- Auction Service Database Schema
-- Code2Cash E-Commerce System
-- ========================================

-- Auctions Table
-- Stores all auction information
CREATE TABLE IF NOT EXISTS auctions (
    auction_id VARCHAR(50) PRIMARY KEY,
    item_id VARCHAR(50) NOT NULL,
    seller_id VARCHAR(50) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'CLOSED', 'NO_SALE', 'CANCELLED')),
    reserve_price DECIMAL(10, 2),
    current_highest_bid DECIMAL(10, 2) DEFAULT 0.00,
    current_highest_bidder_id VARCHAR(50),
    winner_id VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bids Table
-- Stores all bids placed on auctions
CREATE TABLE IF NOT EXISTS bids (
    bid_id VARCHAR(50) PRIMARY KEY,
    auction_id VARCHAR(50) NOT NULL,
    bidder_id VARCHAR(50) NOT NULL,
    bid_amount DECIMAL(10, 2) NOT NULL,
    bid_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (auction_id) REFERENCES auctions(auction_id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_auction_status ON auctions(status);
CREATE INDEX IF NOT EXISTS idx_auction_end_time ON auctions(end_time);
CREATE INDEX IF NOT EXISTS idx_auction_seller ON auctions(seller_id);
CREATE INDEX IF NOT EXISTS idx_bid_auction ON bids(auction_id);
CREATE INDEX IF NOT EXISTS idx_bid_bidder ON bids(bidder_id);
CREATE INDEX IF NOT EXISTS idx_bid_timestamp ON bids(bid_timestamp);

-- Insert sample data for testing
INSERT INTO auctions (auction_id, item_id, seller_id, start_time, end_time, status, reserve_price, current_highest_bid, current_highest_bidder_id)
VALUES 
    ('AUC001', 'ITEM001', 'USER001', datetime('now'), datetime('now', '+2 days'), 'ACTIVE', 100.00, 50.00, NULL),
    ('AUC002', 'ITEM002', 'USER002', datetime('now'), datetime('now', '+1 day'), 'ACTIVE', 200.00, 150.00, 'USER003'),
    ('AUC003', 'ITEM003', 'USER001', datetime('now', '-3 days'), datetime('now', '-1 day'), 'CLOSED', 75.00, 120.00, 'USER004');

INSERT INTO bids (bid_id, auction_id, bidder_id, bid_amount, bid_timestamp)
VALUES
    ('BID001', 'AUC001', 'USER003', 50.00, datetime('now', '-1 hour')),
    ('BID002', 'AUC002', 'USER003', 150.00, datetime('now', '-30 minutes')),
    ('BID003', 'AUC002', 'USER004', 120.00, datetime('now', '-2 hours')),
    ('BID004', 'AUC003', 'USER004', 120.00, datetime('now', '-1 day', '-1 hour')),
    ('BID005', 'AUC003', 'USER005', 100.00, datetime('now', '-1 day', '-3 hours'));

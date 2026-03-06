#!/bin/bash
# Comprehensive Test Cases for Auction Service
# Includes error cases, edge cases, and validation tests

echo "============================================="
echo "Auction Service - Comprehensive Test Cases"
echo "============================================="
echo ""

BASE_URL="http://localhost:8082"

# =====================================
# TEST SUITE 1: Auction Creation Tests
# =====================================
echo "===== TEST SUITE 1: Auction Creation ====="
echo ""

# TC-AUC-01: Success case
echo "[TC-AUC-01] Create auction - SUCCESS"
curl -X POST "${BASE_URL}/api/auctions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer valid-token" \
  -d '{
    "itemId": "ITEM200",
    "sellerId": "SELLER002",
    "durationHours": 24,
    "reservePrice": 150.00
  }'
echo -e "\n"

# TC-AUC-02: Missing required field
echo "[TC-AUC-02] Create auction - MISSING ITEM ID (should fail)"
curl -X POST "${BASE_URL}/api/auctions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer valid-token" \
  -d '{
    "sellerId": "SELLER002",
    "durationHours": 24,
    "reservePrice": 150.00
  }'
echo -e "\n"

# TC-AUC-03: Invalid duration (negative)
echo "[TC-AUC-03] Create auction - NEGATIVE DURATION (should fail)"
curl -X POST "${BASE_URL}/api/auctions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer valid-token" \
  -d '{
    "itemId": "ITEM201",
    "sellerId": "SELLER002",
    "durationHours": -10,
    "reservePrice": 150.00
  }'
echo -e "\n"

# TC-AUC-04: Invalid reserve price (negative)
echo "[TC-AUC-04] Create auction - NEGATIVE RESERVE PRICE (should fail)"
curl -X POST "${BASE_URL}/api/auctions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer valid-token" \
  -d '{
    "itemId": "ITEM202",
    "sellerId": "SELLER002",
    "durationHours": 24,
    "reservePrice": -50.00
  }'
echo -e "\n"

# =====================================
# TEST SUITE 2: Bid Placement Tests
# =====================================
echo "===== TEST SUITE 2: Bid Placement ====="
echo ""

AUCTION_ID="AUC002"  # Use sample auction from database

# TC-BID-01: Successful bid
echo "[TC-BID-01] Place bid - SUCCESS (higher than current)"
curl -X POST "${BASE_URL}/api/auctions/${AUCTION_ID}/bids" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer bidder-token" \
  -d '{
    "bidderId": "BIDDER200",
    "bidAmount": 175.00
  }'
echo -e "\n"

# TC-BID-02: Bid too low
echo "[TC-BID-02] Place bid - BID TOO LOW (should fail)"
curl -X POST "${BASE_URL}/api/auctions/${AUCTION_ID}/bids" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer bidder-token" \
  -d '{
    "bidderId": "BIDDER201",
    "bidAmount": 100.00
  }'
echo -e "\n"

# TC-BID-03: Bid on non-existent auction
echo "[TC-BID-03] Place bid - AUCTION NOT FOUND (should fail)"
curl -X POST "${BASE_URL}/api/auctions/INVALID-AUCTION/bids" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer bidder-token" \
  -d '{
    "bidderId": "BIDDER202",
    "bidAmount": 200.00
  }'
echo -e "\n"

# TC-BID-04: Bid on closed auction
echo "[TC-BID-04] Place bid - AUCTION CLOSED (should fail)"
CLOSED_AUCTION="AUC003"  # This is a closed auction in sample data
curl -X POST "${BASE_URL}/api/auctions/${CLOSED_AUCTION}/bids" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer bidder-token" \
  -d '{
    "bidderId": "BIDDER203",
    "bidAmount": 300.00
  }'
echo -e "\n"

# TC-BID-05: Missing bid amount
echo "[TC-BID-05] Place bid - MISSING BID AMOUNT (should fail)"
curl -X POST "${BASE_URL}/api/auctions/${AUCTION_ID}/bids" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer bidder-token" \
  -d '{
    "bidderId": "BIDDER204"
  }'
echo -e "\n"

# TC-BID-06: Negative bid amount
echo "[TC-BID-06] Place bid - NEGATIVE AMOUNT (should fail)"
curl -X POST "${BASE_URL}/api/auctions/${AUCTION_ID}/bids" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer bidder-token" \
  -d '{
    "bidderId": "BIDDER205",
    "bidAmount": -50.00
  }'
echo -e "\n"

# =====================================
# TEST SUITE 3: Retrieval Tests
# =====================================
echo "===== TEST SUITE 3: Data Retrieval ====="
echo ""

# TC-RET-01: Get active auctions
echo "[TC-RET-01] Get active auctions"
curl -X GET "${BASE_URL}/api/auctions/active"
echo -e "\n"

# TC-RET-02: Get auction by ID (valid)
echo "[TC-RET-02] Get auction by ID - VALID"
curl -X GET "${BASE_URL}/api/auctions/AUC001"
echo -e "\n"

# TC-RET-03: Get auction by ID (invalid)
echo "[TC-RET-03] Get auction by ID - INVALID (should fail)"
curl -X GET "${BASE_URL}/api/auctions/INVALID-ID"
echo -e "\n"

# TC-RET-04: Get bid history
echo "[TC-RET-04] Get bid history for auction"
curl -X GET "${BASE_URL}/api/auctions/AUC002/bids"
echo -e "\n"

# TC-RET-05: Get highest bid (auction with bids)
echo "[TC-RET-05] Get highest bid - HAS BIDS"
curl -X GET "${BASE_URL}/api/auctions/AUC002/highest-bid"
echo -e "\n"

# TC-RET-06: Get highest bid (auction with no bids)
echo "[TC-RET-06] Get highest bid - NO BIDS (should return empty)"
curl -X GET "${BASE_URL}/api/auctions/AUC001/highest-bid"
echo -e "\n"

# TC-RET-07: Get seller's auctions
echo "[TC-RET-07] Get auctions by seller"
curl -X GET "${BASE_URL}/api/auctions/seller/USER001"
echo -e "\n"

# TC-RET-08: Get bid count
echo "[TC-RET-08] Get bid count for auction"
curl -X GET "${BASE_URL}/api/auctions/AUC002/bid-count"
echo -e "\n"

# =====================================
# TEST SUITE 4: Auction Closure Tests
# =====================================
echo "===== TEST SUITE 4: Auction Closure ====="
echo ""

# TC-CLOSE-01: Close auction with winner
echo "[TC-CLOSE-01] Close auction - WITH WINNER"
curl -X PUT "${BASE_URL}/api/auctions/AUC002/close"
echo -e "\n"

# TC-CLOSE-02: Close non-existent auction
echo "[TC-CLOSE-02] Close auction - NOT FOUND (should fail)"
curl -X PUT "${BASE_URL}/api/auctions/INVALID-ID/close"
echo -e "\n"

# TC-CLOSE-03: Manually close all expired auctions
echo "[TC-CLOSE-03] Close all expired auctions (manual trigger)"
curl -X POST "${BASE_URL}/api/auctions/close-expired"
echo -e "\n"

# =====================================
# TEST SUITE 5: Edge Cases
# =====================================
echo "===== TEST SUITE 5: Edge Cases ====="
echo ""

# TC-EDGE-01: Create auction with minimum duration (1 hour)
echo "[TC-EDGE-01] Create auction - MINIMUM DURATION (1 hour)"
curl -X POST "${BASE_URL}/api/auctions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer valid-token" \
  -d '{
    "itemId": "ITEM300",
    "sellerId": "SELLER003",
    "durationHours": 1,
    "reservePrice": 50.00
  }'
echo -e "\n"

# TC-EDGE-02: Create auction with large duration (720 hours = 30 days)
echo "[TC-EDGE-02] Create auction - LARGE DURATION (30 days)"
curl -X POST "${BASE_URL}/api/auctions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer valid-token" \
  -d '{
    "itemId": "ITEM301",
    "sellerId": "SELLER003",
    "durationHours": 720,
    "reservePrice": 100.00
  }'
echo -e "\n"

# TC-EDGE-03: Bid exactly equal to current highest (should fail)
echo "[TC-EDGE-03] Place bid - EQUAL TO CURRENT (should fail)"
curl -X POST "${BASE_URL}/api/auctions/AUC001/bids" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer bidder-token" \
  -d '{
    "bidderId": "BIDDER300",
    "bidAmount": 50.00
  }'
echo -e "\n"

# TC-EDGE-04: Multiple rapid bids (concurrency test)
echo "[TC-EDGE-04] Multiple rapid bids"
curl -X POST "${BASE_URL}/api/auctions/AUC001/bids" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer bidder-token" \
  -d '{"bidderId": "BIDDER301", "bidAmount": 60.00}' &
curl -X POST "${BASE_URL}/api/auctions/AUC001/bids" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer bidder-token" \
  -d '{"bidderId": "BIDDER302", "bidAmount": 65.00}' &
wait
echo -e "\n"

echo "============================================="
echo "Comprehensive Tests Complete!"
echo "============================================="
echo ""
echo "Summary:"
echo "  - Auction Creation: 4 tests"
echo "  - Bid Placement: 6 tests"
echo "  - Data Retrieval: 8 tests"
echo "  - Auction Closure: 3 tests"
echo "  - Edge Cases: 4 tests"
echo "  TOTAL: 25 test cases"

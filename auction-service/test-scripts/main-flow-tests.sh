#!/bin/bash
# Main Flow Test Cases for Auction Service
# Tests the core use cases from Deliverable 1

echo "==================================="
echo "Auction Service - Main Flow Tests"
echo "==================================="
echo ""

BASE_URL="http://localhost:8082"

# UC4-AS: View Active Auctions
echo "Test 1: Get all active auctions (UC4-AS)"
curl -X GET "${BASE_URL}/api/auctions/active"
echo -e "\n"

# UC1-AS: Create Auction Listing
echo "Test 2: Create new auction (UC1-AS)"
curl -X POST "${BASE_URL}/api/auctions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer seller-token-123" \
  -d '{
    "itemId": "ITEM100",
    "sellerId": "SELLER001",
    "durationHours": 48,
    "reservePrice": 200.00
  }'
echo -e "\n"

# Get specific auction details
echo "Test 3: Get auction details"
AUCTION_ID="AUC001"  # Use sample auction from database
curl -X GET "${BASE_URL}/api/auctions/${AUCTION_ID}"
echo -e "\n"

# UC2-AS: Place Bid on Auction
echo "Test 4: Place bid on auction (UC2-AS)"
curl -X POST "${BASE_URL}/api/auctions/${AUCTION_ID}/bids" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer bidder-token-456" \
  -d '{
    "bidderId": "BIDDER100",
    "bidAmount": 75.00
  }'
echo -e "\n"

# Get bid history
echo "Test 5: Get bid history for auction"
curl -X GET "${BASE_URL}/api/auctions/${AUCTION_ID}/bids"
echo -e "\n"

# Get highest bid
echo "Test 6: Get current highest bid"
curl -X GET "${BASE_URL}/api/auctions/${AUCTION_ID}/highest-bid"
echo -e "\n"

# Get bid count
echo "Test 7: Get bid count"
curl -X GET "${BASE_URL}/api/auctions/${AUCTION_ID}/bid-count"
echo -e "\n"

# UC3-AS: Close Auction (manual trigger for testing)
echo "Test 8: Manually close auction (UC3-AS)"
curl -X PUT "${BASE_URL}/api/auctions/${AUCTION_ID}/close"
echo -e "\n"

# Verify auction is closed
echo "Test 9: Verify auction status changed"
curl -X GET "${BASE_URL}/api/auctions/${AUCTION_ID}"
echo -e "\n"

echo "==================================="
echo "Main Flow Tests Complete!"
echo "==================================="

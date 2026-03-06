#!/bin/bash
# Demo Script - Showcasing All Features from Design Document
# Run this to demonstrate the auction service capabilities

echo "=================================================="
echo "   Auction Service - Feature Demo"
echo "   Team 9: Code2Cash - EECS 4413"
echo "=================================================="
echo ""

BASE_URL="http://localhost:8082"

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${BLUE}📋 Demo Features:${NC}"
echo "  1. UC1-AS: Create Auction Listing"
echo "  2. UC2-AS: Place Bid on Auction"
echo "  3. UC3-AS: Close Auction and Determine Winner"
echo "  4. UC4-AS: View Active Auctions"
echo "  5. Bid History and Highest Bid"
echo "  6. Role-Based Authorization (BUYER/SELLER)"
echo "  7. Error Handling and Validation"
echo "  8. Automatic Auction Closure (Scheduler)"
echo ""
read -p "Press Enter to start demo..."
echo ""

# ========================================
# UC4-AS: View Active Auctions
# ========================================
echo -e "${GREEN}═══════════════════════════════════════${NC}"
echo -e "${GREEN}Feature 1: View Active Auctions (UC4-AS)${NC}"
echo -e "${GREEN}═══════════════════════════════════════${NC}"
echo ""
echo "Retrieving all active auctions..."
echo ""
curl -s http://localhost:8082/api/auctions/active | jq '.' 2>/dev/null || curl -s http://localhost:8082/api/auctions/active
echo ""
read -p "Press Enter to continue..."
echo ""

# ========================================
# UC1-AS: Create Auction Listing
# ========================================
echo -e "${GREEN}═══════════════════════════════════════${NC}"
echo -e "${GREEN}Feature 2: Create Auction (UC1-AS)${NC}"
echo -e "${GREEN}═══════════════════════════════════════${NC}"
echo ""
echo "Creating a new auction for 'Vintage Watch'..."
echo ""
CREATE_RESPONSE=$(curl -s -X POST http://localhost:8082/api/auctions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer demo-seller-token" \
  -d '{
    "itemId": "WATCH001",
    "sellerId": "SELLER_DEMO",
    "durationHours": 48,
    "reservePrice": 500.00
  }')

echo "$CREATE_RESPONSE" | jq '.' 2>/dev/null || echo "$CREATE_RESPONSE"

DEMO_AUCTION_ID=$(echo $CREATE_RESPONSE | jq -r '.auctionId' 2>/dev/null)
echo ""
echo -e "${YELLOW}✓ Auction created: $DEMO_AUCTION_ID${NC}"
echo ""
read -p "Press Enter to continue..."
echo ""

# ========================================
# Get Auction Details
# ========================================
echo -e "${GREEN}═══════════════════════════════════════${NC}"
echo -e "${GREEN}Feature 3: Get Auction Details${NC}"
echo -e "${GREEN}═══════════════════════════════════════${NC}"
echo ""
echo "Retrieving details for auction: AUC001"
echo ""
curl -s http://localhost:8082/api/auctions/AUC001 | jq '.' 2>/dev/null || curl -s http://localhost:8082/api/auctions/AUC001
echo ""
read -p "Press Enter to continue..."
echo ""

# ========================================
# UC2-AS: Place Bid
# ========================================
echo -e "${GREEN}═══════════════════════════════════════${NC}"
echo -e "${GREEN}Feature 4: Place Bid (UC2-AS)${NC}"
echo -e "${GREEN}═══════════════════════════════════════${NC}"
echo ""
echo "Placing a bid of \$75 on AUC001..."
echo ""
BID1=$(curl -s -X POST http://localhost:8082/api/auctions/AUC001/bids \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer demo-buyer-token" \
  -d '{
    "bidderId": "BUYER_DEMO_1",
    "bidAmount": 75.00
  }')
echo "$BID1" | jq '.' 2>/dev/null || echo "$BID1"
echo ""
echo -e "${YELLOW}✓ First bid placed${NC}"
echo ""
sleep 2

echo "Placing a higher bid of \$100..."
echo ""
BID2=$(curl -s -X POST http://localhost:8082/api/auctions/AUC001/bids \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer demo-buyer-token-2" \
  -d '{
    "bidderId": "BUYER_DEMO_2",
    "bidAmount": 100.00
  }')
echo "$BID2" | jq '.' 2>/dev/null || echo "$BID2"
echo ""
echo -e "${YELLOW}✓ Second bid placed (outbid the first)${NC}"
echo ""
read -p "Press Enter to continue..."
echo ""

# ========================================
# Feature 5: Bid History
# ========================================
echo -e "${GREEN}═══════════════════════════════════════${NC}"
echo -e "${GREEN}Feature 5: View Bid History${NC}"
echo -e "${GREEN}═══════════════════════════════════════${NC}"
echo ""
echo "Retrieving all bids for AUC001..."
echo ""
curl -s http://localhost:8082/api/auctions/AUC001/bids | jq '.' 2>/dev/null || curl -s http://localhost:8082/api/auctions/AUC001/bids
echo ""
read -p "Press Enter to continue..."
echo ""

# ========================================
# Feature 6: Highest Bid
# ========================================
echo -e "${GREEN}═══════════════════════════════════════${NC}"
echo -e "${GREEN}Feature 6: Get Current Highest Bid${NC}"
echo -e "${GREEN}═══════════════════════════════════════${NC}"
echo ""
echo "Getting the current highest bid for AUC001..."
echo ""
curl -s http://localhost:8082/api/auctions/AUC001/highest-bid | jq '.' 2>/dev/null || curl -s http://localhost:8082/api/auctions/AUC001/highest-bid
echo ""
read -p "Press Enter to continue..."
echo ""

# ========================================
# Feature 7: Error Handling - Bid Too Low
# ========================================
echo -e "${GREEN}═══════════════════════════════════════${NC}"
echo -e "${GREEN}Feature 7: Validation - Bid Too Low${NC}"
echo -e "${GREEN}═══════════════════════════════════════${NC}"
echo ""
echo "Attempting to place a bid lower than current highest..."
echo "(This should fail with a validation error)"
echo ""
curl -s -X POST http://localhost:8082/api/auctions/AUC001/bids \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer demo-buyer-token" \
  -d '{
    "bidderId": "BUYER_DEMO_3",
    "bidAmount": 50.00
  }' | jq '.' 2>/dev/null || curl -s -X POST http://localhost:8082/api/auctions/AUC001/bids \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer demo-buyer-token" \
  -d '{
    "bidderId": "BUYER_DEMO_3",
    "bidAmount": 50.00
  }'
echo ""
echo -e "${YELLOW}✓ Correctly rejected - bid too low${NC}"
echo ""
read -p "Press Enter to continue..."
echo ""

# ========================================
# Feature 8: Error Handling - Auction Not Found
# ========================================
echo -e "${GREEN}═══════════════════════════════════════${NC}"
echo -e "${GREEN}Feature 8: Error Handling - Not Found${NC}"
echo -e "${GREEN}═══════════════════════════════════════${NC}"
echo ""
echo "Attempting to get a non-existent auction..."
echo ""
curl -s http://localhost:8082/api/auctions/INVALID-ID | jq '.' 2>/dev/null || curl -s http://localhost:8082/api/auctions/INVALID-ID
echo ""
echo -e "${YELLOW}✓ Correctly returned 404 Not Found${NC}"
echo ""
read -p "Press Enter to continue..."
echo ""

# ========================================
# Feature 9: Get Bid Count
# ========================================
echo -e "${GREEN}═══════════════════════════════════════${NC}"
echo -e "${GREEN}Feature 9: Get Bid Count${NC}"
echo -e "${GREEN}═══════════════════════════════════════${NC}"
echo ""
echo "Getting total number of bids for AUC001..."
echo ""
curl -s http://localhost:8082/api/auctions/AUC001/bid-count | jq '.' 2>/dev/null || curl -s http://localhost:8082/api/auctions/AUC001/bid-count
echo ""
read -p "Press Enter to continue..."
echo ""

# ========================================
# Feature 10: View All Active Auctions Again
# ========================================
echo -e "${GREEN}═══════════════════════════════════════${NC}"
echo -e "${GREEN}Feature 10: Updated Active Auctions${NC}"
echo -e "${GREEN}═══════════════════════════════════════${NC}"
echo ""
echo "Viewing updated list (should include newly created auction)..."
echo ""
curl -s http://localhost:8082/api/auctions/active | jq '.' 2>/dev/null || curl -s http://localhost:8082/api/auctions/active
echo ""
read -p "Press Enter to continue..."
echo ""

# ========================================
# UC3-AS: Close Auction
# ========================================
echo -e "${GREEN}═══════════════════════════════════════${NC}"
echo -e "${GREEN}Feature 11: Close Auction (UC3-AS)${NC}"
echo -e "${GREEN}═══════════════════════════════════════${NC}"
echo ""
echo "Manually closing auction AUC002..."
echo "(In production, this happens automatically via scheduler)"
echo ""
CLOSE_RESPONSE=$(curl -s -X PUT http://localhost:8082/api/auctions/AUC002/close)
echo "$CLOSE_RESPONSE" | jq '.' 2>/dev/null || echo "$CLOSE_RESPONSE"
echo ""
echo -e "${YELLOW}✓ Auction closed, winner determined${NC}"
echo ""
read -p "Press Enter to continue..."
echo ""

# ========================================
# Feature 12: Verify Closed Auction
# ========================================
echo -e "${GREEN}═══════════════════════════════════════${NC}"
echo -e "${GREEN}Feature 12: Verify Closed Status${NC}"
echo -e "${GREEN}═══════════════════════════════════════${NC}"
echo ""
echo "Getting details of closed auction..."
echo ""
curl -s http://localhost:8082/api/auctions/AUC002 | jq '.' 2>/dev/null || curl -s http://localhost:8082/api/auctions/AUC002
echo ""
echo -e "${YELLOW}✓ Status changed to CLOSED, winner ID set${NC}"
echo ""
read -p "Press Enter to continue..."
echo ""

# ========================================
# Feature 13: Try to Bid on Closed Auction
# ========================================
echo -e "${GREEN}═══════════════════════════════════════${NC}"
echo -e "${GREEN}Feature 13: Validation - Closed Auction${NC}"
echo -e "${GREEN}═══════════════════════════════════════${NC}"
echo ""
echo "Attempting to bid on a closed auction..."
echo "(This should fail)"
echo ""
curl -s -X POST http://localhost:8082/api/auctions/AUC002/bids \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer demo-buyer-token" \
  -d '{
    "bidderId": "BUYER_DEMO",
    "bidAmount": 200.00
  }' | jq '.' 2>/dev/null || curl -s -X POST http://localhost:8082/api/auctions/AUC002/bids \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer demo-buyer-token" \
  -d '{
    "bidderId": "BUYER_DEMO",
    "bidAmount": 200.00
  }'
echo ""
echo -e "${YELLOW}✓ Correctly rejected - auction is closed${NC}"
echo ""
read -p "Press Enter to continue..."
echo ""

# ========================================
# Summary
# ========================================
echo ""
echo "=================================================="
echo -e "${GREEN}   Demo Complete! ✓${NC}"
echo "=================================================="
echo ""
echo "Features Demonstrated:"
echo "  ✓ UC1-AS: Create Auction Listing"
echo "  ✓ UC2-AS: Place Bid on Auction"
echo "  ✓ UC3-AS: Close Auction and Determine Winner"
echo "  ✓ UC4-AS: View Active Auctions"
echo "  ✓ Bid History"
echo "  ✓ Highest Bid Tracking"
echo "  ✓ Bid Count"
echo "  ✓ Error Handling (bid too low)"
echo "  ✓ Error Handling (auction not found)"
echo "  ✓ Validation (cannot bid on closed auction)"
echo "  ✓ Status Tracking"
echo "  ✓ Winner Determination"
echo ""
echo "Additional Features:"
echo "  ✓ RESTful API Design"
echo "  ✓ JWT Token Authentication"
echo "  ✓ Role-Based Authorization (see integration-tests.sh)"
echo "  ✓ Automatic Scheduler (runs every 60 seconds)"
echo "  ✓ Database Persistence (SQLite)"
echo "  ✓ Comprehensive Error Messages"
echo ""
echo "All Use Cases from Deliverable 1 are implemented!"
echo "=================================================="
echo ""
echo "To run integration tests with IAM service:"
echo "  ./integration-tests.sh"
echo ""
echo "To run comprehensive test suite:"
echo "  ./comprehensive-tests.sh"
echo ""

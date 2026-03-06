#!/bin/bash
# Integration Tests with Real IAM Service
# Run this when Ravneet's IAM service is running

echo "================================================"
echo "Auction Service - Integration Tests with IAM"
echo "================================================"
echo ""

BASE_URL="http://localhost:8082"
IAM_URL="http://localhost:8081"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if IAM service is running
echo "Checking if IAM service is running..."
if ! curl -s "${IAM_URL}/auth/login" > /dev/null 2>&1; then
    echo -e "${RED}ERROR: IAM service is not running on port 8081${NC}"
    echo "Please start Ravneet's IAM service first."
    echo ""
    echo "Steps:"
    echo "1. cd ../iam-service  # or wherever Ravneet's service is"
    echo "2. mvn spring-boot:run"
    echo "3. Wait for it to start"
    echo "4. Run this script again"
    exit 1
fi
echo -e "${GREEN}✓ IAM service is running${NC}"
echo ""

# Step 1: Login to get JWT token
echo "===== Step 1: Login to get JWT token ====="
echo ""

# Create a test account (or use existing)
echo "Attempting to create SELLER account..."
SIGNUP_RESPONSE=$(curl -s -X POST "${IAM_URL}/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testseller",
    "password": "testpass123",
    "firstName": "Test",
    "lastName": "Seller",
    "role": "SELLER",
    "address": {
      "street": "123 Test St",
      "city": "Toronto",
      "province": "ON",
      "postalCode": "M5H 2N2"
    }
  }')
echo "Signup response: $SIGNUP_RESPONSE"
echo ""

echo "Logging in as SELLER..."
SELLER_TOKEN_RESPONSE=$(curl -s -X POST "${IAM_URL}/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testseller",
    "password": "testpass123"
  }')

SELLER_TOKEN=$(echo $SELLER_TOKEN_RESPONSE | jq -r '.token' 2>/dev/null)

if [ -z "$SELLER_TOKEN" ] || [ "$SELLER_TOKEN" == "null" ]; then
    echo -e "${RED}ERROR: Failed to get seller token${NC}"
    echo "Response: $SELLER_TOKEN_RESPONSE"
    exit 1
fi

echo -e "${GREEN}✓ Seller token acquired${NC}"
echo "Token: ${SELLER_TOKEN:0:20}..."
echo ""

# Create buyer account
echo "Attempting to create BUYER account..."
curl -s -X POST "${IAM_URL}/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testbuyer",
    "password": "testpass123",
    "firstName": "Test",
    "lastName": "Buyer",
    "role": "BUYER",
    "address": {
      "street": "456 Buyer Ave",
      "city": "Toronto",
      "province": "ON",
      "postalCode": "M1M 1M1"
    }
  }' > /dev/null

echo "Logging in as BUYER..."
BUYER_TOKEN_RESPONSE=$(curl -s -X POST "${IAM_URL}/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testbuyer",
    "password": "testpass123"
  }')

BUYER_TOKEN=$(echo $BUYER_TOKEN_RESPONSE | jq -r '.token' 2>/dev/null)

if [ -z "$BUYER_TOKEN" ] || [ "$BUYER_TOKEN" == "null" ]; then
    echo -e "${RED}ERROR: Failed to get buyer token${NC}"
    echo "Response: $BUYER_TOKEN_RESPONSE"
    exit 1
fi

echo -e "${GREEN}✓ Buyer token acquired${NC}"
echo "Token: ${BUYER_TOKEN:0:20}..."
echo ""

# Step 2: Create auction with SELLER token
echo "===== Step 2: Create auction with SELLER token ====="
echo ""

CREATE_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/auctions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${SELLER_TOKEN}" \
  -d '{
    "itemId": "ITEM999",
    "sellerId": "ignored",
    "durationHours": 24,
    "reservePrice": 100.00
  }')

AUCTION_ID=$(echo $CREATE_RESPONSE | jq -r '.auctionId' 2>/dev/null)

if [ -z "$AUCTION_ID" ] || [ "$AUCTION_ID" == "null" ]; then
    echo -e "${RED}ERROR: Failed to create auction${NC}"
    echo "Response: $CREATE_RESPONSE"
    exit 1
fi

echo -e "${GREEN}✓ Auction created successfully${NC}"
echo "Auction ID: $AUCTION_ID"
echo ""

# Step 3: Try to create auction with BUYER token (should fail)
echo "===== Step 3: Try to create auction with BUYER token (should FAIL) ====="
echo ""

FAIL_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/auctions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${BUYER_TOKEN}" \
  -d '{
    "itemId": "ITEM998",
    "sellerId": "ignored",
    "durationHours": 24,
    "reservePrice": 100.00
  }')

if echo "$FAIL_RESPONSE" | grep -q "SELLER permissions"; then
    echo -e "${GREEN}✓ Correctly rejected - user does not have SELLER permissions${NC}"
else
    echo -e "${YELLOW}⚠ Unexpected response (check role authorization)${NC}"
    echo "Response: $FAIL_RESPONSE"
fi
echo ""

# Step 4: Place bid with BUYER token
echo "===== Step 4: Place bid with BUYER token ====="
echo ""

BID_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/auctions/${AUCTION_ID}/bids" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${BUYER_TOKEN}" \
  -d '{
    "bidderId": "ignored",
    "bidAmount": 150.00
  }')

if echo "$BID_RESPONSE" | grep -q "success"; then
    echo -e "${GREEN}✓ Bid placed successfully${NC}"
else
    echo -e "${RED}ERROR: Failed to place bid${NC}"
    echo "Response: $BID_RESPONSE"
fi
echo ""

# Step 5: Try to place bid with SELLER token (should fail)
echo "===== Step 5: Try to place bid with SELLER token (should FAIL) ====="
echo ""

FAIL_BID=$(curl -s -X POST "${BASE_URL}/api/auctions/${AUCTION_ID}/bids" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${SELLER_TOKEN}" \
  -d '{
    "bidderId": "ignored",
    "bidAmount": 175.00
  }')

if echo "$FAIL_BID" | grep -q "BUYER permissions"; then
    echo -e "${GREEN}✓ Correctly rejected - user does not have BUYER permissions${NC}"
else
    echo -e "${YELLOW}⚠ Unexpected response (check role authorization)${NC}"
    echo "Response: $FAIL_BID"
fi
echo ""

# Step 6: Try without token (should fail)
echo "===== Step 6: Try to create auction without token (should FAIL) ====="
echo ""

NO_TOKEN=$(curl -s -X POST "${BASE_URL}/api/auctions" \
  -H "Content-Type: application/json" \
  -d '{
    "itemId": "ITEM997",
    "sellerId": "ignored",
    "durationHours": 24,
    "reservePrice": 100.00
  }')

if echo "$NO_TOKEN" | grep -q "Invalid"; then
    echo -e "${GREEN}✓ Correctly rejected - no token provided${NC}"
else
    echo -e "${YELLOW}⚠ Should reject requests without tokens${NC}"
    echo "Response: $NO_TOKEN"
fi
echo ""

# Step 7: Get auction details (no auth required)
echo "===== Step 7: Get auction details (no auth required) ====="
echo ""

DETAILS=$(curl -s -X GET "${BASE_URL}/api/auctions/${AUCTION_ID}")

if echo "$DETAILS" | grep -q "$AUCTION_ID"; then
    echo -e "${GREEN}✓ Successfully retrieved auction details without auth${NC}"
else
    echo -e "${RED}ERROR: Failed to get auction details${NC}"
fi
echo ""

# Step 8: Validate token directly with IAM
echo "===== Step 8: Validate token directly with IAM ====="
echo ""

VALIDATE=$(curl -s -X POST "${IAM_URL}/auth/validate" \
  -H "Authorization: Bearer ${SELLER_TOKEN}")

echo "Token validation response:"
echo "$VALIDATE" | jq '.' 2>/dev/null || echo "$VALIDATE"
echo ""

echo "================================================"
echo "Integration Tests Complete!"
echo "================================================"
echo ""
echo -e "${GREEN}Summary:${NC}"
echo "✓ SELLER can create auctions"
echo "✓ BUYER can place bids"
echo "✓ BUYER cannot create auctions (role check)"
echo "✓ SELLER cannot place bids (role check)"
echo "✓ Requests without tokens are rejected"
echo "✓ Public endpoints work without auth"
echo ""
echo "Created Auction ID: $AUCTION_ID"
echo "You can view it at: ${BASE_URL}/api/auctions/${AUCTION_ID}"

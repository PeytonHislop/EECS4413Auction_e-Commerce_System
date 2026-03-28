#!/bin/bash

# Code2Cash Comprehensive API Test Suite
# Tests all major use cases and scenarios
# Usage: bash comprehensive-tests.sh

set -e

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# API Endpoints
GATEWAY_URL="http://localhost:8080"
IAM_URL="http://localhost:8081"
AUCTION_URL="http://localhost:8082"
CATALOGUE_URL="http://localhost:8083"
LEADERBOARD_URL="http://localhost:8085"

echo -e "${YELLOW}=== Code2Cash Comprehensive Test Suite ===${NC}\n"

# Test counters
TESTS_RUN=0
TESTS_PASSED=0
TESTS_FAILED=0

# Helper function
test_endpoint() {
    local test_name=$1
    local method=$2
    local endpoint=$3
    local data=$4
    local expected_code=$5
    local auth_header=${6:-""}

    TESTS_RUN=$((TESTS_RUN + 1))
    echo -ne "${YELLOW}Test $TESTS_RUN:${NC} $test_name... "

    local cmd="curl -s -w '\n%{http_code}' -X $method"
    
    if [ ! -z "$auth_header" ]; then
        cmd="$cmd -H 'Authorization: $auth_header'"
    fi
    
    cmd="$cmd -H 'Content-Type: application/json'"
    
    if [ ! -z "$data" ]; then
        cmd="$cmd -d '$data'"
    fi
    
    cmd="$cmd $endpoint"

    local response=$(eval "$cmd")
    local body=$(echo "$response" | head -n -1)
    local http_code=$(echo "$response" | tail -n 1)

    if [ "$http_code" == "$expected_code" ]; then
        echo -e "${GREEN}✓ PASSED${NC} (HTTP $http_code)"
        TESTS_PASSED=$((TESTS_PASSED + 1))
        echo "$body"
    else
        echo -e "${RED}✗ FAILED${NC} (Expected: $expected_code, Got: $http_code)"
        TESTS_FAILED=$((TESTS_FAILED + 1))
        echo "Response: $body"
    fi
    echo ""
    
    echo "$body"
}

echo -e "\n${YELLOW}=== 1. IAM SERVICE TESTS ===${NC}\n"

# Test 1.1: User Signup
echo "Testing user registration..."
SIGNUP_RESPONSE=$(curl -s -X POST "$IAM_URL/auth/signup" \
    -H "Content-Type: application/json" \
    -d '{
        "username": "testbuyer",
        "email": "buyer@test.com",
        "password": "password123",
        "role": "BUYER"
    }')
echo "Signup Response: $SIGNUP_RESPONSE"

# Test 1.2: User Login
echo -e "\nTesting user login..."
LOGIN_RESPONSE=$(curl -s -X POST "$IAM_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d '{
        "username": "testbuyer",
        "password": "password123"
    }')
echo "Login Response: $LOGIN_RESPONSE"

# Extract JWT token
JWT_TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*' | cut -d'"' -f4)
echo "Extracted JWT: ${JWT_TOKEN:0:20}..."

# Test 1.3: Token Validation
if [ ! -z "$JWT_TOKEN" ]; then
    echo -e "\nTesting token validation..."
    curl -s -X POST "$IAM_URL/auth/validate" \
        -H "Authorization: Bearer $JWT_TOKEN" \
        | jq '.' 2>/dev/null || echo "Token validation response received"
fi

echo -e "\n${YELLOW}=== 2. CATALOGUE SERVICE TESTS (UC7: Upload Item) ===${NC}\n"

# Test 2.1: Create item as BUYER (should fail)
echo "Test: Create item as BUYER (should fail with 403)..."
curl -s -w "\nHTTP Status: %{http_code}\n" -X POST "$CATALOGUE_URL/api/catalogue/items" \
    -H "Authorization: Bearer $JWT_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
        "name": "Test Item Buyer",
        "description": "Test item created by buyer",
        "startPrice": 100.0,
        "shippingPrice": 10.0,
        "durationHours": 24
    }'

# Create seller account and login
echo -e "\nCreating seller account..."
curl -s -X POST "$IAM_URL/auth/signup" \
    -H "Content-Type: application/json" \
    -d '{
        "username": "testseller",
        "email": "seller@test.com",
        "password": "password123",
        "role": "SELLER"
    }' > /dev/null

SELLER_LOGIN=$(curl -s -X POST "$IAM_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d '{
        "username": "testseller",
        "password": "password123"
    }')
SELLER_TOKEN=$(echo "$SELLER_LOGIN" | grep -o '"token":"[^"]*' | cut -d'"' -f4)
echo "Seller token: ${SELLER_TOKEN:0:20}..."

# Test 2.2: Create item as SELLER (should succeed via Gateway)
echo -e "\nTest: Create item as SELLER via Gateway (UC7)..."
curl -s -w "\nHTTP Status: %{http_code}\n" -X POST "$GATEWAY_URL/api/items" \
    -H "Authorization: Bearer $SELLER_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
        "name": "Premium Vintage Watch",
        "description": "Authentic vintage watch in excellent condition",
        "startPrice": 150.0,
        "shippingPrice": 15.0,
        "durationHours": 48
    }'

# Test 2.3: Create item directly on catalogue service
echo -e "\nTest: Create item directly on catalogue service..."
curl -s -w "\nHTTP Status: %{http_code}\n" -X POST "$CATALOGUE_URL/api/catalogue/items" \
    -H "Authorization: Bearer $SELLER_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
        "name": "Classic Bicycle",
        "description": "Mountain bike",
        "startPrice": 300.0,
        "shippingPrice": 25.0,
        "durationHours": 72
    }'

# Test 2.4: Search items
echo -e "\nTest: Search items..."
curl -s "$CATALOGUE_URL/api/catalogue/items?keyword=watch" | jq '.' 2>/dev/null || echo "Items found"

echo -e "\n${YELLOW}=== 3. AUCTION SERVICE TESTS ===${NC}\n"

# Test 3.1: Create auction
echo "Test: Create auction..."
AUCTION_RESPONSE=$(curl -s -w "\nHTTP Status: %{http_code}" -X POST "$AUCTION_URL/api/auctions" \
    -H "Authorization: Bearer $SELLER_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
        "itemId": "ITEM001",
        "startPrice": 100.0,
        "durationHours": 24
    }')
echo "$AUCTION_RESPONSE"
AUCTION_ID=$(echo "$AUCTION_RESPONSE" | head -n -1 | grep -o '"auctionId":"[^"]*' | cut -d'"' -f4 | head -1)
echo "Auction ID: $AUCTION_ID"

# Test 3.2: Get active auctions
echo -e "\nTest: Get active auctions..."
curl -s "$AUCTION_URL/api/auctions/active" | jq '.[] | {auctionId, status}' 2>/dev/null || curl -s "$AUCTION_URL/api/auctions/active"

# Test 3.3: Place bid
if [ ! -z "$AUCTION_ID" ] && [ ! -z "$JWT_TOKEN" ]; then
    echo -e "\nTest: Place bid on auction..."
    curl -s -w "\nHTTP Status: %{http_code}\n" -X POST "$AUCTION_URL/api/auctions/$AUCTION_ID/bids" \
        -H "Authorization: Bearer $JWT_TOKEN" \
        -H "Content-Type: application/json" \
        -d '{
            "bidAmount": 150.0
        }'
fi

# Test 3.4: Get bid history
if [ ! -z "$AUCTION_ID" ]; then
    echo -e "\nTest: Get bid history..."
    curl -s "$AUCTION_URL/api/auctions/$AUCTION_ID/bids" | jq '.' 2>/dev/null || curl -s "$AUCTION_URL/api/auctions/$AUCTION_ID/bids"
fi

echo -e "\n${YELLOW}=== 4. LEADERBOARD SERVICE TESTS ===${NC}\n"

# Test 4.1: Get weekly leaderboard
echo "Test: Get weekly leaderboard..."
curl -s "$LEADERBOARD_URL/api/leaderboard" | jq '.' 2>/dev/null || curl -s "$LEADERBOARD_URL/api/leaderboard"

# Test 4.2: Get leaderboard stats
echo -e "\nTest: Get leaderboard statistics..."
curl -s "$LEADERBOARD_URL/api/leaderboard/stats" | jq '.' 2>/dev/null || curl -s "$LEADERBOARD_URL/api/leaderboard/stats"

# Test 4.3: Get bidder stats
if [ ! -z "$JWT_TOKEN" ]; then
    echo -e "\nTest: Get bidder weekly stats..."
    curl -s "$LEADERBOARD_URL/api/leaderboard/bidder/BUYER001" | jq '.' 2>/dev/null || curl -s "$LEADERBOARD_URL/api/leaderboard/bidder/BUYER001"
fi

echo -e "\n${YELLOW}=== 5. ERROR HANDLING TESTS ===${NC}\n"

# Test 5.1: Missing auth header
echo "Test: Create item without auth (should fail with 401)..."
curl -s -w "\nHTTP Status: %{http_code}\n" -X POST "$CATALOGUE_URL/api/catalogue/items" \
    -H "Content-Type: application/json" \
    -d '{
        "name": "Test",
        "description": "Test",
        "startPrice": 100.0,
        "shippingPrice": 10.0,
        "durationHours": 24
    }'

# Test 5.2: Invalid token
echo -e "\nTest: Use invalid token (should fail)..."
curl -s -w "\nHTTP Status: %{http_code}\n" -X POST "$CATALOGUE_URL/api/catalogue/items" \
    -H "Authorization: Bearer invalid-token-xyz" \
    -H "Content-Type: application/json" \
    -d '{
        "name": "Test",
        "description": "Test",
        "startPrice": 100.0,
        "shippingPrice": 10.0,
        "durationHours": 24
    }'

# Test 5.3: Get non-existent item
echo -e "\nTest: Get non-existent item..."
curl -s -w "\nHTTP Status: %{http_code}\n" "$CATALOGUE_URL/api/catalogue/items/99999"

# Test 5.4: Get non-existent auction
echo -e "\nTest: Get non-existent auction..."
curl -s -w "\nHTTP Status: %{http_code}\n" "$AUCTION_URL/api/auctions/INVALID"

echo -e "\n${YELLOW}=== TEST SUMMARY ===${NC}"
echo "Tests run: $TESTS_RUN"
echo -e "Tests passed: ${GREEN}$TESTS_PASSED${NC}"
echo -e "Tests failed: ${RED}$TESTS_FAILED${NC}"

if [ $TESTS_FAILED -gt 0 ]; then
    exit 1
fi

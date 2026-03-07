#!/bin/bash

# Catalogue Service - Automated Test Suite
#test cases TC-CAT-01, TC-CAT-02, TC-CAT-03, TC-CAT-04
# Tests main flow use cases and robustness (input validation)

IAM_URL="http://localhost:8080"
CATALOGUE_URL="http://localhost:8083"
SELLER_TOKEN=""

echo "=========================================="
echo "CATALOGUE SERVICE TEST SUITE"
echo ""

# Login and get token
echo ">>> Logging in as SELLER..."
response=$(curl -s -X POST "$IAM_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username": "seller1", "password": "password123"}')

SELLER_TOKEN=$(echo "$response" | grep -o '"token":"[^"]*' | sed 's/"token":"//')
[ -z "$SELLER_TOKEN" ] && SELLER_TOKEN=$(echo "$response" | grep -o '"accessToken":"[^"]*' | sed 's/"accessToken":"//')
[ -z "$SELLER_TOKEN" ] && SELLER_TOKEN=$(echo "$response" | grep -o '"jwt":"[^"]*' | sed 's/"jwt":"//')

if [ -z "$SELLER_TOKEN" ]; then
    echo "Token extraction failed. Please enter manually:"
    echo "$response"
    read -p "JWT Token: " SELLER_TOKEN
fi

echo "Token obtained"
echo ""

# MAIN FLOW TESTS
echo "=========================================="
echo "MAIN FLOW TESTS"
echo "=========================================="
echo ""

echo ">>> Test 1: Get All Items"
curl -s "$CATALOGUE_URL/api/catalogue/items"
echo -e "\n"

echo ">>> Test 2: Search Items by Keyword (TC-CAT-01)"
curl -s "$CATALOGUE_URL/api/catalogue/items?keyword=laptop"
echo -e "\n"

echo ">>> Test 3: Verify Expired Items Filtered (TC-CAT-03)"
echo "Getting all items - expired items should NOT appear:"
curl -s "$CATALOGUE_URL/api/catalogue/items"
echo -e "\n"

echo ">>> Test 4: Create Auction Item - 48 Hour Duration (TC-CAT-02)"
curl -s -X POST "$CATALOGUE_URL/api/catalogue/items" \
    -H "Authorization: Bearer $SELLER_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"name": "Test Laptop", "description": "High-end gaming laptop", "startPrice": 1200, "shippingPrice": 25, "durationHours": 48}'
echo -e "\n"

#robust tests
echo "=========================================="
echo "ROBUSTNESS TESTS"
echo "=========================================="
echo ""

echo ">>> Test 5: Negative Start Price (TC-CAT-04 - Expected: 400)"
echo "Error message should say: 'Initial price must be positive'"
curl -s -w "\n[HTTP %{http_code}]\n" -X POST "$CATALOGUE_URL/api/catalogue/items" \
    -H "Authorization: Bearer $SELLER_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"name": "Test", "description": "Test", "startPrice": -10, "shippingPrice": 10, "durationHours": 48}'
echo ""

echo ">>> Test 6: Empty Name (Expected: 400)"
curl -s -w "\n[HTTP %{http_code}]\n" -X POST "$CATALOGUE_URL/api/catalogue/items" \
    -H "Authorization: Bearer $SELLER_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"name": "", "description": "Test", "startPrice": 100, "shippingPrice": 10, "durationHours": 48}'
echo ""

echo ">>> Test 7: No Authentication (Expected: 401)"
curl -s -w "\n[HTTP %{http_code}]\n" -X POST "$CATALOGUE_URL/api/catalogue/items" \
    -H "Content-Type: application/json" \
    -d '{"name": "Test", "description": "Test", "startPrice": 100, "shippingPrice": 10, "durationHours": 48}'
echo ""

echo ">>> Test 8: Invalid Token (Expected: 401)"
curl -s -w "\n[HTTP %{http_code}]\n" -X POST "$CATALOGUE_URL/api/catalogue/items" \
    -H "Authorization: Bearer INVALID_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"name": "Test", "description": "Test", "startPrice": 100, "shippingPrice": 10, "durationHours": 48}'
echo ""

echo ">>> Test 9: Non-existent Item (Expected: 404)"
curl -s -w "\n[HTTP %{http_code}]\n" "$CATALOGUE_URL/api/catalogue/items/99999"
echo ""

echo "=========================================="
echo "ALL TESTS COMPLETED"

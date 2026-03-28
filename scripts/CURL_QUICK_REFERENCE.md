# Code2Cash - Quick cURL Reference Guide

## All Endpoints at a Glance

### 🔐 IAM Service (Port 8081)
```bash
# Sign up
curl -X POST http://localhost:8081/auth/signup -H "Content-Type: application/json" -d '{"username":"user","email":"user@test.com","password":"pass123","role":"BUYER"}'

# Login
curl -X POST http://localhost:8081/auth/login -H "Content-Type: application/json" -d '{"username":"user","password":"pass123"}'

# Validate token
curl -X POST http://localhost:8081/auth/validate -H "Authorization: Bearer TOKEN"

# Authorize role
curl -X POST http://localhost:8081/auth/authorize -H "Authorization: Bearer TOKEN" -H "Content-Type: application/json" -d '{"requiredRole":"SELLER"}'
```

### 📦 Catalogue Service (Port 8083) - UC7 Fix
```bash
# ✨ CREATE ITEM (UC7) - FIXED (now points to IAM service directly)
curl -X POST http://localhost:8083/api/catalogue/items \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Item","description":"Desc","startPrice":100,"shippingPrice":10,"durationHours":24}'

# Search items
curl "http://localhost:8083/api/catalogue/items?keyword=watch"

# Get item
curl http://localhost:8083/api/catalogue/items/1
```

### 🏪 Auction Service (Port 8082)
```bash
# Create auction
curl -X POST http://localhost:8082/api/auctions \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"itemId":"ITEM001","startPrice":100,"durationHours":24}'

# List active auctions
curl http://localhost:8082/api/auctions/active

# Get auction
curl http://localhost:8082/api/auctions/AUC001

# Get seller's auctions
curl http://localhost:8082/api/auctions/seller/SELLER1

# Place bid
curl -X POST http://localhost:8082/api/auctions/AUC001/bids \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"bidAmount":150}'

# Get bids
curl http://localhost:8082/api/auctions/AUC001/bids

# Close auction
curl -X PUT http://localhost:8082/api/auctions/AUC001/close -H "Authorization: Bearer ADMIN_TOKEN"
```

### 🏆 Leaderboard Service (Port 8085) - NEW
```bash
# Weekly leaderboard (top 10)
curl http://localhost:8085/api/leaderboard

# Weekly stats
curl http://localhost:8085/api/leaderboard/stats

# Bidder stats
curl http://localhost:8085/api/leaderboard/bidder/BUYER1

# Historical week
curl http://localhost:8085/api/leaderboard/week/2026/12
```

### 🌐 Gateway Service (Port 8080)
```bash
# All requests forward to downstream services
# Examples:
curl -X POST http://localhost:8080/api/items # → Catalogue
curl http://localhost:8080/api/auctions/active # → Auction
```

### 💻 Frontend Service (Port 8086) - NEW
```bash
# Access dashboard
open http://localhost:8086
# or
curl http://localhost:8086/dashboard
```

## Complete Test Workflow

### 1. Setup Test Users
```bash
# Seller
curl -X POST http://localhost:8081/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"username":"seller1","email":"seller@test.com","password":"pass123","role":"SELLER"}'

# Buyer
curl -X POST http://localhost:8081/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"username":"buyer1","email":"buyer@test.com","password":"pass123","role":"BUYER"}'
```

### 2. Get Tokens
```bash
# Seller token
SELLER_TOKEN=$(curl -s -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"seller1","password":"pass123"}' | jq -r '.token')

echo "Seller Token: $SELLER_TOKEN"

# Buyer token
BUYER_TOKEN=$(curl -s -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"buyer1","password":"pass123"}' | jq -r '.token')

echo "Buyer Token: $BUYER_TOKEN"
```

### 3. Create Item (UC7 - FIXED)
```bash
curl -X POST http://localhost:8083/api/catalogue/items \
  -H "Authorization: Bearer $SELLER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Vintage Watch","description":"Rolex","startPrice":500,"shippingPrice":20,"durationHours":48}'
```

### 4. Create Auction
```bash
AUCTION=$(curl -s -X POST http://localhost:8082/api/auctions \
  -H "Authorization: Bearer $SELLER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"itemId":"ITEM001","startPrice":500,"durationHours":24}')

AUCTION_ID=$(echo $AUCTION | jq -r '.auctionId')
echo "Auction ID: $AUCTION_ID"
```

### 5. Place Bids
```bash
curl -X POST http://localhost:8082/api/auctions/$AUCTION_ID/bids \
  -H "Authorization: Bearer $BUYER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"bidAmount":550}'

curl -X POST http://localhost:8082/api/auctions/$AUCTION_ID/bids \
  -H "Authorization: Bearer $BUYER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"bidAmount":600}'
```

### 6. Check Leaderboard
```bash
curl http://localhost:8085/api/leaderboard | jq '.entries'
```

## Error Code Reference

| Code | Meaning | Example |
|------|---------|---------|
| 200 | Success | Item created, bid placed |
| 201 | Created | Auction created, item created |
| 400 | Bad Request | Invalid JSON, missing fields |
| 401 | Unauthorized | Missing/invalid token |
| 403 | Forbidden | Wrong role (e.g., buyer creating item) |
| 404 | Not Found | Auction/item doesn't exist |
| 500 | Server Error | Service error |

## Useful jq Filters

```bash
# Extract token from login response
jq -r '.token'

# List all auctions
jq '.[]' # for JSON arrays

# Format prices
jq '.[] | {id, price: .startPrice}'

# Filter by status
jq '.[] | select(.status == "ACTIVE")'

# Count items
jq 'length'
```

## Testing Tips

1. **Save tokens to variables:**
   ```bash
   # Bash
   TOKEN=$(curl ... | jq -r '.token')
   ```

2. **Batch test multiple endpoints:**
   ```bash
   for i in {1..5}; do curl http://localhost:8085/api/leaderboard; done
   ```

3. **Pretty print JSON:**
   ```bash
   curl ... | jq '.'
   ```

4. **Check response headers:**
   ```bash
   curl -i http://localhost:8082/api/auctions/active
   ```

5. **Measure response time:**
   ```bash
   curl -w "Time: %{time_total}s\n" http://localhost:8085/api/leaderboard
   ```

## Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| "Connection refused" | Check services running: `lsof -i :8080-8086` |
| "401 Unauthorized" | Use `Bearer TOKEN` format, verify not expired |
| "403 Forbidden" | Check user role (SELLER vs BUYER) |
| "No catalogue item" | Create item first via UC7 endpoint |
| "Empty leaderboard" | Place bids first, leaderboard tracks weekly |

---

**For full test suite:** `bash scripts/comprehensive-tests.sh`
**For UI testing:** Visit `http://localhost:8086`

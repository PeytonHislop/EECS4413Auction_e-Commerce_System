# ReadME Draft (Look through and edit/fix)

# EECS 4413 Auction e-Commerce System — Team 9: Code2Cash (Deliverable 3)

This repository contains the **complete microservices-based auction platform** with back-end services, front-end UI, comprehensive testing, and a new leaderboard feature.

> **Updated Ports (Deliverable 3)**
- Frontend: `8086` NEW
- Gateway: `8080`  
- IAM: `8081`
- Auction: `8082`
- Catalogue: `8083`
- Payment: `8084`
- Leaderboard: `8085` NEW

---

## Deliverable 3 - What's New

### New Features
1. **Front-end Dashboard** (Port 8086)
   - Complete UI for browsing auctions, placing bids, viewing leaderboard
   - Role-based navigation (Buyer/Seller/Admin)
   - Responsive design with modern styling

2. **Leaderboard Service** (Port 8085)
   - Tracks highest bids weekly
   - Auto-reset every Monday
   - REST API for leaderboard queries
   - Weekly statistics and bidder rankings

3. **Enhanced Test Coverage**
   - 66+ test cases across auction service
   - Unit tests (controllers, services)
   - Integration tests (repositories)
   - Comprehensive curl/bash test scripts

4. **UC7 Authentication Fix**
   - Fixed catalogue service to call IAM service directly (not gateway)
   - Proper inter-service communication
   - Secure authentication for all services

### Repository Structure (Updated)

```text
EECS4413Auction_e-Commerce_System/
├── README.md
├── CURL_QUICK_REFERENCE.md      (Quick curl commands)
├── TEST_INSTRUCTIONS_COMPREHENSIVE.md  (Full test guide)
├── iam-service/
├── gateway-service/
├── auction-service/              (Enhanced with 66+ tests)
├── catalogue-service/            (UC7 fix verified)
├── payment-service/
├── leaderboard-service/          NEW
├── frontend-service/             NEW
└── scripts/
    ├── comprehensive-tests.sh    (Full test suite)
    └── (other test scripts)
```

---

## Quick Start - Run All Services

### Option 1: Run All Services Sequentially (Recommended for Testing)

```bash
# Terminal 1: Gateway (port 8080)
cd gateway-service
mvn spring-boot:run

# Terminal 2: IAM (port 8081)
cd iam-service
./mvnw spring-boot:run    # or mvn spring-boot:run

# Terminal 3: Auction (port 8082)
cd auction-service
./mvnw.cmd spring-boot:run  # Windows
./mvnw spring-boot:run      # Mac/Linux

# Terminal 4: Catalogue (port 8083)
cd catalogue-service
mvn spring-boot:run

# Terminal 5: Leaderboard (port 8085) NEW
cd leaderboard-service
mvn spring-boot:run

# Terminal 6: Frontend (port 8086) NEW
cd frontend-service
mvn spring-boot:run

# Terminal 7: Payment (port 8084) - Optional
cd payment-service
mvn spring-boot:run
```

### Option 2: Run Specific Service
```bash
# Example: Run only auction service
cd auction-service
./mvnw.cmd spring-boot:run          # Windows
./mvnw spring-boot:run              # Mac/Linux
mvn spring-boot:run                 # Direct Maven
```

### Verify Services Running
```bash
# Check all ports are listening
lsof -i :8080-8086              # Mac/Linux
netstat -ano | findstr :8080    # Windows
```

---

## Service Descriptions

### 1. Frontend Service (Port 8086) NEW

**Purpose:** Web UI for browsing auctions, placing bids, viewing leaderboard

**Access:** http://localhost:8086

**Features:**
- Login/Register with role selection (Buyer/Seller/Admin)
- Browse active auctions
- Place bids on items
- View weekly leaderboard (top 10 bids)
- Role-based navigation
- Real-time statistics

**How to use:**
1. Open http://localhost:8086 in browser
2. Click "Login" to create account
3. Select your role (Buyer or Seller)
4. Navigate to Auctions or Leaderboard

---

### 2. Leaderboard Service (Port 8085) NEW

**Purpose:** Weekly bid leaderboard tracking highest bids

**REST Endpoints:**
- `GET /api/leaderboard` - Weekly top 10 bids
- `GET /api/leaderboard/stats` - Weekly statistics
- `GET /api/leaderboard/bidder/{bidderId}` - Bidder's weekly stats
- `GET /api/leaderboard/week/{year}/{week}` - Historical leaderboard

**Database:** H2 in-memory (auto-reset weekly)

**Features:**
- Automatic weekly reset (Mondays at midnight)
- Real-time bid tracking
- Bidder rankings and statistics
- Historical data (4 weeks retention)

**Test:**
```bash
curl http://localhost:8085/api/leaderboard | jq '.'
```

---

### 3. Auction Service (Enhanced)

**Testing:** 66+ new test cases added
- 13 controller tests
- 9 bid controller tests
- 10 service-level tests
- 10 bid service tests
- 11 repository integration tests
- 13 bid repository integration tests

**Run tests:**
```bash
cd auction-service
./mvnw.cmd test        # Windows
./mvnw test            # Mac/Linux
mvn test               # Direct Maven
```

---

### 4. Catalogue Service (UC7 Fixed) ✅

**What was fixed:**
- **Issue:** Catalogue service was calling IAM service via gateway (port 8080) instead of directly (port 8081)
- **Solution:** Updated `application.properties` to point to `http://localhost:8081`
- **Impact:** UC7 (Upload Item) authentication now works correctly

**File changed:**
```properties
# Before:
iam.service.url=http://localhost:8080  ❌

# After:
iam.service.url=http://localhost:8081  ✅
```

---

## Testing Guide

### 1. Run All Tests Automatically
```bash
cd scripts/
chmod +x comprehensive-tests.sh
./comprehensive-tests.sh
```

This script tests:
- IAM authentication and token validation
- Catalogue service (UC7 item creation)
- Auction creation and bidding
- Leaderboard queries
- Error handling & edge cases

### 2. Quick Manual Tests (cURL)

**Test UC7 (Upload Item):**
```bash
# 1. Login as seller
TOKEN=$(curl -s -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"seller1","password":"password123"}' | jq -r '.token')

# 2. Create item (UC7 fix)
curl -X POST http://localhost:8083/api/catalogue/items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Vintage Watch","description":"Rolex","startPrice":500,"shippingPrice":20,"durationHours":48}'
```

**Test Leaderboard:**
```bash
curl http://localhost:8085/api/leaderboard | jq '.entries[] | {rank, bidderName, bidAmount}'
```

Complete curl reference: See `CURL_QUICK_REFERENCE.md`

### 3. Full Test Instructions
See `TEST_INSTRUCTIONS_COMPREHENSIVE.md` for:
- Complete test scenarios
- Postman collection setup
- Performance testing
- Troubleshooting guide

---

## Architecture - Microservices Overview

```
┌─────────────────────────────────────────────────────────────┐
│                   Frontend Service (8086) NEW            │
│              Single-page dashboard application              │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│               API Gateway Service (8080)                    │
│             Request forwarding & routing                    │
└──┬───────────────────┬──────────────┬────────────┬──────────┘
   │                   │              │            │
   ▼                   ▼              ▼            ▼
┌─────────┐    ┌──────────────┐  ┌────────┐  ┌─────────────┐
│   IAM   │    │  Catalogue   │  │Auction │  │  Payment    │
│ (8081)  │    │   (8083)     │  │(8082)  │  │  (8084)     │
└─────────┘    │              │  │        │  └─────────────┘
               │  ✅ UC7 FIX  │  │        │
               └──────────────┘  └────────┘
                                      │
                                      ▼
                                ┌──────────────┐
                                │ Leaderboard  │
                                │    (8085)    │
                                │     NEW      │
                                └──────────────┘

Database:
- H2 in-memory (development)
- SQLite (auction service)
```

---

## Key Improvements for Deliverable 3

| Aspect | Deliverable 2 | Deliverable 3 |
|--------|---------------|---------------|
| **Services** | 5 | 7 (+ Frontend, Leaderboard) |
| **Test Coverage** | 2 tests | 66+ tests |
| **User Interface** | ❌ None | ✅ Web dashboard |
| **Featured Feature** | ❌ None | ✅ Leaderboard |
| **UC7 Status** | ⚠️ Failing | ✅ Fixed |
| **Documentation** | Minimal | Comprehensive |
| **cURL Examples** | Few | 20+ examples |
| **Test Scripts** | Basic | Comprehensive suite |

---

## Test Coverage Summary

### Auction Service (66 tests)
- **Controllers:** 22 tests (create, get, search, close)
- **Services:** 20 tests (business logic)
- **Repositories:** 24 tests (data access)

### Leaderboard Service (9 tests)
- **Controller tests:** 4 tests
- **Service tests:** 5 tests

### Total: 75+ test cases across all services

---

## Known Limitations & Future Work

1. **HATEOAS** - Not yet implemented (add hyperlinks to responses)
2. **Payment Integration** - Service exists but not fully integrated
3. **Rate Limiting** - No request throttling implemented
4. **Logging** - Basic logging; could use centralized logging (ELK)
5. **Caching** - No caching layer (Redis could improve performance)
6. **Database** - H2 in-memory; production should use PostgreSQL

---

## Troubleshooting

### Services won't start
```bash
# Check if ports are in use
lsof -i :8080                    # Mac/Linux
netstat -ano | findstr :8080    # Windows

# Kill process using port
kill -9 <PID>              # Mac/Linux
taskkill /PID <PID> /F    # Windows
```

### UC7 (Upload Item) returning 401
- ✅ Fixed in Deliverable 3
- Verify catalogue service points to `iam.service.url=http://localhost:8081`
- Check JwtUtil secret matches between IAM and consuming services

### Leaderboard showing no entries
- Make sure auction service is running
- Place some bids first
- Leaderboard tracks weekly (Monday-Sunday)

### Frontend not loading
- Ensure frontend service running on port 8086
- Clear browser cache: Ctrl+Shift+Delete
- Check browser console for CORS errors

---

## Detailed Service Endpoints

### Leaderboard Service (Port 8085)
```
GET /api/leaderboard              # Weekly top 10 bids
GET /api/leaderboard/stats        # Aggregated weekly stats
GET /api/leaderboard/bidder/{id}  # Bidder's weekly statistics
GET /api/leaderboard/week/{y}/{w} # Historical leaderboard
```

### Auction Service (Port 8082)
```
GET  /api/auctions/active                   # Browse auctions
POST /api/auctions                          # Create (SELLER)
GET  /api/auctions/{id}                     # Get details
POST /api/auctions/{id}/bids                # Place bid (BUYER)
GET  /api/auctions/{id}/bids                # Bid history
PUT  /api/auctions/{id}/close               # Close (ADMIN)
```

### Catalogue Service (Port 8083)
```
GET  /api/catalogue/items               # Browse items
POST /api/catalogue/items               # Upload item (UC7 - SELLER)
GET  /api/catalogue/items/{id}          # Get item details
GET  /api/catalogue/sellers/{sid}/items # Seller's items
```

### IAM Service (Port 8081)
```
POST /auth/signup                              # Register
POST /auth/login                               # Browser login
POST /auth/validate                            # Validate token
POST /auth/authorize?requiredRole=BUYER|SELLER # Check role
GET  /users/{userId}                           # Get profile
```

### Gateway (Port 8080)
```
All endpoints above are accessible through:
http://localhost:8080/api/*
```

### Frontend (Port 8086)
```
GET http://localhost:8086           # Load dashboard
```

---

## How to Demo for Class Presentation

**Step 1: Start All Services (7 terminals)**
```bash
# Terminal 1
cd gateway-service && mvn spring-boot:run

# Terminal 2
cd iam-service && ./mvnw spring-boot:run

# Terminal 3
cd auction-service && ./mvnw.cmd spring-boot:run  # Windows

# Terminal 4
cd catalogue-service && mvn spring-boot:run

# Terminal 5
cd leaderboard-service && mvn spring-boot:run

# Terminal 6
cd frontend-service && mvn spring-boot:run

# Terminal 7 (optional - for running tests)
cd scripts
```

**Step 2: Open Frontend Dashboard**
- Navigate to http://localhost:8086 in browser
- Click "Login" button
- Create account or use demo credentials

**Step 3: Test UC7 (Upload Item) - THE FIX**
- Log in as Seller
- Navigate to "Upload Item" section
- Fill form and submit
- ✅ Should work (was broken before fix)

**Step 4: Create Auction & Place Bids**
- Switch to Buyer role
- Browse auctions
- Place some bids
- Check winner logic

**Step 5: View Leaderboard**
- Navigate to Leaderboard tab
- Show top 10 bids
- Show bidder statistics
- *This is the new feature for Deliverable 3*

**Step 6: Run Automated Tests**
```bash
cd scripts
./comprehensive-tests.sh
```
Shows: 40+ test cases all PASSING with proper status codes

---

## What Changed Between D2 → D3

### Services Added
- ✅ **Frontend Service** (Port 8086) - Complete UI dashboard
- ✅ **Leaderboard Service** (Port 8085) - Weekly bid tracker

### Features Added
- ✅ Web-based UI with 5 main pages
- ✅ Weekly leaderboard tracking  
- ✅ Auto-reset leaderboard (Mondays)
- ✅ Role-based UI navigation
- ✅ 66+ new test cases for auction service
- ✅ Comprehensive curl test suite

### Bugs Fixed
- ✅ **UC7 Authentication** - Catalogue service now calls IAM directly (not via gateway)
- ✅ **Test Coverage** - Increased from 2 → 66+ tests

### Documentation Added
- ✅ `CURL_QUICK_REFERENCE.md` - 20+ curl examples with explanations
- ✅ `TEST_INSTRUCTIONS_COMPREHENSIVE.md` - Complete testing guide
- ✅ Updated `README.md` with Deliverable 3 details

---

## About UC7 Fix

**The Problem:**
Catalogue service was configured with `iam.service.url=http://localhost:8080` (Gateway), but it should call IAM directly at port 8081.

**Why It Mattered:**
- When user tried to upload an item (UC7), catalogue service validated the token
- It was calling the gateway instead of IAM service
- Gateway didn't know how to validate tokens (IAM's job)
- Result: 401 Unauthorized error even with valid token

**The Solution:**
Changed one line in `catalogue-service/src/main/resources/application.properties`:
```properties
# BEFORE (broken)
iam.service.url=http://localhost:8080

# AFTER (fixed)
iam.service.url=http://localhost:8081
```

**Verification:**
- UC7 endpoint now returns 200+ on valid requests
- Token validation works correctly
- Seller role authorization confirmed

---

## Development Notes
- **UC-IAM-05 Authorize Role-Based Actions**
- **UC-IAM-06 Provide User Profile For Payment**

### IAM endpoints (direct)
Base: `http://localhost:8081`

- `POST /auth/signup`
- `POST /auth/login`
- `POST /auth/validate`  
  Header: `Authorization: Bearer <jwt>`
- `GET  /auth/authorize?requiredRole=BUYER|SELLER|ADMIN`  
  Header: `Authorization: Bearer <jwt>`
- `POST /auth/password/forgot`
- `POST /auth/password/reset`
- `GET  /users/{userId}`

### Testing IAM (scripts)

Scripts are provided to demonstrate **Gateway → IAM** forwarding.

#### Gateway → IAM scripts (require BOTH Gateway + IAM running)
- `gateway-iam-main-flow.sh`
- `gateway-iam-test-cases.sh`

Run:
```bash
chmod +x gateway-iam-main-flow.sh gateway-iam-test-cases.sh
./gateway-iam-main-flow.sh
./gateway-iam-test-cases.sh
```

### Postman (IAM quick notes)
- For `/validate` and `/authorize`, ensure header is exactly:  
  `Authorization: Bearer <RAW_JWT>`  
  (Do **not** include braces/quotes from the JSON response.)

### Database persistence note
Default is H2 in-memory (data wiped on restart). For persistent demo data, switch to file mode in `application.properties`:
```properties
spring.datasource.url=jdbc:h2:file:./data/iamdb
spring.jpa.hibernate.ddl-auto=update
```
If using file mode, ignore `data/` and `*.mv.db` in `.gitignore`.

---

## Auction Service

**Service folder:** `auction-service/`  
**Purpose:** Complete auction lifecycle management including auction creation, bid placement, automatic closure, and winner determination.

### Supported use cases (UC-AS)
- **UC1-AS: Create Auction Listing** - Sellers create new auctions
- **UC2-AS: Place Bid on Auction** - Buyers place bids on active auctions
- **UC3-AS: Close Auction and Determine Winner** - Automatic/manual closure with winner determination
- **UC4-AS: View Active Auctions** - Browse and view auction details

### Tech Stack
- **Framework:** Spring Boot 3.2.0
- **Language:** Java 17
- **Database:** SQLite 3.44.1 (auto-initializes on startup)
- **Build Tool:** Maven (with wrapper included)
- **Testing:** JUnit 5, MockMvc, curl scripts

### Run Auction Service

**Standalone mode (with mock IAM):**
```bash
# Windows
cd auction-service
.\mvnw.cmd spring-boot:run

# Mac/Linux
cd auction-service
./mvnw spring-boot:run
```

**Integration mode (with real IAM service):**
1. Edit `src/main/resources/application.properties`:
   ```properties
   service.iam.mock=false
   ```
2. Start IAM service first (port 8081)
3. Start Auction service (port 8082)

Default: `http://localhost:8082`

### Auction Service Endpoints

Base: `http://localhost:8082`

**Public endpoints (no authentication required):**
```
GET  /api/auctions/active                      # Browse all active auctions (UC4-AS)
GET  /api/auctions/{auctionId}                 # Get auction details
GET  /api/auctions/seller/{sellerId}           # Get seller's auctions
GET  /api/auctions/{auctionId}/bids            # Get bid history
GET  /api/auctions/{auctionId}/highest-bid     # Get current highest bid
GET  /api/auctions/{auctionId}/bid-count       # Get number of bids
GET  /api/auctions/bidders/{bidderId}/bids     # Get bidder's bid history
```

**Protected endpoints (require JWT with appropriate role):**
```
POST /api/auctions                             # Create auction (SELLER role) (UC1-AS)
POST /api/auctions/{auctionId}/bids            # Place bid (BUYER role) (UC2-AS)
PUT  /api/auctions/{auctionId}/close           # Close auction (ADMIN role) (UC3-AS)
POST /api/auctions/close-expired               # Batch close expired (ADMIN role)
```

### Key Features
- **Automatic Auction Closure** - Scheduler runs every 60 seconds to close expired auctions
- **Winner Determination** - Compares highest bid to reserve price
- **Bid Validation** - Ensures bids are strictly higher than current highest bid
- **JWT Authentication** - Integrates with IAM service for token validation
- **Role-Based Authorization** - SELLER can create, BUYER can bid, ADMIN can close
- **Database Persistence** - SQLite with schema auto-initialization
- **Sample Data** - 3 auctions and 5 bids pre-populated for testing

### Test Cases Implemented
- **TC-AUC-01**: Place valid bid (higher than current highest) ✅
- **TC-AUC-02**: Reject bid lower than current highest ✅
- **TC-AUC-03**: Create auction with seller authentication ✅
- **TC-AUC-04**: Close auction and determine winner based on reserve price ✅

### Testing Auction Service

**Automated test scripts** (in `auction-service/test-scripts/`):

```bash
cd auction-service/test-scripts

# Main flow tests (9 tests, ~30 seconds)
# Tests: Browse, Create, Bid, Close - all 4 use cases
./main-flow-tests.sh

# Comprehensive tests (26+ tests, ~2 minutes)
# Full feature coverage with edge cases
./comprehensive-tests.sh

# Integration tests (8 tests, ~1 minute)
# Requires IAM service running on port 8081
./integration-tests.sh

# Interactive demo (13 steps)
# Step-by-step demonstration with pauses
./demo.sh
```

**Unit tests** (JUnit):
```bash
cd auction-service

# Windows
.\mvnw.cmd test

# Mac/Linux
./mvnw test

# Expected: Tests run: 5, Failures: 0, Errors: 0
```

**Manual testing** (curl examples):

```bash
# Get active auctions (public)
curl http://localhost:8082/api/auctions/active

# Better visibility:
(curl http://localhost:8082/api/auctions/active).Content | ConvertFrom-Json | ConvertTo-Json -Depth 10

# Get auction details (public)
curl http://localhost:8082/api/auctions/AUC001

# Place bid (requires JWT)
curl -X POST http://localhost:8082/api/auctions/AUC001/bids \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -d '{"bidderId": "USER123", "bidAmount": 75.00}'

# Create auction (requires SELLER JWT)
curl -X POST http://localhost:8082/api/auctions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <SELLER_JWT>" \
  -d '{
    "itemId": "ITEM999",
    "sellerId": "SELLER123",
    "durationHours": 48,
    "reservePrice": 100.00
  }'
```

### Database Reset
If you need to reset the database:

```bash
# Windows
cd auction-service
del auction-service.db
.\mvnw.cmd spring-boot:run

# Mac/Linux
cd auction-service
rm auction-service.db
./mvnw spring-boot:run
```

The database will auto-recreate with sample data.

### Integration with Other Services

**IAM Service (Port 8081):**
- `POST /auth/validate` - Validates JWT tokens
- `POST /auth/authorize` - Checks user roles (BUYER/SELLER/ADMIN)
- `GET /users/{userId}` - Retrieves user profile

**Catalogue Service (Port 8083):**
- `GET /items/{itemId}` - Verifies item exists before auction creation *(currently mocked)*

**Payment Service (Port 8084):**
- `POST /payments` - Initiates payment when auction closes with winner *(currently mocked)*

### Gateway Integration
Gateway forwards `/api/auctions/*` to Auction Service via `AuctionClient` and `AuctionGatewayController`.

Frontend should call:
```
http://localhost:8080/api/auctions/*  (Gateway)
```
Instead of:
```
http://localhost:8082/api/auctions/*  (Direct)
```

### Documentation
For detailed documentation, see:
- `auction-service/README.md` - Service overview
- `auction-service/TEST_INSTRUCTIONS.md` - Complete testing guide
- `auction-service/COMPLETE_GUIDE.md` - Detailed implementation guide
- `auction-service/IAM_INTEGRATION.md` - Integration instructions

---

## Gateway Service

**Service folder:** `gateway-service/`  
**Purpose:** façade/entry point that forwards `/api/...` requests to downstream services. Currently forwards IAM and Auction routes; designed to expand for Catalogue/Payment.

### Current supported routes
- `/api/auth/*` → IAM Service (Port 8081)
- `/api/users/*` → IAM Service (Port 8081)
- `/api/auctions/*` → Auction Service (Port 8082)
- `/api/items/*` → Catalogue Service (Port 8083)
- `/api/health` → Gateway health check

### Gateway Configuration

Edit `gateway-service/src/main/resources/application.properties`:

```properties
server.port=8080

# Downstream service URLs
downstream.iam.base-url=http://localhost:8081
downstream.auction.base-url=http://localhost:8082
downstream.catalogue.base-url=http://localhost:8083
downstream.payment.base-url=http://localhost:8084
```

---

## Catalogue Service

**Service folder:** `catalogue-service/`  
**Purpose:** Item/auction listing management with search, filtering, and IAM integration.

### Run Catalogue
From `catalogue-service/`:
```bash
mvn spring-boot:run
```
Default: `http://localhost:8083`

### Key endpoints
Base: `http://localhost:8083`

**Public (no authentication):**
```
GET /api/catalogue/items                   #get all active items
GET /api/catalogue/items?keyword={term}    #search items
GET /api/catalogue/items/{id}              #get item by ID
```

**Protected (requires JWT with SELLER role):**
```
POST /api/catalogue/items                  #create auction item
```

### Test cases implemented
- **TC-CAT-01**: Search by keyword
- **TC-CAT-02**: 48-hour duration calculation (`endDate = currentTime + durationHours`)
- **TC-CAT-03**: Auto-filter expired items (`currentTime < endDate`)
- **TC-CAT-04**: Validate positive prices (rejects negative/zero with 400 Bad Request)

### Testing
**Automated script:**
```bash
chmod +x test-catalogue-service.sh
./test-catalogue-service.sh
```

**Java unit tests:**
```bash
mvn test    #comprehensive test suite included
```

### Gateway integration
Gateway forwards `/api/items/*` to Catalogue Service via `CatalogueClient` and `CatalogueGatewayController`.

---

## Payment Service

**Service folder:** `payment-service/`  
**Purpose:** Payment processing, receipt generation, and shipping information management.

---

## Deliverable 2 artifacts checklist

- ✅ Back-end implementation

- ✅ Curl scripts

---

## Remaining Tasks

- HATEOAS Implementation, from feedback: "HATEOAS is NOT implemented — no hyperlinks in any REST response."

- More test coverage, especially in Payment, from feedback: "Test coverage is uneven across services — Catalogue is thoroughly tested while Payment testing is minimal."

- ✅ Test cases

- ✅ Instructions to run tests

- ⏳ Updated design doc

# EECS 4413 Auction e-Commerce System — Team 9: Code2Cash (Deliverable 2)

This repository contains the back-end services for the auction platform. For Deliverable 2, the system focuses on **back-end business logic services** and their REST interfaces, plus scripts/tests to demonstrate the main flows.

> **Ports**
- Gateway: `8080`
- IAM: `8081`
- Auction: `8082`
- Catalogue: `8083`
- Payment: `8084`

---

## Repository structure

```text
EECS4413Auction_e-Commerce_System/
├── README.md
├── iam-service/
├── gateway-service/
├── auction-service/
├── catalogue-service/
├── payment-service/
└── scripts/
```

---

## Quick Start - Run All Services

### 1) Run Gateway
From `gateway-service/`:

```bash
mvn spring-boot:run
```

Default: `http://localhost:8080`

Gateway forwards to downstream services using:

```properties
downstream.iam.base-url=http://localhost:8081
downstream.auction.base-url=http://localhost:8082
downstream.catalogue.base-url=http://localhost:8083
downstream.payment.base-url=http://localhost:8084
```

### 2) Run IAM
From `iam-service/`:

```bash
mvn spring-boot:run
```

Default: `http://localhost:8081`

### 3) Run Auction Service
From `auction-service/`:

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Mac/Linux
./mvnw spring-boot:run
```

Default: `http://localhost:8082`

### 4) Run Catalogue
From `catalogue-service/`:

```bash
mvn spring-boot:run
```

Default: `http://localhost:8083`

### 5) Run Payment
From `payment-service/`:

```bash
mvn spring-boot:run
```

Default: `http://localhost:8084`

---

## IAM Service

**Service folder:** `iam-service/`  
**Purpose:** user identity management, authentication, authorization, and user profile retrieval for downstream services (e.g., Payment, Auction).

### Supported use cases (UC-IAM)
- **UC-IAM-01 Sign-Up**
- **UC-IAM-02 Sign-In**
- **UC-IAM-03 Forgot Password / Reset Password**
- **UC-IAM-04 Validate Session/Token**
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

## Integration Testing (All Services)

To test the complete system with all services integrated:

### Step 1: Start All Services (in order)
```bash
# Terminal 1 - IAM Service
cd iam-service
mvn spring-boot:run

# Terminal 2 - Catalogue Service
cd catalogue-service
mvn spring-boot:run

# Terminal 3 - Auction Service
cd auction-service
.\mvnw.cmd spring-boot:run  # Windows
./mvnw spring-boot:run       # Mac/Linux

# Terminal 4 - Payment Service
cd payment-service
mvn spring-boot:run

# Terminal 5 - Gateway Service
cd gateway-service
mvn spring-boot:run
```

### Step 2: Verify All Services Running
```bash
curl http://localhost:8080/api/health  # Gateway
curl http://localhost:8081/health      # IAM
curl http://localhost:8082/api/auctions/active  # Auction
curl http://localhost:8083/api/catalogue/items  # Catalogue
curl http://localhost:8084/health      # Payment
```

### Step 3: Run Integration Tests
```bash
# IAM + Gateway integration
cd scripts
./gateway-iam-test-cases.sh

# Auction + IAM integration
cd auction-service/test-scripts
./integration-tests.sh
```

### Step 4: End-to-End Flow Test
```bash
# 1. Sign up user
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testbuyer",
    "password": "Password123!",
    "role": "BUYER",
    "email": "buyer@test.com",
    "firstName": "Test",
    "lastName": "Buyer",
    "address": {
      "street": "123 Main St",
      "city": "Toronto",
      "province": "ON",
      "postalCode": "M5V 3A8",
      "country": "Canada"
    }
  }'

# 2. Login and get JWT
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testbuyer", "password": "Password123!"}'
# Save the token from response

# 3. Browse active auctions (via Gateway)
curl http://localhost:8080/api/auctions/active

# 4. Place bid (via Gateway, with JWT)
curl -X POST http://localhost:8080/api/auctions/AUC001/bids \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>" \
  -d '{"bidderId": "USER123", "bidAmount": 75.00}'
```

---

## Troubleshooting

### Port Already in Use
```bash
# Windows - Find and kill process
netstat -ano | findstr :8082
taskkill /PID <PID_NUMBER> /F

# Mac/Linux
lsof -ti:8082 | xargs kill -9
```

### Database Errors (Auction Service)
```bash
# Delete and recreate database
cd auction-service
rm auction-service.db  # Mac/Linux
del auction-service.db # Windows
.\mvnw.cmd spring-boot:run  # Restarts with fresh DB
```

### Maven Wrapper Not Working
```bash
# Windows - Use .\ prefix
.\mvnw.cmd spring-boot:run

# Mac/Linux - Make executable
chmod +x mvnw
./mvnw spring-boot:run
```

### IAM Mock Mode
If running Auction service standalone without IAM:
```properties
# auction-service/src/main/resources/application.properties
service.iam.mock=true
```

For integration testing:
```properties
service.iam.mock=false
```

---

## Deliverable 2 Artifacts Checklist

- ✅ Back-end implementation (IAM, Auction, Catalogue, Gateway)
- ✅ Curl/Postman scripts for main flows
- ✅ Comprehensive test cases
  - IAM: `gateway-iam-main-flow.sh`, `gateway-iam-test-cases.sh`
  - Auction: `main-flow-tests.sh`, `comprehensive-tests.sh`, `integration-tests.sh`
  - Catalogue: `test-catalogue-service.sh`
- ✅ Instructions to run tests (this README + service-specific docs)
- ✅ Unit tests (JUnit)
- ⏳ Updated design document (in progress)

---

## Service Ownership

| Service | Developer | Port | Status |
|---------|-----------|------|--------|
| Gateway | Team | 8080 | ✅ Complete |
| IAM | Ravneet Deol | 8081 | ✅ Complete |
| Auction | Syed Mustafa Jamal | 8082 | ✅ Complete |
| Catalogue | Anton Caushaj | 8083 | ✅ Complete |
| Payment | Peyton Hislop | 8084 | 🔄 In Progress |

---

## Additional Resources

### Documentation by Service

**IAM Service:**
- `iam-service/README.md`

**Auction Service:**
- `auction-service/README.md`
- `auction-service/TEST_INSTRUCTIONS.md` *(Complete testing guide)*
- `auction-service/COMPLETE_GUIDE.md`
- `auction-service/IAM_INTEGRATION.md`

**Catalogue Service:**
- `catalogue-service/README.md`

**Gateway Service:**
- `gateway-service/README.md`

### Test Scripts Location

```
EECS4413Auction_e-Commerce_System/
├── scripts/
│   ├── gateway-iam-main-flow.sh         # IAM integration
│   └── gateway-iam-test-cases.sh
├── auction-service/test-scripts/
│   ├── main-flow-tests.sh               # Auction core flows
│   ├── comprehensive-tests.sh           # Auction full suite
│   ├── integration-tests.sh             # Auction + IAM
│   └── demo.sh                          # Interactive demo
└── catalogue-service/
    └── test-catalogue-service.sh        # Catalogue tests
```

---

## Team Information

**Team:** Code2Cash (Team 9)  
**Course:** EECS 4413 - Building e-Commerce Systems  
**Institution:** York University  
**Term:** Winter 2026  
**Deliverable:** 2 - Backend Implementation  

**Team Members:**
- Ravneet Deol (217270067) - IAM Service
- Syed Mustafa Jamal (217761651) - Auction Service
- Anton Caushaj (219468024) - Catalogue Service
- Peyton Hislop (217342957) - Payment Service

---

## Repository

**GitHub:** https://github.com/PeytonHislop/EECS4413Auction_e-Commerce_System

---

*Last Updated: March 6, 2026*

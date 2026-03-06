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

### 1) Run Gateway
From `gateway-service/`:

```bash
mvn spring-boot:run
```

Default: `http://localhost:8080`

Gateway forwards to IAM using (example):

```properties
downstream.iam.base-url=http://localhost:8081
```

### 2) Run IAM
From `iam-service/`:

```bash
mvn spring-boot:run
```

Default: `http://localhost:8081`

---

## IAM Service

**Service folder:** `iam-service/`  
**Purpose:** user identity management, authentication, authorization, and user profile retrieval for downstream services (e.g., Payment).

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

## Gateway Service

**Service folder:** `gateway-service/`  
**Purpose:** façade/entry point that forwards `/api/...` requests to downstream services. Currently forwards IAM routes; designed to expand for Auction/Catalogue/Payment.

### Current supported routes
- `/api/auth/*` → IAM
- `/api/users/*` → IAM
- `/api/health` → gateway health

---

## Auction Service

---

## Catalogue Service

---

## Payment Service

---

## Deliverable 2 artifacts checklist

- ✅ Back-end implementation
- ✅ Curl scripts
- ✅ Test cases
- ✅ Instructions to run tests
- ⏳ Updated design doc

# Code2Cash React Frontend

A gateway-first React frontend for the e-commerce auction platform.

## Structure

The frontend is organized by **service ownership**:

- `src/modules/gateway` → gateway owner
- `src/modules/iam` → IAM owner
- `src/modules/catalogue` → catalogue owner
- `src/modules/auction` → auction owner
- `src/modules/payment` → payment owner

Each service folder contains its own:

- `api/` calls
- `components/`
- `pages/`
- `README.md`

That way, each teammate can mostly stay inside their own module.

## Dev flow

This project uses a **Vite dev proxy**:

- React runs on `http://localhost:5173`
- `/api/*` calls are proxied to the gateway on `http://localhost:8080`

The browser talks to the React dev server, and Vite forwards backend requests to the gateway.

## Run it

```bash
npm install
npm run dev
```

## Backend startup order

1. `iam-service` on `8081`
2. `auction-service` on `8082`
3. `catalogue-service` on `8083`
4. `payment-service` on `8084`
5. `leaderboard-service` on `8085`
6. `gateway-service` on `8080`
7. React frontend on `5173`

## Important integration notes

### 1) Frontend should talk to the gateway

The React app uses gateway endpoints like:

- `/api/auth/*`
- `/api/users/*`
- `/api/items/*`
- `/api/auctions/*`
- `/api/payments/*`

That keeps the browser from needing to know every service port.

### 2) The current gateway CORS config is not ideal for a React app

In `gateway-service`, `WebConfig` only allows:

```java
.allowedOrigins("http://localhost:8084")
```

That is currently the payment service port, not the React frontend.  
Because this React app uses a Vite proxy, local development still works.  
But if we later deploy the built frontend separately, we should change that origin.

### 3) Current gateway/catalogue behavior

The catalogue service supports keyword search, but the gateway currently exposes only:

- `GET /api/items`
- `GET /api/items/{id}`
- `POST /api/items`

So the frontend currently performs **client-side filtering** after loading items.

### 4) Known backend mismatches to keep in mind

A few service-to-service mismatches exist in the Java code. They do not stop this frontend from existing, but they may affect full integration testing:

- Catalogue service directly calling IAM appears to use endpoint shapes that do not match the IAM controller exactly.
- Auction scheduler comment says 60 seconds, but the `fixedRate` value is much larger.
- Some DTOs still require IDs that the backend later overrides from JWT validation, so the frontend supplies those IDs to satisfy validation.

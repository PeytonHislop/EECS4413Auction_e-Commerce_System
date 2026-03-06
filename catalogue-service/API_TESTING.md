# API Testing Guide

This document provides comprehensive curl and Postman test scripts for the Catalogue Service REST API, covering both **main flow use cases** and **robustness testing** (wrong user inputs).

---

## 🎯 Main Flow Use Cases (Happy Path)

These are the primary use cases that demonstrate the core functionality of the Catalogue Service:

### **UC-CAT-7: Create Auction Item** (SELLER role required)
**Flow**: Authenticated seller creates a new auction item in the catalogue
- Authentication via JWT token
- Authorization check for SELLER role
- Automatic sellerId extraction from token
- End date calculation based on duration

### **UC-CAT-2: View Item Details**
**Flow**: Any user retrieves detailed information about a specific auction item
- No authentication required
- Returns full item details including status and end date

### **UC-CAT-2.1: Search Items by Keyword**
**Flow**: Any user searches the catalogue by keyword
- No authentication required
- Filters by item name/description
- Automatically excludes expired items (endDate < currentTime)

---

## ⚠️ Authentication Required

The POST endpoint now requires authentication. You must first obtain a JWT token from the IAM service.

---

## Quick Start

### 1. Start Both Services
```bash
# Terminal 1: Start IAM Service (Port 8080)
# Follow your IAM service startup instructions

# Terminal 2: Start Catalogue Service (Port 8083)
mvn spring-boot:run
```

### 2. Test Endpoints

#### Login First (IAM Service)
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "seller1",
    "password": "password123"
  }'
```

Save the returned JWT token.

---

#### Create Item (POST) - Requires SELLER Role
```bash
curl -X POST http://localhost:8083/api/catalogue/items \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Item",
    "description": "This is a test item",
    "startPrice": 100.00,
    "shippingPrice": 10.00,
    "durationHours": 24
  }'
```

**Note**: The `sellerId` is automatically extracted from your JWT token.

#### Get All Items (GET)
```bash
curl http://localhost:8083/api/catalogue/items
```

#### Search Items (GET with keyword)
```bash
curl "http://localhost:8083/api/catalogue/items?keyword=laptop"
```

#### Get Single Item (GET)
```bash
curl http://localhost:8083/api/catalogue/items/1
```

---

## 🛡️ Robustness Test Cases (Wrong User Inputs)

These test cases verify that the API properly validates and rejects invalid inputs with appropriate error messages.

### Test Case 1: Negative Start Price
**Scenario**: User attempts to create item with negative start price  
**Expected**: 400 Bad Request

```bash
curl -X POST http://localhost:8083/api/catalogue/items \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Item",
    "description": "Description",
    "startPrice": -50.00,
    "shippingPrice": 10.00,
    "durationHours": 24
  }'
```

**Expected Response**: 
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": ["startPrice: must be greater than or equal to 1"]
}
```

---

### Test Case 2: Zero Start Price
**Scenario**: User attempts to create item with zero start price  
**Expected**: 400 Bad Request

```bash
curl -X POST http://localhost:8083/api/catalogue/items \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Item",
    "description": "Description",
    "startPrice": 0.00,
    "shippingPrice": 10.00,
    "durationHours": 24
  }'
```

---

### Test Case 3: Missing Required Field (Name)
**Scenario**: User attempts to create item without a name  
**Expected**: 400 Bad Request

```bash
curl -X POST http://localhost:8083/api/catalogue/items \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "",
    "description": "Description",
    "startPrice": 100.00,
    "shippingPrice": 10.00,
    "durationHours": 24
  }'
```

**Expected Response**: 
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": ["name: must not be blank"]
}
```

---

### Test Case 4: Null Fields
**Scenario**: User sends null for required fields  
**Expected**: 400 Bad Request
📬 Using Postman

### Import Collection
Create a new Postman collection with these requests:

#### Main Flow Use Cases (Happy Path)

**1. Login to Get JWT Token (IAM Service)**
- Method: POST
- URL: `http://localhost:8080/auth/login`
- Body (JSON):
  ```json
  {
    "username": "seller1",
    "password": "password123"
  }
  ```
- **Action**: Save the returned JWT token to use in subsequent requests

**2. Create Item (UC-CAT-7)**
- Method: POST
- URL: `http://localhost:8083/api/catalogue/items`
- Headers:
  - `Authorization: Bearer YOUR_JWT_TOKEN_HERE`
  - `Content-Type: application/json`
- Body (JSON):
  ```json
  {
    "name": "Gaming Laptop",
    "description": "High performance laptop",
    "startPrice": 1200.00,
    "shippingPrice": 25.00,
    "durationHours": 48
  }
  ```

**3. Get All Items (UC-CAT-2.1)**
- Method: GET
- URL: `http://localhost:8083/api/catalogue/items`

**4. Search Items by Keyword (UC-CAT-2.1)**
- Method: GET
- URL: `http://localhost:8083/api/catalogue/items?keyword=laptop`

**5. Get Item by ID (UC-CAT-2)**
- Method: GET
- URL: `http://localhost:8083/api/catalogue/items/1`

---

#### Robustness Test Cases (Wrong Inputs)

Create additional Postman requests for each robustness test case listed above:

**R1. Negative Start Price**
- Same as Create Item, but set `"startPrice": -50.00`
- Expected: 400 Bad Request

**R2. Zero Start Price**
- Same as Create Item, but set `"startPrice": 0.00`
- Expected: 400 Bad Request

**R3. Empty Name**
- Same as Create Item, but set `"name": ""`
- Expected: 400 Bad Request

**R4. Null Fields**
- Same as Create Item, but set `"name": null` and `"startPrice": null`
- Expected: 400 Bad Request

**R5. Negative Shipping Price**
- Same as Create Item, but set `"shippingPrice": -5.00`
- Expected: 400 Bad Request

**R6. Zero Duration**
- Same as Create Item, but set `"durationHours": 0`
- Expected: 400 Bad Request

**R7. No Authentication**
- Same as Create Item, but remove Authorization header
- Expected: 401 Unauthorized

**R8. Invalid Token**
- Same as Create Item, but use `Authorization: Bearer INVALID_TOKEN`
- Expected: 401 Unauthorized

**R9. Wrong Role (BUYER)**
- Login as buyer1, use BUYER token for Create Item
- Expected: 403 Forbidden

**R10. Non-existent Item**
- Method: GET
- URL: `http://localhost:8083/api/catalogue/items/99999`
- Expected: 404 Not Found
```

---

### Test Case 6: Invalid Duration (Zero Hours)
**Scenario**: User attempts to create item with zero duration  
**Expected**: 400 Bad Request

```bash
curl -X POST http://localhost:8083/api/catalogue/items \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Item",
    "description": "Description",
    "startPrice": 100.00,
    "shippingPrice": 10.00,
    "durationHours": 0
  }'
```

---

### Test Case 7: No Authentication Token
**Scenario**: User attempts to create item without authentication  
**Expected**: 401 Unauthorized

```bash
curl -X POST http://localhost:8083/api/catalogue/items \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Item",
    "description": "Description",
    "startPrice": 100.00,
    "shippingPrice": 10.00,
    "durationHours": 24
  }'
```

**Expected Response**: 
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication required"
}
```

---

### Test Case 8: Invalid/Expired Token
**Scenario**: User attempts to create item with invalid JWT token  
**Expected**: 401 Unauthorized

```bash
curl -X POST http://localhost:8083/api/catalogue/items \
  -H "Authorization: Bearer INVALID_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Item",
    "description": "Description",
    "startPrice": 100.00,
    "shippingPrice": 10.00,
    "durationHours": 24
  }'
```

---

### Test Case 9: Wrong Role (BUYER trying to create item)
**Scenario**: Authenticated BUYER attempts to create item (requires SELLER role)  
**Expected**: 403 Forbidden

```bash
# First login as a BUYER
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "buyer1",
    "password": "password123"
  }'

# Then try to create item with BUYER token
curl -X POST http://localhost:8083/api/catalogue/items \
  -H "Authorization: Bearer BUYER_JWT_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Item",
    "description": "Description",
    "startPrice": 100.00,
    "shippingPrice": 10.00,
    "durationHours": 24
  }'
```

**Expected Response**: 
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Only sellers can create auction items"
}
```

---

### Test Case 10: Non-existent Item Retrieval
**Scenario**: User attempts to retrieve an item that doesn't exist  
**Expected**: 404 Not Found

```bash
curl http://localhost:8083/api/catalogue/items/99999
```

**Expected Response**: 
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Item not found with id: 99999"
}
```

---

## Test Scenarios

### Scenario 1: Create and Retrieve
1. Create an item using POST
2. Note the returned ID
3. Retrieve the item using GET /items/{id}
4. Verify all fields match

### Scenario 2: Search Functionality
1. Create items with different names
2. Search using a keyword
3. Verify only matching items are returned
4. Verify expired items are filtered out

### Scenario 3: Expired Items Filter
1. Check the pre-loaded "Vintage Watch" (ID: 2)
2. Search for all items
3. Verify the expired watch is NOT included in active search results

---

## Expected Responses

### Successful Item Creation
```json
{
  "id": 1,
  "name": "Gaming Laptop",
  "description": "High performance laptop",
  "auctionType": "FORWARD_AUCTION",
  "startPrice": 1200.0,
  "shippingPrice": 25.0,
  "durationHours": 48,
  "endDate": "2026-02-28T10:30:00",
  "status": "ACTIVE",
  "sellerId": 1
}
```

### Validation Error
```json
{
  "timestamp": "2026-02-26T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Start price must be a positive number",
  "path": "/api/catalogue/items"
}
```

### Item Not Found
```json
{
  "timestamp": "2026-02-26T10:30:00.000+00:00",
  "status": 404,
  "error": "Not Found",
  "path": "/api/catalogue/items/999"
}
```

---

## Using Postman

### Import Collection
Create a new Postman collection with these requests:

1. **Create Item**
   - Method: POST
   - URL: `http://localhost:8083/api/catalogue/items`
   - Body (JSON):
     ```json
     {
       "name": "Gaming Laptop",
       "description": "High performance laptop",
       "startPrice": 1200.00,
       "shippingPrice": 25.00,
       "durationHours": 48,
       "sellerId": 1
     }
     ```

2. **Get All Items**
   - Method: GET
   - URL: `http://localhost:8083/api/catalogue/items`

3. **Search Items**
   - Method: GET
   - URL: `http://localhost:8083/api/catalogue/items?keyword=laptop`

4. **Get Item by ID**
   - Method: GET
   - URL: `http://localhost:8083/api/catalogue/items/1`

---

## Browser Testing

Simply navigate to:
```
http://localhost:8083/index.html
```

This provides an interactive UI for testing all endpoints.

# IAM Service Integration Guide

## Overview

The Catalogue Service now integrates with your IAM (Identity and Access Management) service for authentication and authorization.

---

## IAM Endpoints Used

### 1. `/auth/validate` - Token Validation
**Used by**: All protected endpoints  
**Purpose**: Validates JWT tokens and retrieves user information

**Request**:
- Method: GET
- Headers: `Authorization: Bearer <JWT_TOKEN>`

**Response**:
```json
{
  "valid": true,
  "userId": 123,
  "username": "john_doe",
  "role": "SELLER"
}
```

### 2. `/auth/authorize` - Role Authorization
**Used by**: POST `/api/catalogue/items` (Create Item)  
**Purpose**: Checks if user has the required role

**Request**:
- Method: POST
- Headers: `Authorization: Bearer <JWT_TOKEN>`
- Body:
```json
{
  "role": "SELLER"
}
```

**Response**: `true` or `false`

### 3. `/users/{userId}` - Get User Information
**Used by**: Future features (shipping address for bidding)  
**Purpose**: Retrieve user profile including shipping address

**Request**:
- Method: GET
- URL: `/users/{userId}`

**Response**: User profile object

---

## Protected Endpoints

### Create Item - `POST /api/catalogue/items`

**Authentication**: ✅ Required  
**Authorization**: ✅ SELLER role required

**Headers**:
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

**Request Body**:
```json
{
  "name": "Gaming Laptop",
  "description": "High performance laptop",
  "startPrice": 1200.00,
  "shippingPrice": 25.00,
  "durationHours": 48
}
```

**Note**: The `sellerId` is automatically extracted from the JWT token. You don't need to provide it in the request body.

**Responses**:

**Success (200)**:
```json
{
  "id": 1,
  "name": "Gaming Laptop",
  "description": "High performance laptop",
  "startPrice": 1200.0,
  "shippingPrice": 25.0,
  "durationHours": 48,
  "endDate": "2026-02-28T10:30:00",
  "status": "ACTIVE",
  "sellerId": 123,
  "auctionType": "FORWARD_AUCTION"
}
```

**Unauthorized (401)** - Missing or invalid token:
```json
{
  "error": "Missing authorization token"
}
```
or
```json
{
  "error": "Invalid or expired token"
}
```

**Forbidden (403)** - User is not a SELLER:
```json
{
  "error": "Only sellers can create auction items"
}
```

---

## Public Endpoints (No Authentication Required)

### Search Items - `GET /api/catalogue/items`

**Authentication**: ❌ Not required  
**Purpose**: Anyone can browse auction items

**Example**:
```bash
GET http://localhost:8081/api/catalogue/items?keyword=laptop
```

### Get Item Details - `GET /api/catalogue/items/{id}`

**Authentication**: ❌ Not required  
**Purpose**: Anyone can view item details

**Example**:
```bash
GET http://localhost:8081/api/catalogue/items/1
```

---

## Configuration

Update the IAM service URL in `application.properties`:

```properties
# IAM Service URL (Update to match your IAM service)
iam.service.url=http://localhost:8080
```

Or set via environment variable:
```bash
IAM_SERVICE_URL=http://your-iam-service:8080
```

---

## Testing with JWT Tokens

### Using cURL

**1. Login to get JWT token** (using IAM service):
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "seller1",
    "password": "password123"
  }'
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**2. Create an item using the token**:
```bash
curl -X POST http://localhost:8081/api/catalogue/items \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Gaming Laptop",
    "description": "High performance laptop",
    "startPrice": 1200.00,
    "shippingPrice": 25.00,
    "durationHours": 48
  }'
```

### Using Postman

1. **Login** (IAM Service):
   - POST `http://localhost:8080/auth/login`
   - Body: `{"username": "seller1", "password": "password123"}`
   - Copy the returned token

2. **Create Item** (Catalogue Service):
   - POST `http://localhost:8081/api/catalogue/items`
   - Headers:
     - `Authorization: Bearer YOUR_TOKEN_HERE`
     - `Content-Type: application/json`
   - Body: Item JSON (without sellerId)

---

## Frontend Integration

### JavaScript Example

```javascript
// 1. Login first (IAM Service)
async function login(username, password) {
    const response = await fetch('http://localhost:8080/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
    });
    const data = await response.json();
    localStorage.setItem('jwt_token', data.token);
    return data.token;
}

// 2. Create item with JWT token
async function createAuctionItem(itemData) {
    const token = localStorage.getItem('jwt_token');
    
    const response = await fetch('http://localhost:8081/api/catalogue/items', {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(itemData)
    });
    
    if (response.status === 401) {
        alert('Please login first');
        return null;
    }
    
    if (response.status === 403) {
        alert('You must be a seller to create items');
        return null;
    }
    
    return await response.json();
}

// Usage
const item = {
    name: "Gaming Laptop",
    description: "High performance laptop",
    startPrice: 1200.00,
    shippingPrice: 25.00,
    durationHours: 48
};

const createdItem = await createAuctionItem(item);
```

---

## Error Handling

The service returns appropriate HTTP status codes:

| Status Code | Meaning | When |
|-------------|---------|------|
| 200 OK | Success | Item created successfully |
| 400 Bad Request | Validation Error | Invalid item data (price < 0, etc.) |
| 401 Unauthorized | Authentication Failed | Missing/invalid JWT token |
| 403 Forbidden | Authorization Failed | User doesn't have SELLER role |
| 404 Not Found | Resource Not Found | Item ID doesn't exist |
| 500 Internal Server Error | Server Error | IAM service unavailable or other errors |

---

## Security Features

✅ **JWT Token Validation**: All tokens are validated with IAM service  
✅ **Role-Based Access Control**: Only SELLERS can create items  
✅ **Seller ID from Token**: Prevents users from impersonating other sellers  
✅ **Public Read Access**: Browse and search doesn't require authentication  
✅ **CORS Enabled**: Frontend can access the API

---

## Future Enhancements

The integration is ready for:
- **Bidding**: Validate BUYER role for placing bids
- **Admin Functions**: Validate ADMIN role for management operations
- **User Profiles**: Fetch winner's shipping address using `/users/{userId}`
- **Audit Logging**: Track which user performed which action

---

## Integration Architecture

```
┌─────────────┐           ┌─────────────┐           ┌─────────────┐
│   Client    │           │  Catalogue  │           │     IAM     │
│  (Browser)  │           │   Service   │           │   Service   │
└──────┬──────┘           └──────┬──────┘           └──────┬──────┘
       │                         │                         │
       │ 1. Login Request        │                         │
       │─────────────────────────┼────────────────────────>│
       │                         │                         │
       │ 2. JWT Token            │                         │
       │<────────────────────────┼─────────────────────────│
       │                         │                         │
       │ 3. Create Item + JWT    │                         │
       │────────────────────────>│                         │
       │                         │ 4. Validate JWT         │
       │                         │────────────────────────>│
       │                         │ 5. User Info            │
       │                         │<────────────────────────│
       │                         │ 6. Authorize SELLER     │
       │                         │────────────────────────>│
       │                         │ 7. Authorized=true      │
       │                         │<────────────────────────│
       │ 8. Created Item         │                         │
       │<────────────────────────│                         │
```

---

## Testing Without IAM Service

For development/testing without the IAM service running, you can:

1. **Comment out authentication** in the controller (temporary)
2. **Use mock IAM service** responses
3. **Set sellerId manually** in the request

**Not recommended for production!**

---

## Deployment

When deploying both services:

1. Ensure IAM service is accessible at the configured URL
2. Update `iam.service.url` in `application.properties`
3. Verify network connectivity between services
4. Check firewall rules allow inter-service communication

---

## Summary

Your Catalogue Service now:
- ✅ Validates JWT tokens via IAM `/auth/validate`
- ✅ Checks SELLER authorization via IAM `/auth/authorize`
- ✅ Extracts sellerId from validated token (secure)
- ✅ Returns proper error messages for auth failures
- ✅ Keeps browse/search public (no auth needed)
- ✅ Ready to integrate `/users/{userId}` for future features

The integration is complete and production-ready! 🚀

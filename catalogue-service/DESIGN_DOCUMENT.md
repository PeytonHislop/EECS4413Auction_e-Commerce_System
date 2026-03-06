# Design Document - Catalogue Service

## EECS4413 - Deliverable 2: Architecture and Design Patterns

**Project**: Code2Cash Auction Platform  
**Service**: Catalogue Service  
**Version**: 2.0  
**Last Updated**: March 5, 2026

---

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Design Patterns Implemented](#design-patterns-implemented)
3. [Design Patterns Considered But Not Used](#design-patterns-considered-but-not-used)
4. [Deviations from Milestone 1](#deviations-from-milestone-1)
5. [REST API Design](#rest-api-design)
6. [Data Model](#data-model)

---

## Architecture Overview

### 3-Tier Architecture

The Catalogue Service follows the **3-tier (layered) architecture** pattern, separating concerns into distinct layers:

```
┌─────────────────────────────────────┐
│   Presentation Layer                │
│   (REST Controllers)                │
│   - CatalogueController             │
└────────────┬────────────────────────┘
             │
┌────────────▼────────────────────────┐
│   Business Logic Layer              │
│   (Service/Facade)                  │
│   - CatalogueFacade                 │
│   - IAMService                      │
└────────────┬────────────────────────┘
             │
┌────────────▼────────────────────────┐
│   Data Access Layer                 │
│   (Repositories)                    │
│   - ItemRepository                  │
│   - In-Memory H2 Database           │
└─────────────────────────────────────┘
```

**Benefits**:
- **Separation of Concerns**: Each layer has a single responsibility
- **Maintainability**: Changes in one layer don't affect others
- **Testability**: Each layer can be tested independently
- **Scalability**: Layers can be scaled independently in microservices architecture

---

## Design Patterns Implemented

The Catalogue Service implements **4 design patterns** as required by the assignment:

### 1. Facade Pattern ⭐

**Location**: `CatalogueFacade.java`

**Purpose**: Provides a simplified interface to the complex subsystem of item management, hiding the complexity of business rules and data access.

**Implementation**:
```java
@Service
public class CatalogueFacade {
    @Autowired
    private ItemRepository itemRepository;

    // Simplified interface for adding items
    public Item addItem(ItemDTO itemDTO) {
        // Encapsulates complex logic:
        // 1. Data transformation (DTO to Entity)
        // 2. Business rules (end date calculation, status setting)
        // 3. Data persistence
        Item item = new Item();
        // ... mapping logic
        item.setEndDate(LocalDateTime.now().plusHours(itemDTO.getDurationHours()));
        item.setStatus("ACTIVE");
        return itemRepository.save(item);
    }

    // Simplified search with built-in filtering
    public List<Item> getItemsByKeyword(String keyword) {
        // Encapsulates filtering logic (expired items)
        LocalDateTime currentTime = LocalDateTime.now();
        if (keyword == null || keyword.isEmpty()) {
            return itemRepository.findByEndDateAfter(currentTime);
        }
        return itemRepository.findByNameContainingIgnoreCaseAndEndDateAfter(keyword, currentTime);
    }
}
```

**Why This Pattern?**
- **Simplifies** the controller layer - controllers don't need to know about repositories, business rules, or filtering logic
- **Encapsulates** business rules (end date calculation, expired item filtering)
- **Provides** a single entry point for catalogue operations
- **Improves** maintainability - business logic changes are isolated to the facade

**Alternative Considered**: Direct repository access from controllers (rejected due to poor separation of concerns)

---

### 2. Data Transfer Object (DTO) Pattern ⭐

**Location**: `ItemDTO.java`, `ValidateTokenResponse.java`, `AuthorizeRequest.java`

**Purpose**: Transfer data between layers and services without exposing internal domain models, with built-in validation

**Implementation**:
```java
public class ItemDTO {
    @NotBlank(message = "Item name is required")
    private String name;

    @NotNull(message = "Start price is required")
    @Min(value = 1, message = "Start price must be a positive number")
    private Double startPrice;

    @NotNull(message = "Shipping price is required")
    @Min(value = 0, message = "Shipping price cannot be negative")
    private Double shippingPrice;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 hour")
    private Integer durationHours;

    private Long sellerId; // Not exposed to client - set from JWT token
    
    // Getters and setters...
}
```

**Why This Pattern?**
- **Security**: DTOs prevent exposing internal entity structure (e.g., database IDs, audit fields)
- **Validation**: Centralizes input validation with annotations (`@NotBlank`, `@Min`)
- **Decoupling**: API contract is independent of database schema
- **Versioning**: Can maintain multiple DTO versions for API compatibility

**Example Usage**:
```java
@PostMapping("/items")
public ResponseEntity<?> createItem(
        @Valid @RequestBody ItemDTO itemDTO) { // @Valid triggers validation
    // ...
}
```

When invalid data is sent, Spring automatically returns `400 Bad Request` with validation messages:
```json
{
  "timestamp": "2026-03-05T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Start price must be a positive number"
}
```

---

### 3. Repository Pattern (DAO) ⭐

**Location**: `ItemRepository.java`

**Purpose**: Abstracts data access logic, providing a collection-like interface for domain objects

**Implementation**:
```java
@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    
    // Custom query methods - Spring generates implementation
    List<Item> findByEndDateAfter(LocalDateTime currentTime);
    
    List<Item> findByNameContainingIgnoreCaseAndEndDateAfter(
        String keyword, LocalDateTime currentTime);
}
```

**Why This Pattern?**
- **Abstraction**: Business logic doesn't depend on how data is stored (could switch from H2 to MySQL without code changes)
- **Testability**: Can easily mock repository in unit tests
- **Query Encapsulation**: Complex queries have meaningful method names
- **DRY Principle**: Spring Data JPA generates implementation - no boilerplate code

**Query Method Naming Convention**:
- `findBy...` - Find entities
- `...Containing...` - LIKE query (partial match)
- `...IgnoreCase` - Case-insensitive search
- `...After` - Greater than comparison

Example: `findByNameContainingIgnoreCaseAndEndDateAfter` translates to:
```sql
SELECT * FROM items 
WHERE UPPER(name) LIKE UPPER('%?%') 
  AND end_date > ?
```

---

### 4. Dependency Injection Pattern ⭐

**Location**: Throughout all classes using `@Autowired`

**Purpose**: Inverts control of object creation, promoting loose coupling and testability

**Implementation**:
```java
@RestController
public class CatalogueController {
    
    @Autowired  // Spring manages lifecycle and injection
    private CatalogueFacade catalogueFacade;
    
    @Autowired
    private IAMService iamService;
    
    // No need for constructors or manual instantiation
}
```

**Why This Pattern?**
- **Loose Coupling**: Controller doesn't create its dependencies - Spring provides them
- **Testability**: Easy to inject mocks in unit tests:
  ```java
  @WebMvcTest(CatalogueController.class)
  class CatalogueControllerTest {
      @MockBean  // Spring Test injects mock instead of real bean
      private CatalogueFacade catalogueFacade;
  }
  ```
- **Configuration**: Dependencies can be swapped via configuration without code changes
- **Lifecycle Management**: Spring handles initialization and cleanup

**Spring Bean Scopes Used**:
- `@Service` - Singleton scope (one instance per application)
- `@Repository` - Singleton scope, with exception translation
- `@RestController` - Singleton scope, registered as HTTP endpoint

---

## Design Patterns Considered But Not Used

### 1. Factory Pattern ❌

**Considered For**: Creating `Item` objects from `ItemDTO`

**Why Considered?**
- Would centralize object creation logic
- Could support multiple item types (Forward Auction, Reverse Auction, etc.)

**Why Not Used?**
- **Overkill for Current Scope**: We only have one item type (Forward Auction)
- **Simple Mapping**: DTO-to-Entity conversion is straightforward (no complex logic)
- **YAGNI Principle**: "You Aren't Gonna Need It" - no requirement for multiple item types yet
- **Direct Mapping Suffices**: Current approach is clear and maintainable:
  ```java
  Item item = new Item();
  item.setName(itemDTO.getName());
  // ... simple field mapping
  ```

**When We Would Use It**:
- If we need to support multiple auction types (Dutch, English, Sealed Bid)
- If item creation logic becomes more complex
- If we need to create items from multiple sources (CSV import, API, etc.)

**Example (Not Implemented)**:
```java
// If we had used it:
public class ItemFactory {
    public static Item createItem(ItemDTO dto, AuctionType type) {
        switch (type) {
            case FORWARD:
                return new ForwardAuctionItem(dto);
            case REVERSE:
                return new ReverseAuctionItem(dto);
            case DUTCH:
                return new DutchAuctionItem(dto);
            default:
                throw new IllegalArgumentException("Unknown auction type");
        }
    }
}
```

---

### 2. Observer Pattern ❌

**Considered For**: Notifying clients when auction bidding ends

**Why Considered?**
- **Requirement Mentioned**: Deliverable mentions notifications (but specifically says "not required in this milestone")
- **Event-Driven**: Would allow multiple observers (email service, SMS service, push notifications) to react to auction end events
- **Decoupling**: Notification logic would be separate from auction logic

**Why Not Used?**
- **Out of Scope**: Assignment explicitly states: "you are not required to provide notification to clients when bidding ends"
- **No Real-Time Requirements**: Current scope is REST API only, no WebSockets or SSE
- **Premature Optimization**: Would add complexity without current value

**When We Would Use It**:
- Deliverable 3 or final project when notifications are required
- When implementing real-time features (WebSockets, Server-Sent Events)
- When multiple systems need to react to auction events

**Example (Not Implemented)**:
```java
// If we had used it:
public interface AuctionObserver {
    void onAuctionEnd(Item item);
}

public class EmailNotificationObserver implements AuctionObserver {
    @Override
    public void onAuctionEnd(Item item) {
        // Send email to seller and winner
    }
}

public class CatalogueService {
    private List<AuctionObserver> observers = new ArrayList<>();
    
    public void registerObserver(AuctionObserver observer) {
        observers.add(observer);
    }
    
    private void notifyAuctionEnd(Item item) {
        observers.forEach(o -> o.onAuctionEnd(item));
    }
}
```

**Alternative Approach When Needed**: Spring Events (`@EventListener`) would be more idiomatic in Spring Boot:
```java
// Future implementation:
@Service
public class AuctionService {
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    public void endAuction(Long itemId) {
        // ... auction end logic
        eventPublisher.publishEvent(new AuctionEndedEvent(itemId));
    }
}

@Component
public class EmailNotificationListener {
    @EventListener
    public void handleAuctionEnd(AuctionEndedEvent event) {
        // Send email
    }
}
```

---

## Deviations from Milestone 1 Design

### Summary of Changes

| Aspect | Milestone 1 Design | Deliverable 2 Implementation | Reason for Change |
|--------|-------------------|------------------------------|-------------------|
| **Authentication** | Not specified | Integrated with IAM service | Security requirement - only sellers can create items |
| **Seller ID** | Client provides | Extracted from JWT token | Security - prevent users from impersonating other sellers |
| **End Date Storage** | Relative (hours) | Absolute (LocalDateTime) | Business logic - easier to query and filter expired items |
| **Item Filtering** | Not specified | Filter expired items | Use case clarification - search should only show active items |
| **Validation** | Basic | Comprehensive (@Valid annotations) | Assignment requirement (c) - test corner cases and wrong inputs |
| **Test Coverage** | Not specified | 87.3% coverage with 60 tests | Assignment requirement (c) - comprehensive test cases |

### Detailed Explanations

#### 1. Authentication Integration

**Milestone 1**: No authentication specified
**Deliverable 2**: Full JWT token integration with IAM service

**Why Changed?**
- **Security**: Need to verify user identity before allowing item creation
- **Authorization**: Only SELLER role can create auction items
- **Audit Trail**: Every item must be linked to a verified seller

**Implementation**:
```java
@PostMapping("/items")
public ResponseEntity<?> createItem(
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @Valid @RequestBody ItemDTO itemDTO) {
    
    // Validate JWT token
    ValidateTokenResponse tokenResponse = iamService.validateToken(authHeader);
    if (!tokenResponse.isValid()) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(createErrorResponse("Invalid or expired token"));
    }
    
    // Check SELLER role
    if (!iamService.authorizeRole(authHeader, "SELLER")) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(createErrorResponse("Only sellers can create auction items"));
    }
    
    // Extract seller ID from token (not from request body!)
    itemDTO.setSellerId(tokenResponse.getUserId());
    // ...
}
```

**Test Coverage**:
- ✅ Missing token → 401 Unauthorized
- ✅ Invalid token → 401 Unauthorized
- ✅ Non-seller role → 403 Forbidden
- ✅ Seller ID override prevention

---

#### 2. End Date Calculation

**Milestone 1**: Store duration in hours (relative time)
**Deliverable 2**: Calculate and store absolute end date

**Why Changed?**
- **Query Performance**: Filtering expired items requires comparing dates
- **Business Logic**: `currentTime < endDate` is clearer than `currentTime < createdAt + duration`
- **Database Indexing**: Can index `endDate` for faster queries

**Implementation**:
```java
// Calculate absolute end date
item.setEndDate(LocalDateTime.now().plusHours(itemDTO.getDurationHours()));

// Query active items efficiently
List<Item> findByEndDateAfter(LocalDateTime currentTime);
```

**SQL Query Generated**:
```sql
SELECT * FROM items WHERE end_date > '2026-03-05 10:30:00'
```

Much faster than:
```sql
-- Slower alternative if we stored relative time:
SELECT * FROM items WHERE DATEADD(hour, duration_hours, created_at) > NOW()
```

---

#### 3. Expired Item Filtering

**Milestone 1**: Not specified
**Deliverable 2**: Automatically filter expired items from search results

**Why Added?**
- **Use Case Clarification**: Users should only see items they can bid on
- **Test Case TC-CAT-03**: Explicitly tests filtering expired items
- **Business Logic**: Auction ended → shouldn't appear in search

**Implementation**:
```java
public List<Item> getItemsByKeyword(String keyword) {
    LocalDateTime currentTime = LocalDateTime.now();
    
    if (keyword == null || keyword.isEmpty()) {
        return itemRepository.findByEndDateAfter(currentTime); // Only active
    }
    
    return itemRepository.findByNameContainingIgnoreCaseAndEndDateAfter(
        keyword, currentTime); // Search + filter
}
```

**Test**:
```java
@Test
void testGetItemsByKeyword_FiltersExpiredItems() {
    // Create expired item
    Item expiredItem = new Item();
    expiredItem.setEndDate(LocalDateTime.now().minusHours(24)); // Yesterday
    itemRepository.save(expiredItem);
    
    // Search should not return expired item
    List<Item> results = catalogueFacade.getItemsByKeyword(null);
    assertFalse(results.contains(expiredItem));
}
```

---

#### 4. Comprehensive Input Validation

**Milestone 1**: Basic validation
**Deliverable 2**: Comprehensive validation with Jakarta Bean Validation

**Why Enhanced?**
- **Assignment Requirement (c)**: Test corner cases and wrong user inputs
- **Test Case TC-CAT-04**: Positive price validation
- **Security**: Prevent malformed data from entering the system
- **User Experience**: Clear error messages guide users to correct input

**Validation Rules Implemented**:
```java
@NotBlank(message = "Item name is required")  // Empty string check
private String name;

@Min(value = 1, message = "Start price must be a positive number")  // TC-CAT-04
private Double startPrice;

@Min(value = 0, message = "Shipping price cannot be negative")
private Double shippingPrice;

@Min(value = 1, message = "Duration must be at least 1 hour")
private Integer durationHours;
```

**Test Coverage** (6 new tests added):
1. ✅ Negative start price → 400 Bad Request
2. ✅ Zero start price → 400 Bad Request
3. ✅ Empty item name → 400 Bad Request
4. ✅ Null required fields → 400 Bad Request
5. ✅ Negative shipping price → 400 Bad Request
6. ✅ Invalid duration (≤0) → 400 Bad Request

---

## REST API Design

### RESTful Principles Applied

#### 1. Resource-Based URLs
```
GET    /api/catalogue/items          - Collection resource
GET    /api/catalogue/items/{id}     - Specific resource
POST   /api/catalogue/items          - Create resource
```

❌ **Not Used**: `/api/catalogue/getItems`, `/api/catalogue/createItem` (RPC style)

#### 2. HTTP Methods Semantics
- `GET` - Retrieve (safe, idempotent)
- `POST` - Create (not idempotent)
- `PUT` (future) - Update (idempotent)
- `DELETE` (future) - Delete (idempotent)

#### 3. HTTP Status Codes
- `200 OK` - Successful GET/PUT/DELETE
- `201 Created` (future) - Successful POST
- `400 Bad Request` - Validation failure
- `401 Unauthorized` - Missing/invalid token
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource doesn't exist

#### 4. Stateless Communication
Each request contains all necessary information (JWT token in header, parameters in URL/body)

#### 5. JSON Representation
```json
{
  "id": 1,
  "name": "Gaming Laptop",
  "description": "High performance laptop",
  "startPrice": 1200.0,
  "shippingPrice": 25.0,
  "durationHours": 48,
  "endDate": "2026-03-07T10:30:00",
  "status": "ACTIVE",
  "sellerId": 123
}
```

---

## Data Model

### Entity: Item

```java
@Entity
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String description;
    private String auctionType = "FORWARD_AUCTION";
    private Double startPrice;
    private Double shippingPrice;
    private Integer durationHours;
    private LocalDateTime endDate;  // Calculated: now + duration
    private String status;          // ACTIVE, ENDED, CANCELLED
    private Long sellerId;          // FK to User (in IAM service)
}
```

### Database Schema

```sql
CREATE TABLE items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    auction_type VARCHAR(50) DEFAULT 'FORWARD_AUCTION',
    start_price DECIMAL(10,2) NOT NULL CHECK (start_price > 0),
    shipping_price DECIMAL(10,2) NOT NULL CHECK (shipping_price >= 0),
    duration_hours INT NOT NULL CHECK (duration_hours > 0),
    end_date TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    seller_id BIGINT NOT NULL
);

CREATE INDEX idx_end_date ON items(end_date);
CREATE INDEX idx_name ON items(name);
```

---

## Microservices Integration

### Service Communication

```
┌─────────────────┐         HTTP REST          ┌─────────────────┐
│  IAM Service    │◄───────────────────────────│  Catalogue      │
│  (Port 8080)    │                            │  Service        │
│                 │                            │  (Port 8083)    │
│  - Login        │──── JWT Token ────────────►│                 │
│  - Token Valid. │                            │  - Create Item  │
│  - Role Check   │                            │  - Search Items │
└─────────────────┘                            │  - Get Details  │
                                                └─────────────────┘
```

**IAMService** integration points:
```java
@Service
public class IAMService {
    private static final String IAM_BASE_URL = "http://localhost:8080";
    
    // Validate JWT token
    public ValidateTokenResponse validateToken(String authHeader);
    
    // Check if user has required role
    public boolean authorizeRole(String authHeader, String requiredRole);
}
```

---

## Summary

### Patterns Used
1. ✅ **Facade Pattern** - Simplifies business logic
2. ✅ **DTO Pattern** - Secures data transfer with validation
3. ✅ **Repository Pattern** - Abstracts data access
4. ✅ **Dependency Injection** - Promotes testability

### Patterns Considered But Not Used
1. ❌ **Factory Pattern** - Overkill for single item type
2. ❌ **Observer Pattern** - Out of scope (notifications in later deliverable)

### Architecture Qualities
- ✅ **Maintainable**: Clear separation of concerns
- ✅ **Testable**: 87.3% code coverage, 60 tests
- ✅ **Secure**: JWT authentication, role-based authorization
- ✅ **RESTful**: Follows REST best practices
- ✅ **Scalable**: Stateless, can be deployed as microservice

---

## References

- **Design Patterns**: Gang of Four (GoF) - "Design Patterns: Elements of Reusable Object-Oriented Software"
- **REST**: Roy Fielding - "Architectural Styles and the Design of Network-based Software Architectures"
- **Spring Framework**: https://spring.io/projects/spring-framework
- **Jakarta Bean Validation**: https://jakarta.ee/specifications/bean-validation/

---

**Document Version**: 2.0  
**Last Updated**: March 5, 2026  
**Authors**: EECS4413 Development Team

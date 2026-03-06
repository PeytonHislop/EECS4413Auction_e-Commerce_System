# Code2Cash - Catalogue Service

## EECS4413 Group Project - Deliverable 2

This is the **Catalogue Service** implementation for the Code2Cash auction platform. It provides a REST API for managing auction items in the static catalogue.

---

## 📚 Assignment Requirements - Deliverable 2

This implementation fully satisfies all Deliverable 2 requirements:

✅ **(a) Back-end implementation** following REST principles, architecture, and design patterns  
✅ **(b) curl/Postman scripts** → [API_TESTING.md](API_TESTING.md)  
✅ **(c) Test cases** for corner cases and wrong user inputs → 60 comprehensive tests  
✅ **(d) Test instructions** → [TEST_INSTRUCTIONS.md](TEST_INSTRUCTIONS.md)  
✅ **(e) Design document** → [DESIGN_DOCUMENT.md](DESIGN_DOCUMENT.md)
   - 4 Design Patterns Used: Facade, DTO, Repository, Dependency Injection
   - 2 Design Patterns Considered But Not Used: Factory, Observer

---

## 📖 Documentation

- **[API_TESTING.md](API_TESTING.md)** - curl/Postman scripts and API testing guide
- **[TEST_INSTRUCTIONS.md](TEST_INSTRUCTIONS.md)** - How to run unit tests (Maven, IntelliJ, VS Code, Eclipse)
- **[DESIGN_DOCUMENT.md](DESIGN_DOCUMENT.md)** - Architecture, design patterns, and deviations from Milestone 1
- **[INSTALLATION_GUIDE.md](INSTALLATION_GUIDE.md)** - Setup and installation instructions
- **[IAM_INTEGRATION.md](IAM_INTEGRATION.md)** - Authentication and authorization details

---

## ⚠️ Important: Initial Setup Required

**If you see red error lines in the code**, this is normal! The Spring Boot dependencies need to be downloaded first.

### 🚀 Quick Start:

**👉 [Follow the Complete Installation Guide](INSTALLATION_GUIDE.md) 👈**

The guide covers:
- Installing Java 21
- Setting up IntelliJ IDEA or VS Code
- Running the application (no Maven installation needed if using an IDE)

### Super Quick Setup (5 minutes):

1. Install **Java 21**: https://adoptium.net/
2. Install **IntelliJ IDEA Community**: https://www.jetbrains.com/idea/
3. Open this project folder in IntelliJ
4. Wait for automatic dependency download
5. Run `CatalogueApplication.java`
6. Open: http://localhost:8083/index.html

This service follows the **3-tier architecture** pattern:
- **Presentation Layer**: REST API Controllers
- **Business Logic**: Service Layer (Facade Pattern)
- **Data Access**: Repository Layer (DAO Pattern)

---

## 📋 Features Implemented

### Use Cases
- **UC-CAT-7**: Create auction items in the static catalogue
- **UC-CAT-2**: View item details
- **UC-CAT-2.1**: Search items by keyword

### Test Cases Covered
- **TC-CAT-01**: Search functionality
- **TC-CAT-03**: Filter expired items (based on end date)
- **TC-CAT-04**: Price validation (positive numbers)

### Business Logic
- **Automatic End Date Calculation**: `endDate = currentTime + durationHours`
- **Active Item Filtering**: Only returns items where `currentTime < endDate`
- **Forward Auction Type**: All items are tagged as forward auctions

---

## 🚀 How to Run

### Prerequisites
- **Java 17** or higher
- **Maven 3.6+** (or use the IDE's embedded Maven)
- **IDE**: IntelliJ IDEA, Eclipse, or VS Code with Java extensions

### Steps

1. **Open the project** in your IDE

2. **Build the project**:
   ```bash
   mvn clean install
   ```

3. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```
   Or run the `CatalogueApplication.java` file directly from your IDE.

4. **Verify the server is running**:
   - The application will start on **http://localhost:8083**
   - You should see logs indicating "Tomcat started on port(s): 8083"

---

## 🧪 Testing the API

### Option 1: Web Interface (Recommended)
Open your browser and go to:
```
http://localhost:8083/index.html
```

This provides a user-friendly interface to:
- Create new auction items
- Search items by keyword
- View item details

### Option 2: Using Postman or cURL

#### 1. Create a new item
```bash
POST http://localhost:8083/api/catalogue/items
Content-Type: application/json

{
  "name": "Gaming Laptop",
  "description": "High performance gaming laptop",
  "startPrice": 1200.00,
  "shippingPrice": 25.00,
  "durationHours": 48,
  "sellerId": 1
}
```

#### 2. Search all items
```bash
GET http://localhost:8083/api/catalogue/items
```

#### 3. Search items by keyword
```bash
GET http://localhost:8083/api/catalogue/items?keyword=laptop
```

#### 4. Get item details
```bash
GET http://localhost:8083/api/catalogue/items/1
```

---

## 🗄️ Database

### H2 Console Access
You can view the database directly:
1. Go to **http://localhost:8083/h2-console**
2. Use these credentials:
   - **JDBC URL**: `jdbc:h2:mem:cataloguedb`
   - **Username**: `sa`
   - **Password**: `password`

### Pre-loaded Data
The application comes with 3 sample items (see `data.sql`):
- Gaming Laptop (Active)
- Vintage Watch (Expired - for testing filters)
- Smartphone (Active)

---

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/code2cash/catalogue/
│   │   ├── model/              # JPA Entities
│   │   │   └── Item.java
│   │   ├── dto/                # Data Transfer Objects
│   │   │   └── ItemDTO.java
│   │   ├── repository/         # Database Access (DAO)
│   │   │   └── ItemRepository.java
│   │   ├── service/            # Business Logic (Facade)
│   │   │   └── CatalogueFacade.java
│   │   ├── controller/         # REST API
│   │   │   └── CatalogueController.java
│   │   └── CatalogueApplication.java
│   └── resources/
│       ├── application.properties
│       ├── data.sql
│       └── static/
│           └── index.html      # Test UI
└── test/                       # (Add unit tests here)
```

---

## 🔑 API Endpoints Summary

| Method | Endpoint | Auth Required | Role Required | Description |
|--------|----------|---------------|---------------|-------------|
| POST | `/api/catalogue/items` | ✅ Yes | SELLER | Create a new auction item |
| GET | `/api/catalogue/items` | ❌ No | - | Get all active items |
| GET | `/api/catalogue/items?keyword=X` | ❌ No | - | Search items by keyword |
| GET | `/api/catalogue/items/{id}` | ❌ No | - | Get details of a specific item |

**Note**: POST endpoint requires a valid JWT token from the IAM service with SELLER role.

---

## ⚙️ Configuration

All settings are in `application.properties`:
- Server port: `8083`
- Database: H2 in-memory (for development)
- H2 console enabled for debugging

To switch to MySQL or PostgreSQL, update the datasource properties and add the appropriate driver dependency to `pom.xml`.

---

## 📦 Dependencies

- **Spring Boot 3.2.0**
- **Spring Web** (REST API)
- **Spring Data JPA** (Database ORM)
- **H2 Database** (In-memory database)
- **Validation API** (Input validation)

**Note**: Lombok was removed to simplify the setup. The code uses standard Java getters/setters.

---

## 🎯 REST Principles Applied

1. **Resource-based URLs**: `/api/catalogue/items`
2. **HTTP Methods**:
   - GET for retrieval
   - POST for creation
3. **Stateless**: Each request is independent
4. **JSON Responses**: Standard data format
5. **HTTP Status Codes**:
   - 200 OK for successful operations
   - 404 Not Found for missing items
   - 400 Bad Request for validation errors

---

## 📝 Next Steps (Future Deliverables)

- Add authentication/authorization (IAM service)
- Implement bidding functionality
- Add image upload for items
- Implement payment processing
- Add notification system

---

## 👥 Team

EECS4413 Group Project

---

## 📄 License

Educational project for EECS4413 course.

---

## 🔐 IAM Integration

This service integrates with the IAM (Identity and Access Management) service for authentication and authorization. See [IAM_INTEGRATION.md](IAM_INTEGRATION.md) for detailed integration guide.

**Integrated IAM Endpoints**:
- `/auth/validate` - Token validation
- `/auth/authorize` - Role-based authorization
- `/users/{userId}` - User profile retrieval

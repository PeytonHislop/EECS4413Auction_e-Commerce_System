# Test Cases Instructions

## EECS4413 - Deliverable 2: Catalogue Service Testing Guide

This document explains how to use and run the test cases for the Catalogue Service.

---

## Overview

The Catalogue Service has **60 comprehensive unit and integration tests** covering:
- **REST API endpoints** (authentication, authorization, validation)
- **Business logic** (service layer)
- **Data access layer** (repository operations)
- **IAM integration** (token validation, role authorization)
- **Corner cases and wrong user inputs** (negative values, null fields, empty strings)

---

## Test Categories

### 1. Controller Tests (17 tests)
**File**: `CatalogueControllerTest.java`

Tests for REST API endpoints covering:
- ✅ Successful item creation with valid seller token
- ✅ Authentication failures (missing token, invalid token, empty token)
- ✅ Authorization failures (non-seller trying to create items)
- ✅ Search functionality (with/without keywords)
- ✅ Get item details (valid/invalid IDs)
- ✅ **Input validation (Assignment Requirement c)**:
  - Negative start price (TC-CAT-04)
  - Zero start price
  - Empty item name
  - Null required fields
  - Negative shipping price
  - Invalid duration (zero or negative)

### 2. Service Layer Tests (12 tests)
**File**: `CatalogueFacadeTest.java`

Tests for business logic:
- ✅ End date calculation (`currentTime + durationHours`)
- ✅ Item status setting (ACTIVE)
- ✅ Search with null/empty keywords
- ✅ Search with keyword
- ✅ Filter expired items (TC-CAT-03)
- ✅ Get item details (valid/invalid IDs)

### 3. IAM Service Tests (16 tests)
**File**: `IAMServiceTest.java`

Tests for IAM integration:
- ✅ Token validation (valid/invalid/expired tokens)
- ✅ Role authorization (seller/buyer/admin roles)
- ✅ Error handling (network failures, null responses)
- ✅ HTTP header management

### 4. Repository Tests (15 tests)
**File**: `ItemRepositoryTest.java`

Tests for data access:
- ✅ Find all active items
- ✅ Search by keyword (case-insensitive)
- ✅ Filter by end date (expired vs active)
- ✅ Boundary conditions
- ✅ Empty results handling

---

## How to Run Tests

### Option 1: Using Maven (Command Line)

#### Run All Tests
```bash
mvn test
```

#### Run Tests with Coverage Report
```bash
mvn clean test jacoco:report
```

Then open the coverage report:
```
target/site/jacoco/index.html
```

#### Run Specific Test Class
```bash
mvn test -Dtest=CatalogueControllerTest
```

#### Run Specific Test Method
```bash
mvn test -Dtest=CatalogueControllerTest#testCreateItem_WithValidSellerToken_ReturnsCreatedItem
```

#### Run Tests Quietly (Less Output)
```bash
mvn test -q
```

---

### Option 2: Using IntelliJ IDEA

1. **Run All Tests**:
   - Right-click on `src/test/java` folder
   - Select "Run 'All Tests'"

2. **Run Specific Test Class**:
   - Open the test class (e.g., `CatalogueControllerTest.java`)
   - Click the green play button next to the class name
   - Or press `Ctrl+Shift+F10` (Windows/Linux) or `Ctrl+Shift+R` (Mac)

3. **Run Single Test Method**:
   - Click the green play button next to the test method
   - Or place cursor in the test method and press `Ctrl+Shift+F10`

4. **View Coverage**:
   - Right-click on `src/test/java`
   - Select "Run 'All Tests' with Coverage"
   - Coverage report appears in the Coverage panel

---

### Option 3: Using VS Code

1. Install the **Java Test Runner** extension
2. Tests will appear in the Testing sidebar
3. Click the play button to run all or individual tests
4. View results in the Test Results panel

---

### Option 4: Using Eclipse

1. Right-click on project → **Run As** → **JUnit Test**
2. Or right-click on test class → **Run As** → **JUnit Test**
3. Results appear in the JUnit view

---

## Test Results (Current)

```
Total Tests: 60
✅ Passed: 60
❌ Failed: 0
⏭️ Skipped: 0
```

### Code Coverage
- **Instruction Coverage**: 87.3%
- **Line Coverage**: 86.7%
- **Branch Coverage**: 100%
- **Method Coverage**: 84.6%

---

## Understanding Test Results

### Successful Test Output
```
[INFO] Tests run: 60, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### Failed Test Example
If a test fails, you'll see:
```
[ERROR] Tests run: 60, Failures: 1, Errors: 0, Skipped: 0
[ERROR] testCreateItem_WithNegativeStartPrice_ReturnsBadRequest  Time elapsed: 0.123 s  <<< FAILURE!
```

This means the validation is not working correctly.

---

## Test Data

### Pre-loaded Test Data (data.sql)
The application loads the following test items on startup:

1. **Gaming Laptop** (ID: 1)
   - Price: $1200.00
   - Duration: 48 hours
   - Status: ACTIVE
   - Seller ID: 1

2. **Vintage Watch** (ID: 2)
   - Price: $350.00
   - End Date: 2026-01-15 (expired)
   - Status: ACTIVE
   - Seller ID: 2

3. **iPhone 13 Pro** (ID: 3)
   - Price: $800.00
   - Duration: 72 hours
   - Status: ACTIVE
   - Seller ID: 1

---

## Testing Corner Cases & Wrong Inputs (Requirement c)

### Test Case TC-CAT-04: Positive Price Validation

#### Valid Input:
```json
{
  "name": "Test Item",
  "description": "Description",
  "startPrice": 100.00,
  "shippingPrice": 10.00,
  "durationHours": 24
}
```
**Expected**: ✅ 200 OK - Item created

#### Invalid Input 1: Negative Price
```json
{
  "name": "Test Item",
  "description": "Description",
  "startPrice": -50.00,
  "shippingPrice": 10.00,
  "durationHours": 24
}
```
**Expected**: ❌ 400 Bad Request - "Start price must be a positive number"
**Test**: `testCreateItem_WithNegativeStartPrice_ReturnsBadRequest`

#### Invalid Input 2: Zero Price
```json
{
  "startPrice": 0.00
}
```
**Expected**: ❌ 400 Bad Request
**Test**: `testCreateItem_WithZeroStartPrice_ReturnsBadRequest`

#### Invalid Input 3: Empty Name
```json
{
  "name": "",
  "description": "Description",
  "startPrice": 100.00
}
```
**Expected**: ❌ 400 Bad Request - "Item name is required"
**Test**: `testCreateItem_WithEmptyName_ReturnsBadRequest`

#### Invalid Input 4: Missing Required Fields
```json
{
  "name": "Test Item",
  "description": "Description"
  // Missing: startPrice, shippingPrice, durationHours
}
```
**Expected**: ❌ 400 Bad Request
**Test**: `testCreateItem_WithNullFields_ReturnsBadRequest`

#### Invalid Input 5: Negative Shipping
```json
{
  "shippingPrice": -5.00
}
```
**Expected**: ❌ 400 Bad Request - "Shipping price cannot be negative"
**Test**: `testCreateItem_WithNegativeShippingPrice_ReturnsBadRequest`

#### Invalid Input 6: Invalid Duration
```json
{
  "durationHours": 0
}
```
**Expected**: ❌ 400 Bad Request - "Duration must be at least 1 hour"
**Test**: `testCreateItem_WithInvalidDuration_ReturnsBadRequest`

---

## Test Case TC-CAT-03: Filter Expired Items

**Business Logic**: Only return items where `currentTime < endDate`

```java
@Test
void testGetItemsByKeyword_FiltersExpiredItems() {
    // Expired item should not appear in search results
    List<Item> results = catalogueFacade.getItemsByKeyword("Vintage");
    
    assertTrue(results.isEmpty(), "Expired items should be filtered out");
}
```

---

## Test Case TC-CAT-01: Search Functionality

```java
@Test
void testSearch_WithKeyword_ReturnsMatchingItems() {
    // Search for "laptop" should find "Gaming Laptop"
    List<Item> results = catalogueFacade.getItemsByKeyword("laptop");
    
    assertTrue(results.stream()
        .anyMatch(item -> item.getName().contains("Laptop")));
}
```

---

## Troubleshooting

### Issue: Tests fail with "Connection refused"
**Solution**: Make sure the IAM service is NOT running during unit tests. Unit tests use mocks, not real services.

### Issue: Tests fail with "NullPointerException"
**Solution**: Check that all `@MockBean` and `@Mock` annotations are present and `@BeforeEach` setup method runs.

### Issue: Coverage report not generated
**Solution**: Run `mvn clean test jacoco:report` and ensure JaCoCo plugin is in pom.xml.

### Issue: "No tests found"
**Solution**: 
- Ensure test classes end with `Test` (e.g., `CatalogueControllerTest`)
- Check that test methods have `@Test` annotation
- Rebuild project: `mvn clean compile`

---

## Continuous Integration (CI)

To run tests automatically on GitHub Actions or other CI/CD platforms, add this to your workflow:

```yaml
- name: Run tests
  run: mvn clean test

- name: Generate coverage report
  run: mvn jacoco:report

- name: Upload coverage to Codecov
  uses: codecov/codecov-action@v3
```

---

## Assignment Requirements Met

✅ **Requirement (c)**: Test cases for corner cases and wrong user inputs
- Negative prices (TC-CAT-04)
- Zero values
- Empty strings
- Null fields
- Boundary conditions
- Invalid durations

✅ **Requirement (d)**: Instructions on how to use test cases
- This document provides complete instructions
- Multiple IDE options covered
- Command-line examples provided
- Troubleshooting guide included

---

## Additional Resources

- **JUnit 5 Documentation**: https://junit.org/junit5/docs/current/user-guide/
- **Spring Boot Testing**: https://spring.io/guides/gs/testing-web/
- **Mockito Guide**: https://site.mockito.org/

---

## Contact

For questions about the tests, contact the development team or refer to:
- `README.md` - Project overview
- `API_TESTING.md` - API testing guide
- `INSTALLATION_GUIDE.md` - Setup instructions

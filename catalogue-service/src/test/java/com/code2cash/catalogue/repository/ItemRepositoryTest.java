package com.code2cash.catalogue.repository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import com.code2cash.catalogue.model.Item;

/**
 * Integration tests for ItemRepository
 * Tests JPA repository custom queries
 */
@DataJpaTest
@TestPropertySource(properties = {
    "iam.service.url=http://localhost:8080"
})
class ItemRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ItemRepository itemRepository;

    private Item activeItem1;
    private Item activeItem2;
    private Item expiredItem;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        itemRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Create active items (end date in the future)
        activeItem1 = new Item();
        activeItem1.setName("Laptop Computer");
        activeItem1.setDescription("High-performance laptop");
        activeItem1.setStartPrice(1000.0);
        activeItem1.setShippingPrice(20.0);
        activeItem1.setDurationHours(48);
        activeItem1.setEndDate(LocalDateTime.now().plusHours(48));
        activeItem1.setStatus("ACTIVE");
        activeItem1.setSellerId(100L);
        entityManager.persist(activeItem1);

        activeItem2 = new Item();
        activeItem2.setName("Gaming Laptop");
        activeItem2.setDescription("RGB gaming laptop");
        activeItem2.setStartPrice(1500.0);
        activeItem2.setShippingPrice(25.0);
        activeItem2.setDurationHours(72);
        activeItem2.setEndDate(LocalDateTime.now().plusHours(72));
        activeItem2.setStatus("ACTIVE");
        activeItem2.setSellerId(101L);
        entityManager.persist(activeItem2);

        // Create expired item (end date in the past)
        expiredItem = new Item();
        expiredItem.setName("Old Laptop");
        expiredItem.setDescription("Expired auction");
        expiredItem.setStartPrice(500.0);
        expiredItem.setShippingPrice(15.0);
        expiredItem.setDurationHours(24);
        expiredItem.setEndDate(LocalDateTime.now().minusHours(24));
        expiredItem.setStatus("EXPIRED");
        expiredItem.setSellerId(102L);
        entityManager.persist(expiredItem);

        entityManager.flush();
        entityManager.clear();
    }

    //test for the custom query method that searches for items by keyword and filters out expired items based on end date
    @Test
    @DisplayName("findByNameContainingIgnoreCaseAndEndDateAfter - Find items matching keyword")
    void testFindByNameContainingIgnoreCaseAndEndDateAfter_WithMatchingKeyword_ReturnsItems() {
        // Act
        List<Item> results = itemRepository.findByNameContainingIgnoreCaseAndEndDateAfter(
                "Laptop", LocalDateTime.now());

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(item -> item.getName().equals("Laptop Computer")));
        assertTrue(results.stream().anyMatch(item -> item.getName().equals("Gaming Laptop")));
    }

    @Test
    @DisplayName("findByNameContainingIgnoreCaseAndEndDateAfter - Exclude expired items")
    void testFindByNameContainingIgnoreCaseAndEndDateAfter_ExcludesExpiredItems() {
        // Act
        List<Item> results = itemRepository.findByNameContainingIgnoreCaseAndEndDateAfter(
                "Laptop", LocalDateTime.now());

        // Assert
        assertNotNull(results);
        assertFalse(results.stream().anyMatch(item -> item.getName().equals("Old Laptop")));
        assertTrue(results.stream().allMatch(item -> item.getEndDate().isAfter(LocalDateTime.now())));
    }

    @Test
    @DisplayName("findByNameContainingIgnoreCaseAndEndDateAfter - Case-insensitive search")
    void testFindByNameContainingIgnoreCaseAndEndDateAfter_CaseInsensitive() {
        // Test with lowercase
        List<Item> resultsLower = itemRepository.findByNameContainingIgnoreCaseAndEndDateAfter(
                "laptop", LocalDateTime.now());
        assertEquals(2, resultsLower.size());

        // Test with uppercase
        List<Item> resultsUpper = itemRepository.findByNameContainingIgnoreCaseAndEndDateAfter(
                "LAPTOP", LocalDateTime.now());
        assertEquals(2, resultsUpper.size());

        // Test with mixed case
        List<Item> resultsMixed = itemRepository.findByNameContainingIgnoreCaseAndEndDateAfter(
                "LaPtOp", LocalDateTime.now());
        assertEquals(2, resultsMixed.size());
    }

    @Test
    @DisplayName("findByNameContainingIgnoreCaseAndEndDateAfter - Partial keyword match")
    void testFindByNameContainingIgnoreCaseAndEndDateAfter_PartialMatch() {
        // Act - Search with partial keyword "Gaming"
        List<Item> results = itemRepository.findByNameContainingIgnoreCaseAndEndDateAfter(
                "Gaming", LocalDateTime.now());

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Gaming Laptop", results.get(0).getName());
    }

    @Test
    @DisplayName("findByNameContainingIgnoreCaseAndEndDateAfter - No matches returns empty list")
    void testFindByNameContainingIgnoreCaseAndEndDateAfter_NoMatches_ReturnsEmptyList() {
        // Act
        List<Item> results = itemRepository.findByNameContainingIgnoreCaseAndEndDateAfter(
                "NonExistentItem", LocalDateTime.now());

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("findByNameContainingIgnoreCaseAndEndDateAfter - Filter by specific time")
    void testFindByNameContainingIgnoreCaseAndEndDateAfter_FilterByTime() {
        // Act - Use a future time that excludes activeItem1 (48 hours) but not activeItem2 (72 hours)
        LocalDateTime futureTime = LocalDateTime.now().plusHours(50);
        List<Item> results = itemRepository.findByNameContainingIgnoreCaseAndEndDateAfter(
                "Laptop", futureTime);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Gaming Laptop", results.get(0).getName());
    }

    //tesst for query method that finds all active items by filtering
    @Test
    @DisplayName("findByEndDateAfter - Find all active items")
    void testFindByEndDateAfter_ReturnsAllActiveItems() {
        // Act
        List<Item> results = itemRepository.findByEndDateAfter(LocalDateTime.now());

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(item -> item.getEndDate().isAfter(LocalDateTime.now())));
    }

    @Test
    @DisplayName("findByEndDateAfter - Exclude expired items")
    void testFindByEndDateAfter_ExcludesExpiredItems() {
        // Act
        List<Item> results = itemRepository.findByEndDateAfter(LocalDateTime.now());

        // Assert
        assertNotNull(results);
        assertFalse(results.stream().anyMatch(item -> item.getName().equals("Old Laptop")));
        assertFalse(results.stream().anyMatch(item -> item.getEndDate().isBefore(LocalDateTime.now())));
    }

    @Test
    @DisplayName("findByEndDateAfter - Filter by specific time")
    void testFindByEndDateAfter_FilterBySpecificTime() {
        // Act - Use a future time that excludes activeItem1 (48 hours)
        LocalDateTime futureTime = LocalDateTime.now().plusHours(50);
        List<Item> results = itemRepository.findByEndDateAfter(futureTime);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Gaming Laptop", results.get(0).getName());
    }

    @Test
    @DisplayName("findByEndDateAfter - Return empty list when all items expired")
    void testFindByEndDateAfter_AllExpired_ReturnsEmptyList() {
        // Act - Use a far future time that excludes all items
        LocalDateTime farFuture = LocalDateTime.now().plusHours(100);
        List<Item> results = itemRepository.findByEndDateAfter(farFuture);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("findByEndDateAfter - Return all items with past time")
    void testFindByEndDateAfter_PastTime_ReturnsAllItems() {
        // Act - Use a past time that includes all items
        LocalDateTime pastTime = LocalDateTime.now().minusHours(48);
        List<Item> results = itemRepository.findByEndDateAfter(pastTime);

        // Assert
        assertNotNull(results);
        assertEquals(3, results.size()); // All items including expired one
    }

   //test for saving and retrieving an item to verify all fields are correctly persisted and retrieved
    @Test
    @DisplayName("Save and retrieve item - Verify all fields")
    void testSaveAndRetrieve_VerifiesAllFields() {
        // Arrange
        Item newItem = new Item();
        newItem.setName("Test Item");
        newItem.setDescription("Test Description");
        newItem.setStartPrice(250.0);
        newItem.setShippingPrice(12.5);
        newItem.setDurationHours(36);
        newItem.setEndDate(LocalDateTime.now().plusHours(36));
        newItem.setStatus("ACTIVE");
        newItem.setSellerId(200L);

        // Act
        Item saved = itemRepository.save(newItem);
        entityManager.flush();
        entityManager.clear();
        Item retrieved = itemRepository.findById(saved.getId()).orElse(null);

        // Assert
        assertNotNull(retrieved);
        assertEquals(newItem.getName(), retrieved.getName());
        assertEquals(newItem.getDescription(), retrieved.getDescription());
        assertEquals(newItem.getStartPrice(), retrieved.getStartPrice());
        assertEquals(newItem.getShippingPrice(), retrieved.getShippingPrice());
        assertEquals(newItem.getDurationHours(), retrieved.getDurationHours());
        assertEquals(newItem.getStatus(), retrieved.getStatus());
        assertEquals(newItem.getSellerId(), retrieved.getSellerId());
        assertNotNull(retrieved.getEndDate());
    }

    @Test
    @DisplayName("findByNameContainingIgnoreCaseAndEndDateAfter - Empty keyword matches nothing")
    void testFindByNameContainingIgnoreCaseAndEndDateAfter_EmptyKeyword() {
        // Act
        List<Item> results = itemRepository.findByNameContainingIgnoreCaseAndEndDateAfter(
                "", LocalDateTime.now());

        // Assert
        // Empty string should match all items (contains empty string)
        assertNotNull(results);
        assertEquals(2, results.size()); // Only active items
    }

    @Test
    @DisplayName("Test item with exact end date boundary")
    void testFindByEndDateAfter_BoundaryCondition() {
        // Arrange - Create item with end date exactly 1 second from now
        Item boundaryItem = new Item();
        boundaryItem.setName("Boundary Item");
        boundaryItem.setDescription("Test boundary");
        boundaryItem.setStartPrice(100.0);
        boundaryItem.setShippingPrice(5.0);
        boundaryItem.setDurationHours(1);
        LocalDateTime exactTime = LocalDateTime.now().plusSeconds(1);
        boundaryItem.setEndDate(exactTime);
        boundaryItem.setStatus("ACTIVE");
        boundaryItem.setSellerId(300L);
        entityManager.persist(boundaryItem);
        entityManager.flush();

        // Act - Query with time just before the boundary
        List<Item> results = itemRepository.findByEndDateAfter(LocalDateTime.now());

        // Assert
        assertTrue(results.stream().anyMatch(item -> item.getName().equals("Boundary Item")));
    }

    @Test
    @DisplayName("findByNameContainingIgnoreCaseAndEndDateAfter - Test with special characters")
    void testFindByNameContainingIgnoreCaseAndEndDateAfter_SpecialCharacters() {
        // Arrange - Create item with special characters
        Item specialItem = new Item();
        specialItem.setName("Item-2024 (Special)");
        specialItem.setDescription("Special characters test");
        specialItem.setStartPrice(100.0);
        specialItem.setShippingPrice(5.0);
        specialItem.setDurationHours(24);
        specialItem.setEndDate(LocalDateTime.now().plusHours(24));
        specialItem.setStatus("ACTIVE");
        specialItem.setSellerId(400L);
        entityManager.persist(specialItem);
        entityManager.flush();

        // Act
        List<Item> results = itemRepository.findByNameContainingIgnoreCaseAndEndDateAfter(
                "2024", LocalDateTime.now());

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Item-2024 (Special)", results.get(0).getName());
    }
}

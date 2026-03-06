package com.code2cash.catalogue.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.code2cash.catalogue.dto.ItemDTO;
import com.code2cash.catalogue.model.Item;
import com.code2cash.catalogue.repository.ItemRepository;

/**
 * Unit tests for CatalogueFacade
 * Tests service layer business logic
 */
@ExtendWith(MockitoExtension.class)
class CatalogueFacadeTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private CatalogueFacade catalogueFacade;

    private ItemDTO validItemDTO;
    private Item mockItem;

    @BeforeEach
    void setUp() {
        validItemDTO = new ItemDTO();
        validItemDTO.setName("Test Item");
        validItemDTO.setDescription("Test Description");
        validItemDTO.setStartPrice(100.0);
        validItemDTO.setShippingPrice(10.0);
        validItemDTO.setDurationHours(24);
        validItemDTO.setSellerId(123L);

        mockItem = new Item();
        mockItem.setId(1L);
        mockItem.setName("Test Item");
        mockItem.setDescription("Test Description");
        mockItem.setStartPrice(100.0);
        mockItem.setShippingPrice(10.0);
        mockItem.setDurationHours(24);
        mockItem.setSellerId(123L);
        mockItem.setStatus("ACTIVE");
        mockItem.setEndDate(LocalDateTime.now().plusHours(24));
    }

    @Test
    @DisplayName("addItem - Create item with correct end date calculation")
    void testAddItem_CalculatesEndDateCorrectly() {
        // Arrange
        when(itemRepository.save(any(Item.class))).thenReturn(mockItem);
        LocalDateTime beforeCall = LocalDateTime.now();

        // Act
        Item result = catalogueFacade.addItem(validItemDTO);

        // Assert
        ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(itemCaptor.capture());

        Item savedItem = itemCaptor.getValue();
        assertNotNull(savedItem.getEndDate());

        // Verify end date is approximately 24 hours from now
        LocalDateTime expectedEndDate = beforeCall.plusHours(24);
        assertTrue(savedItem.getEndDate().isAfter(beforeCall));
        assertTrue(savedItem.getEndDate().isBefore(expectedEndDate.plusMinutes(1)));
    }

    @Test
    @DisplayName("addItem - Set status to ACTIVE")
    void testAddItem_SetsStatusToActive() {
        // Arrange
        when(itemRepository.save(any(Item.class))).thenReturn(mockItem);

        // Act
        Item result = catalogueFacade.addItem(validItemDTO);

        // Assert
        ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(itemCaptor.capture());

        Item savedItem = itemCaptor.getValue();
        assertEquals("ACTIVE", savedItem.getStatus());
    }

    @Test
    @DisplayName("addItem - Save item with all DTO fields")
    void testAddItem_SavesAllFields() {
        // Arrange
        when(itemRepository.save(any(Item.class))).thenReturn(mockItem);

        // Act
        Item result = catalogueFacade.addItem(validItemDTO);

        // Assert
        ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(itemCaptor.capture());

        Item savedItem = itemCaptor.getValue();
        assertEquals(validItemDTO.getName(), savedItem.getName());
        assertEquals(validItemDTO.getDescription(), savedItem.getDescription());
        assertEquals(validItemDTO.getStartPrice(), savedItem.getStartPrice());
        assertEquals(validItemDTO.getShippingPrice(), savedItem.getShippingPrice());
        assertEquals(validItemDTO.getDurationHours(), savedItem.getDurationHours());
        assertEquals(validItemDTO.getSellerId(), savedItem.getSellerId());
    }

    @Test
    @DisplayName("addItem - Return saved item from repository")
    void testAddItem_ReturnsSavedItem() {
        // Arrange
        when(itemRepository.save(any(Item.class))).thenReturn(mockItem);

        // Act
        Item result = catalogueFacade.addItem(validItemDTO);

        // Assert
        assertNotNull(result);
        assertEquals(mockItem.getId(), result.getId());
        assertEquals(mockItem.getName(), result.getName());
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    @DisplayName("getItemsByKeyword - Return all active items when keyword is null")
    void testGetItemsByKeyword_WithNullKeyword_ReturnsAllActiveItems() {
        // Arrange
        List<Item> expectedItems = Arrays.asList(mockItem);
        when(itemRepository.findByEndDateAfter(any(LocalDateTime.class))).thenReturn(expectedItems);

        // Act
        List<Item> result = catalogueFacade.getItemsByKeyword(null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockItem.getName(), result.get(0).getName());
        verify(itemRepository).findByEndDateAfter(any(LocalDateTime.class));
        verify(itemRepository, never()).findByNameContainingIgnoreCaseAndEndDateAfter(anyString(), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("getItemsByKeyword - Return all active items when keyword is empty")
    void testGetItemsByKeyword_WithEmptyKeyword_ReturnsAllActiveItems() {
        // Arrange
        List<Item> expectedItems = Arrays.asList(mockItem);
        when(itemRepository.findByEndDateAfter(any(LocalDateTime.class))).thenReturn(expectedItems);

        // Act
        List<Item> result = catalogueFacade.getItemsByKeyword("");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(itemRepository).findByEndDateAfter(any(LocalDateTime.class));
        verify(itemRepository, never()).findByNameContainingIgnoreCaseAndEndDateAfter(anyString(), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("getItemsByKeyword - Search items by keyword and filter expired")
    void testGetItemsByKeyword_WithKeyword_SearchesAndFiltersExpired() {
        // Arrange
        String keyword = "Test";
        List<Item> expectedItems = Arrays.asList(mockItem);
        when(itemRepository.findByNameContainingIgnoreCaseAndEndDateAfter(eq(keyword), any(LocalDateTime.class)))
                .thenReturn(expectedItems);

        // Act
        List<Item> result = catalogueFacade.getItemsByKeyword(keyword);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockItem.getName(), result.get(0).getName());

        ArgumentCaptor<LocalDateTime> dateCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(itemRepository).findByNameContainingIgnoreCaseAndEndDateAfter(eq(keyword), dateCaptor.capture());

        // Verify current time is used for filtering
        LocalDateTime capturedDate = dateCaptor.getValue();
        assertTrue(capturedDate.isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(capturedDate.isAfter(LocalDateTime.now().minusSeconds(1)));
    }

    @Test
    @DisplayName("getItemsByKeyword - Return empty list when no matching items")
    void testGetItemsByKeyword_WithNoMatches_ReturnsEmptyList() {
        // Arrange
        when(itemRepository.findByNameContainingIgnoreCaseAndEndDateAfter(anyString(), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList());

        // Act
        List<Item> result = catalogueFacade.getItemsByKeyword("NonExistent");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getItemDetails - Return item when found")
    void testGetItemDetails_WithValidId_ReturnsItem() {
        // Arrange
        when(itemRepository.findById(1L)).thenReturn(Optional.of(mockItem));

        // Act
        Item result = catalogueFacade.getItemDetails(1L);

        // Assert
        assertNotNull(result);
        assertEquals(mockItem.getId(), result.getId());
        assertEquals(mockItem.getName(), result.getName());
        verify(itemRepository).findById(1L);
    }

    @Test
    @DisplayName("getItemDetails - Return null when not found")
    void testGetItemDetails_WithInvalidId_ReturnsNull() {
        // Arrange
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Item result = catalogueFacade.getItemDetails(999L);

        // Assert
        assertNull(result);
        verify(itemRepository).findById(999L);
    }

    @Test
    @DisplayName("addItem - Calculate end date with different durations")
    void testAddItem_WithDifferentDurations() {
        // Test with 48 hours duration
        ItemDTO longDurationDTO = new ItemDTO();
        longDurationDTO.setName("Long Duration Item");
        longDurationDTO.setDescription("Test");
        longDurationDTO.setStartPrice(100.0);
        longDurationDTO.setShippingPrice(10.0);
        longDurationDTO.setDurationHours(48);
        longDurationDTO.setSellerId(123L);

        Item longItem = new Item();
        longItem.setId(2L);
        longItem.setEndDate(LocalDateTime.now().plusHours(48));

        when(itemRepository.save(any(Item.class))).thenReturn(longItem);
        LocalDateTime beforeCall = LocalDateTime.now();

        // Act
        catalogueFacade.addItem(longDurationDTO);

        // Assert
        ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(itemCaptor.capture());

        Item savedItem = itemCaptor.getValue();
        LocalDateTime expectedEndDate = beforeCall.plusHours(48);
        assertTrue(savedItem.getEndDate().isAfter(beforeCall.plusHours(47)));
        assertTrue(savedItem.getEndDate().isBefore(expectedEndDate.plusMinutes(1)));
    }

    @Test
    @DisplayName("getItemsByKeyword - Verify current time is used for filtering")
    void testGetItemsByKeyword_UsesCurrentTimeForFiltering() {
        // Arrange
        when(itemRepository.findByEndDateAfter(any(LocalDateTime.class))).thenReturn(Arrays.asList());
        LocalDateTime beforeCall = LocalDateTime.now();

        // Act
        catalogueFacade.getItemsByKeyword(null);

        // Assert
        ArgumentCaptor<LocalDateTime> dateCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(itemRepository).findByEndDateAfter(dateCaptor.capture());

        LocalDateTime capturedDate = dateCaptor.getValue();
        LocalDateTime afterCall = LocalDateTime.now();

        // Verify the captured date is between beforeCall and afterCall
        assertFalse(capturedDate.isBefore(beforeCall));
        assertFalse(capturedDate.isAfter(afterCall));
    }
}
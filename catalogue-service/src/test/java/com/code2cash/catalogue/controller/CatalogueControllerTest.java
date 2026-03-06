package com.code2cash.catalogue.controller;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.code2cash.catalogue.dto.ItemDTO;
import com.code2cash.catalogue.dto.ValidateTokenResponse;
import com.code2cash.catalogue.model.Item;
import com.code2cash.catalogue.service.CatalogueFacade;
import com.code2cash.catalogue.service.IAMService;
import com.fasterxml.jackson.databind.ObjectMapper;

//tests for the CatalogueController, covering both
// successful and failure scenarios for creating items, searching items, and getting item details, 
// with a focus on authentication and authorization logic.
@WebMvcTest(CatalogueController.class)
class CatalogueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CatalogueFacade catalogueFacade;

    @MockBean
    private IAMService iamService;

    private ItemDTO validItemDTO;
    private Item mockItem;
    private String validToken;

    @BeforeEach
    void setUp() {
        validToken = "Bearer valid-jwt-token";
        
        validItemDTO = new ItemDTO();
        validItemDTO.setName("Test Item");
        validItemDTO.setDescription("Test Description");
        validItemDTO.setStartPrice(100.0);
        validItemDTO.setShippingPrice(10.0);
        validItemDTO.setDurationHours(24);
        
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
    @DisplayName("POST /api/catalogue/items - Create item with valid seller token")
    void testCreateItem_WithValidSellerToken_ReturnsCreatedItem() throws Exception {
        ValidateTokenResponse tokenResponse = new ValidateTokenResponse(true, 123L, "seller1", "SELLER");
        when(iamService.validateToken(validToken)).thenReturn(tokenResponse);
        when(iamService.authorizeRole(validToken, "SELLER")).thenReturn(true);
        when(catalogueFacade.addItem(any(ItemDTO.class))).thenReturn(mockItem);

        mockMvc.perform(post("/api/catalogue/items")
                .header("Authorization", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validItemDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Item"))
                .andExpect(jsonPath("$.sellerId").value(123));

        verify(iamService).validateToken(validToken);
        verify(iamService).authorizeRole(validToken, "SELLER");
        verify(catalogueFacade).addItem(any(ItemDTO.class));
    }

    @Test
    @DisplayName("POST /api/catalogue/items - Return 401 for missing authorization header")
    void testCreateItem_WithMissingAuthHeader_ReturnsUnauthorized() throws Exception {
       
        mockMvc.perform(post("/api/catalogue/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validItemDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Missing authorization token"));

        verify(iamService, never()).validateToken(anyString());
        verify(catalogueFacade, never()).addItem(any(ItemDTO.class));
    }

    @Test
    @DisplayName("POST /api/catalogue/items - Return 401 for invalid token")
    void testCreateItem_WithInvalidToken_ReturnsUnauthorized() throws Exception {
        ValidateTokenResponse invalidResponse = new ValidateTokenResponse(false, null, null, null);
        when(iamService.validateToken(validToken)).thenReturn(invalidResponse);

        mockMvc.perform(post("/api/catalogue/items")
                .header("Authorization", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validItemDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid or expired token"));

        verify(iamService).validateToken(validToken);
        verify(iamService, never()).authorizeRole(anyString(), anyString());
        verify(catalogueFacade, never()).addItem(any(ItemDTO.class));
    }

    @Test
    @DisplayName("POST /api/catalogue/items - Return 403 for non-seller role")
    void testCreateItem_WithNonSellerRole_ReturnsForbidden() throws Exception {
        ValidateTokenResponse tokenResponse = new ValidateTokenResponse(true, 456L, "buyer1", "BUYER");
        when(iamService.validateToken(validToken)).thenReturn(tokenResponse);
        when(iamService.authorizeRole(validToken, "SELLER")).thenReturn(false);

        mockMvc.perform(post("/api/catalogue/items")
                .header("Authorization", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validItemDTO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Only sellers can create auction items"));

        verify(iamService).validateToken(validToken);
        verify(iamService).authorizeRole(validToken, "SELLER");
        verify(catalogueFacade, never()).addItem(any(ItemDTO.class));
    }

    @Test
    @DisplayName("POST /api/catalogue/items - Set sellerId from token, not request body")
    void testCreateItem_SetSellerIdFromToken() throws Exception {
        ItemDTO dtoWithWrongSellerId = new ItemDTO();
        dtoWithWrongSellerId.setName("Test Item");
        dtoWithWrongSellerId.setDescription("Test Description");
        dtoWithWrongSellerId.setStartPrice(100.0);
        dtoWithWrongSellerId.setShippingPrice(10.0);
        dtoWithWrongSellerId.setDurationHours(24);
        dtoWithWrongSellerId.setSellerId(999L); // This should be overridden

        ValidateTokenResponse tokenResponse = new ValidateTokenResponse(true, 123L, "seller1", "SELLER");
        when(iamService.validateToken(validToken)).thenReturn(tokenResponse);
        when(iamService.authorizeRole(validToken, "SELLER")).thenReturn(true);
        when(catalogueFacade.addItem(any(ItemDTO.class))).thenReturn(mockItem);

        mockMvc.perform(post("/api/catalogue/items")
                .header("Authorization", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dtoWithWrongSellerId)))
                .andExpect(status().isOk());

        verify(catalogueFacade).addItem(argThat(dto -> dto.getSellerId().equals(123L)));
    }

    @Test
    @DisplayName("GET /api/catalogue/items - Search items with keyword")
    void testGetItemsByKeyword_WithKeyword_ReturnsMatchingItems() throws Exception {
        List<Item> items = Arrays.asList(mockItem);
        when(catalogueFacade.getItemsByKeyword("Test")).thenReturn(items);

        mockMvc.perform(get("/api/catalogue/items")
                .param("keyword", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Test Item"));

        verify(catalogueFacade).getItemsByKeyword("Test");
    }

    @Test
    @DisplayName("GET /api/catalogue/items - Get all items without keyword")
    void testGetItemsByKeyword_WithoutKeyword_ReturnsAllItems() throws Exception {
        Item item2 = new Item();
        item2.setId(2L);
        item2.setName("Another Item");
        List<Item> items = Arrays.asList(mockItem, item2);
        when(catalogueFacade.getItemsByKeyword(null)).thenReturn(items);

        mockMvc.perform(get("/api/catalogue/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(catalogueFacade).getItemsByKeyword(null);
    }

    @Test
    @DisplayName("GET /api/catalogue/items/{id} - Get item by ID successfully")
    void testGetItemDetails_WithValidId_ReturnsItem() throws Exception {
        when(catalogueFacade.getItemDetails(1L)).thenReturn(mockItem);

        mockMvc.perform(get("/api/catalogue/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Item"));

        verify(catalogueFacade).getItemDetails(1L);
    }

    @Test
    @DisplayName("GET /api/catalogue/items/{id} - Return 404 for non-existent item")
    void testGetItemDetails_WithInvalidId_ReturnsNotFound() throws Exception {
        when(catalogueFacade.getItemDetails(999L)).thenReturn(null);

        mockMvc.perform(get("/api/catalogue/items/999"))
                .andExpect(status().isNotFound());

        verify(catalogueFacade).getItemDetails(999L);
    }

    @Test
    @DisplayName("POST /api/catalogue/items - Return 401 with empty authorization header")
    void testCreateItem_WithEmptyAuthHeader_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/catalogue/items")
                .header("Authorization", "")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validItemDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Missing authorization token"));

        verify(iamService, never()).validateToken(anyString());
    }

    @Test
    @DisplayName("GET /api/catalogue/items - Search with empty keyword returns all items")
    void testGetItemsByKeyword_WithEmptyKeyword_ReturnsAllItems() throws Exception {
        List<Item> items = Arrays.asList(mockItem);
        when(catalogueFacade.getItemsByKeyword("")).thenReturn(items);

        mockMvc.perform(get("/api/catalogue/items")
                .param("keyword", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(catalogueFacade).getItemsByKeyword("");
    }

    // ========== Validation Tests for Wrong User Inputs (Assignment Requirement c) ==========

    @Test
    @DisplayName("POST /api/catalogue/items - Reject negative start price (TC-CAT-04)")
    void testCreateItem_WithNegativeStartPrice_ReturnsBadRequest() throws Exception {
        ItemDTO invalidItemDTO = new ItemDTO();
        invalidItemDTO.setName("Test Item");
        invalidItemDTO.setDescription("Test Description");
        invalidItemDTO.setStartPrice(-50.0); // Invalid: negative price
        invalidItemDTO.setShippingPrice(10.0);
        invalidItemDTO.setDurationHours(24);

        ValidateTokenResponse tokenResponse = new ValidateTokenResponse(true, 123L, "seller1", "SELLER");
        when(iamService.validateToken(validToken)).thenReturn(tokenResponse);
        when(iamService.authorizeRole(validToken, "SELLER")).thenReturn(true);

        mockMvc.perform(post("/api/catalogue/items")
                .header("Authorization", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidItemDTO)))
                .andExpect(status().isBadRequest());

        verify(catalogueFacade, never()).addItem(any(ItemDTO.class));
    }

    @Test
    @DisplayName("POST /api/catalogue/items - Reject zero start price")
    void testCreateItem_WithZeroStartPrice_ReturnsBadRequest() throws Exception {
        ItemDTO invalidItemDTO = new ItemDTO();
        invalidItemDTO.setName("Test Item");
        invalidItemDTO.setDescription("Test Description");
        invalidItemDTO.setStartPrice(0.0); // Invalid: zero is not positive
        invalidItemDTO.setShippingPrice(10.0);
        invalidItemDTO.setDurationHours(24);

        ValidateTokenResponse tokenResponse = new ValidateTokenResponse(true, 123L, "seller1", "SELLER");
        when(iamService.validateToken(validToken)).thenReturn(tokenResponse);
        when(iamService.authorizeRole(validToken, "SELLER")).thenReturn(true);

        mockMvc.perform(post("/api/catalogue/items")
                .header("Authorization", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidItemDTO)))
                .andExpect(status().isBadRequest());

        verify(catalogueFacade, never()).addItem(any(ItemDTO.class));
    }

    @Test
    @DisplayName("POST /api/catalogue/items - Reject empty item name")
    void testCreateItem_WithEmptyName_ReturnsBadRequest() throws Exception {
        ItemDTO invalidItemDTO = new ItemDTO();
        invalidItemDTO.setName(""); // Invalid: empty name
        invalidItemDTO.setDescription("Test Description");
        invalidItemDTO.setStartPrice(100.0);
        invalidItemDTO.setShippingPrice(10.0);
        invalidItemDTO.setDurationHours(24);

        ValidateTokenResponse tokenResponse = new ValidateTokenResponse(true, 123L, "seller1", "SELLER");
        when(iamService.validateToken(validToken)).thenReturn(tokenResponse);
        when(iamService.authorizeRole(validToken, "SELLER")).thenReturn(true);

        mockMvc.perform(post("/api/catalogue/items")
                .header("Authorization", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidItemDTO)))
                .andExpect(status().isBadRequest());

        verify(catalogueFacade, never()).addItem(any(ItemDTO.class));
    }

    @Test
    @DisplayName("POST /api/catalogue/items - Reject null required fields")
    void testCreateItem_WithNullFields_ReturnsBadRequest() throws Exception {
        ItemDTO invalidItemDTO = new ItemDTO();
        invalidItemDTO.setName("Test Item");
        invalidItemDTO.setDescription("Test Description");
        // Missing required fields: startPrice, shippingPrice, durationHours

        ValidateTokenResponse tokenResponse = new ValidateTokenResponse(true, 123L, "seller1", "SELLER");
        when(iamService.validateToken(validToken)).thenReturn(tokenResponse);
        when(iamService.authorizeRole(validToken, "SELLER")).thenReturn(true);

        mockMvc.perform(post("/api/catalogue/items")
                .header("Authorization", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidItemDTO)))
                .andExpect(status().isBadRequest());

        verify(catalogueFacade, never()).addItem(any(ItemDTO.class));
    }

    @Test
    @DisplayName("POST /api/catalogue/items - Reject negative shipping price")
    void testCreateItem_WithNegativeShippingPrice_ReturnsBadRequest() throws Exception {
        ItemDTO invalidItemDTO = new ItemDTO();
        invalidItemDTO.setName("Test Item");
        invalidItemDTO.setDescription("Test Description");
        invalidItemDTO.setStartPrice(100.0);
        invalidItemDTO.setShippingPrice(-5.0); // Invalid: negative shipping
        invalidItemDTO.setDurationHours(24);

        ValidateTokenResponse tokenResponse = new ValidateTokenResponse(true, 123L, "seller1", "SELLER");
        when(iamService.validateToken(validToken)).thenReturn(tokenResponse);
        when(iamService.authorizeRole(validToken, "SELLER")).thenReturn(true);

        mockMvc.perform(post("/api/catalogue/items")
                .header("Authorization", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidItemDTO)))
                .andExpect(status().isBadRequest());

        verify(catalogueFacade, never()).addItem(any(ItemDTO.class));
    }

    @Test
    @DisplayName("POST /api/catalogue/items - Reject zero or negative duration")
    void testCreateItem_WithInvalidDuration_ReturnsBadRequest() throws Exception {
        ItemDTO invalidItemDTO = new ItemDTO();
        invalidItemDTO.setName("Test Item");
        invalidItemDTO.setDescription("Test Description");
        invalidItemDTO.setStartPrice(100.0);
        invalidItemDTO.setShippingPrice(10.0);
        invalidItemDTO.setDurationHours(0); // Invalid: must be at least 1 hour

        ValidateTokenResponse tokenResponse = new ValidateTokenResponse(true, 123L, "seller1", "SELLER");
        when(iamService.validateToken(validToken)).thenReturn(tokenResponse);
        when(iamService.authorizeRole(validToken, "SELLER")).thenReturn(true);

        mockMvc.perform(post("/api/catalogue/items")
                .header("Authorization", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidItemDTO)))
                .andExpect(status().isBadRequest());

        verify(catalogueFacade, never()).addItem(any(ItemDTO.class));
    }
}

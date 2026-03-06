package com.code2cash.catalogue.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.code2cash.catalogue.dto.ItemDTO;
import com.code2cash.catalogue.dto.ValidateTokenResponse;
import com.code2cash.catalogue.model.Item;
import com.code2cash.catalogue.service.CatalogueFacade;
import com.code2cash.catalogue.service.IAMService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/catalogue")
@CrossOrigin(origins = "*") // Allow frontend access
public class CatalogueController {

    @Autowired
    private CatalogueFacade catalogueFacade;

    @Autowired
    private IAMService iamService;

    // UC-CAT-7: Create new item (Requires SELLER role)
    @PostMapping("/items")
    public ResponseEntity<?> createItem(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody ItemDTO itemDTO) {
        
        // Validate JWT token
        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Missing authorization token"));
        }

        ValidateTokenResponse tokenResponse = iamService.validateToken(authHeader);
        if (!tokenResponse.isValid()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Invalid or expired token"));
        }

        // Check if user has SELLER role
        if (!iamService.authorizeRole(authHeader, "SELLER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse("Only sellers can create auction items"));
        }

        // Set the sellerId from the validated token (not from request body)
        itemDTO.setSellerId(tokenResponse.getUserId());
        Item createdItem = catalogueFacade.addItem(itemDTO);
        return ResponseEntity.ok(createdItem);
    }

    // UC-CAT-2.1: Search items (Public - no auth required)
    @GetMapping("/items")
    public ResponseEntity<List<Item>> getItemsByKeyword(@RequestParam(required = false) String keyword) {
        List<Item> items = catalogueFacade.getItemsByKeyword(keyword);
        return ResponseEntity.ok(items);
    }

    // UC-CAT-2: Get specific item details (Public - no auth required)
    @GetMapping("/items/{id}")
    public ResponseEntity<?> getItemDetails(@PathVariable Long id) {
        Item item = catalogueFacade.getItemDetails(id);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(item);
    }

    // Helper method to create error response
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}

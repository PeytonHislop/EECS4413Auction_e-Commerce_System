package com.yorku.eecs4413.platform.gateway_service.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.yorku.eecs4413.platform.gateway_service.client.CatalogueClient;

@RestController
@RequestMapping("/api/catalogue/items")
public class CatalogueGatewayController {

    private final CatalogueClient catalogue;

    public CatalogueGatewayController(CatalogueClient catalogue) {
        this.catalogue = catalogue;
    }

    @PostMapping
    public ResponseEntity<String> createItem(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
            @RequestBody String body
    ) {
        return catalogue.postItemWithAuth("/api/catalogue/items", auth, body);
    }

    @GetMapping
    public ResponseEntity<String> getAllItems() {
        return catalogue.get("/api/catalogue/items");
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getItemById(@PathVariable Long id) {
        return catalogue.get("/api/catalogue/items/" + id);
    }
}

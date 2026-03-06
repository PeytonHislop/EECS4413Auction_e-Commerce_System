package com.code2cash.catalogue.repository;

import com.code2cash.catalogue.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    
    // Custom query to find items by keyword AND ensure they haven't ended [cite: 725, 823]
    // Handles TC-CAT-01 (Search) and TC-CAT-03 (Filter Expired)
    List<Item> findByNameContainingIgnoreCaseAndEndDateAfter(String keyword, LocalDateTime currentTime);
    
    // Find all active items
    List<Item> findByEndDateAfter(LocalDateTime currentTime);
}

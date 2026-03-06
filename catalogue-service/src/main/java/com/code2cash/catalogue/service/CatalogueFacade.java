package com.code2cash.catalogue.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.code2cash.catalogue.dto.ItemDTO;
import com.code2cash.catalogue.model.Item;
import com.code2cash.catalogue.repository.ItemRepository;

@Service
public class CatalogueFacade {

    @Autowired
    private ItemRepository itemRepository;

    // Implements UC-CAT-7: Auction Item (Static Catalogue)
    public Item addItem(ItemDTO itemDTO) {
        Item item = new Item();
        item.setName(itemDTO.getName());
        item.setDescription(itemDTO.getDescription());
        item.setStartPrice(itemDTO.getStartPrice());
        item.setShippingPrice(itemDTO.getShippingPrice());
        item.setDurationHours(itemDTO.getDurationHours());
        item.setSellerId(itemDTO.getSellerId());
        item.setStatus("ACTIVE");

        // Logic: Calculate absolute End Date [cite: 730]
        // EndTime = CurrentTime + Duration
        item.setEndDate(LocalDateTime.now().plusHours(itemDTO.getDurationHours()));

        return itemRepository.save(item);
    }

    // Implements UC-CAT-2.1: Item Search
    // Business Logic: Filter items where CurrentTime < EndDate
    public List<Item> getItemsByKeyword(String keyword) {
        LocalDateTime currentTime = LocalDateTime.now();
        
        if (keyword == null || keyword.isEmpty()) {
            // Return all active items if no keyword
            // Filter items where CurrentTime < EndDate
            return itemRepository.findByEndDateAfter(currentTime);
        }
        
        // SELECT items WHERE keywords LIKE %keyword%
        // Filter out items where CurrentTime > EndDate [cite: 823]
        return itemRepository.findByNameContainingIgnoreCaseAndEndDateAfter(keyword, currentTime);
    }

    // Implements UC-CAT-2: Item Detail
    public Item getItemDetails(Long id) {
        Optional<Item> item = itemRepository.findById(id);
        return item.orElse(null);
    }
}

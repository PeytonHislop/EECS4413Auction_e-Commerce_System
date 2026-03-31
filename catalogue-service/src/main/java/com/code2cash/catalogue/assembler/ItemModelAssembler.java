package com.code2cash.catalogue.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import com.code2cash.catalogue.controller.CatalogueController;
import com.code2cash.catalogue.model.Item;

/**
 * Model Assembler for Item to add HATEOAS links
 * Implements REST Level 3 - Hypermedia as the Engine of Application State
 */
@Component
public class ItemModelAssembler implements RepresentationModelAssembler<Item, EntityModel<Item>> {

    @Override
    public EntityModel<Item> toModel(Item item) {
        return EntityModel.of(item,
                linkTo(methodOn(CatalogueController.class).getItemDetails(item.getId())).withSelfRel(),
                linkTo(methodOn(CatalogueController.class).getItemsByKeyword(null)).withRel("all-items"));
    }
}

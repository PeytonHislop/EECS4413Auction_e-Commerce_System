package com.code2cash.catalogue.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "catalogue_items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    
    @Column(length = 1000)
    private String description;

    // "Forward Auction Type" implies we need to track this, though strictly logic
    private final String auctionType = "FORWARD_AUCTION"; 

    private Double startPrice;
    private Double shippingPrice;
    
    // Duration in hours (input by seller)
    private Integer durationHours;

    // Calculated End Date [cite: 730]
    private LocalDateTime endDate;

    // Status: ACTIVE, EXPIRED, SOLD
    private String status;

    // Link to the Seller (UserId from IAM service)
    private Long sellerId;

    // Constructors
    public Item() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuctionType() {
        return auctionType;
    }

    public Double getStartPrice() {
        return startPrice;
    }

    public void setStartPrice(Double startPrice) {
        this.startPrice = startPrice;
    }

    public Double getShippingPrice() {
        return shippingPrice;
    }

    public void setShippingPrice(Double shippingPrice) {
        this.shippingPrice = shippingPrice;
    }

    public Integer getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(Integer durationHours) {
        this.durationHours = durationHours;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", auctionType='" + auctionType + '\'' +
                ", startPrice=" + startPrice +
                ", shippingPrice=" + shippingPrice +
                ", durationHours=" + durationHours +
                ", endDate=" + endDate +
                ", status='" + status + '\'' +
                ", sellerId=" + sellerId +
                '}';
    }
}

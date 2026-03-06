package com.auction.payment.dto;


import jakarta.validation.constraints.*;

public class PaymentRequest {

	@NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Auction ID is required")
    private String auctionId;

    @Positive(message = "Amount must be greater than 0")
    private double amount;

    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
    private String cardNumber;

    @NotBlank(message = "Name on card is required")
    private String nameOnCard;

    @NotBlank(message = "Expiry date is required")
    @Pattern(
            regexp = "^(0[1-9]|1[0-2])\\/\\d{2}$",
            message = "Expiry must be in MM/YY format"
        )
    private String expiry;

    @NotBlank(message = "CVV is required")
    @Pattern(regexp = "\\d{3}", message = "CVV must be 3 digits")
    private String cvv;

 // ---- Getters ----

    public String getUserId() {
        return userId;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public double getAmount() {
        return amount;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getNameOnCard() {
        return nameOnCard;
    }

    public String getExpiry() {
        return expiry;
    }

    public String getCvv() {
        return cvv;
    }

    // ---- Setters ----

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public void setNameOnCard(String nameOnCard) {
        this.nameOnCard = nameOnCard;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }
}

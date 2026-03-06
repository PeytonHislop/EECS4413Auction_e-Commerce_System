package com.auction.payment.dto;




public class ReceiptResponse {

    private String orderId;
    private double itemPrice;
    private double shippingCost;
    private double total;
    private int shippingDays;
    private String shippingAddress;
 // Constructor

    public ReceiptResponse(String orderId,
                           double itemPrice,
                           double shippingCost,
                           double total,
                           int shippingDays) {
        this.orderId = orderId;
        this.itemPrice = itemPrice;
        this.shippingCost = shippingCost;
        this.total = total;
        this.shippingDays = shippingDays;
    }

    // ---- Getters ----

    public String getOrderId() {
        return orderId;
    }

    public double getItemPrice() {
        return itemPrice;
    }

    public double getShippingCost() {
        return shippingCost;
    }

    public double getTotal() {
        return total;
    }

    public int getShippingDays() {
        return shippingDays;
    }

    // ---- Setters ----

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setItemPrice(double itemPrice) {
        this.itemPrice = itemPrice;
    }

    public void setShippingCost(double shippingCost) {
        this.shippingCost = shippingCost;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public void setShippingDays(int shippingDays) {
        this.shippingDays = shippingDays;
    }

	public String getShippingAddress() {
		return shippingAddress;
	}

	public void setShippingAddress(String shippingAddress) {
		this.shippingAddress = shippingAddress;
	}
}

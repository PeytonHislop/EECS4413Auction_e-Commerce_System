package com.yorku.eecs4413.platform.iam_service.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class Address {

    private String streetNumber;
    private String streetName;
    private String city;
    private String country;
    private String postalCode;

    public Address() {}

    public Address(String streetNumber, String streetName, String city, String country, String postalCode) {
        this.streetNumber = streetNumber;
        this.streetName = streetName;
        this.city = city;
        this.country = country;
        this.postalCode = postalCode;
    }

    public String getStreetNumber() { return streetNumber; }
    public void setStreetNumber(String streetNumber) { this.streetNumber = streetNumber; }

    public String getStreetName() { return streetName; }
    public void setStreetName(String streetName) { this.streetName = streetName; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
}
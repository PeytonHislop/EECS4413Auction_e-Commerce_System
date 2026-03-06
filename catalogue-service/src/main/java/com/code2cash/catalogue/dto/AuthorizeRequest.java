package com.code2cash.catalogue.dto;

public class AuthorizeRequest {

    private String role;

    // Constructors
    public AuthorizeRequest() {
    }

    public AuthorizeRequest(String role) {
        this.role = role;
    }

    // Getters and Setters
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

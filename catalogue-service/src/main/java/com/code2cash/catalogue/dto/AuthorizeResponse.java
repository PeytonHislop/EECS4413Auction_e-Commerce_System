package com.code2cash.catalogue.dto;

public class AuthorizeResponse {

    private boolean authorized;
    private String requiredRole;

    // Constructors
    public AuthorizeResponse() {
    }

    public AuthorizeResponse(boolean authorized, String requiredRole) {
        this.authorized = authorized;
        this.requiredRole = requiredRole;
    }

    // Getters and Setters
    public boolean isAuthorized() {
        return authorized;
    }

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }

    public String getRequiredRole() {
        return requiredRole;
    }

    public void setRequiredRole(String requiredRole) {
        this.requiredRole = requiredRole;
    }
}

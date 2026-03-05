package com.yorku.eecs4413.platform.gateway_service.controller;

import com.yorku.eecs4413.platform.gateway_service.client.IamClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthGatewayController {

    private final IamClient iam;

    public AuthGatewayController(IamClient iam) {
        this.iam = iam;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody String body) {
        return iam.postJson("/auth/signup", body);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody String body) {
        return iam.postJson("/auth/login", body);
    }

    @PostMapping("/validate")
    public ResponseEntity<String> validate(@RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
        return iam.postWithAuth("/auth/validate", auth);
    }

    @GetMapping("/authorize")
    public ResponseEntity<String> authorize(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
            @RequestParam String requiredRole
    ) {
        return iam.getWithAuth("/auth/authorize?requiredRole=" + requiredRole, auth);
    }

    @PostMapping("/password/forgot")
    public ResponseEntity<String> forgot(@RequestBody String body) {
        return iam.postJson("/auth/password/forgot", body);
    }

    @PostMapping("/password/reset")
    public ResponseEntity<String> reset(@RequestBody String body) {
        return iam.postJson("/auth/password/reset", body);
    }
}
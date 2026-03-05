package com.yorku.eecs4413.platform.iam_service.controller;

import com.yorku.eecs4413.platform.iam_service.dto.*;
import com.yorku.eecs4413.platform.iam_service.model.Role;
import com.yorku.eecs4413.platform.iam_service.model.User;
import com.yorku.eecs4413.platform.iam_service.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> signup(@Valid @RequestBody SignupRequest req) {
        User user = service.signup(req);
        return Map.of(
                "userId", user.getId(),
                "username", user.getUsername(),
                "role", user.getRole().name(),
                "message", "User created successfully"
        );
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return service.login(req);
    }

    @PostMapping("/validate")
    public ValidateTokenResponse validate(@RequestHeader("Authorization") String authHeader) {
        String token = extractBearer(authHeader);
        return service.validateToken(token);
    }

    @PostMapping("/password/forgot")
    public Map<String, String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        String token = service.createPasswordResetToken(req);

        return Map.of(
                "message", "Password reset token generated",
                "resetToken", token
        );
    }

    @PostMapping("/password/reset")
    public Map<String, String> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        service.resetPassword(req);
        return Map.of("message", "Password reset successful");
    }

    @GetMapping("/authorize")
    public Map<String, Object> authorizeRole(@RequestHeader("Authorization") String authHeader,
                                             @RequestParam Role requiredRole) {
        String token = extractBearer(authHeader);
        boolean allowed = service.authorizeRole(token, requiredRole);
        return Map.of(
                "authorized", allowed,
                "requiredRole", requiredRole.name()
        );
    }

    private String extractBearer(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing or invalid Authorization header");
        }
        return authHeader.substring(7);
    }
}
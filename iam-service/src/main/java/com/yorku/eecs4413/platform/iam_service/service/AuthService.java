package com.yorku.eecs4413.platform.iam_service.service;

import com.yorku.eecs4413.platform.iam_service.dto.*;
import com.yorku.eecs4413.platform.iam_service.exception.*;
import com.yorku.eecs4413.platform.iam_service.model.*;
import com.yorku.eecs4413.platform.iam_service.repository.PasswordResetTokenRepository;
import com.yorku.eecs4413.platform.iam_service.repository.UserRepository;
import com.yorku.eecs4413.platform.iam_service.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordResetTokenRepository resetTokenRepository,
                       BCryptPasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.resetTokenRepository = resetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public User signup(SignupRequest req) {
        if (userRepository.existsByUsername(req.getUsername().trim())) {
            throw new UserAlreadyExistsException("Username already exists");
        }
        if (userRepository.existsByEmail(req.getEmail().trim().toLowerCase())) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        validatePasswordRules(req.getPassword());

        if (req.getShippingAddress() == null) {
            throw new IllegalArgumentException("Shipping address is required");
        }

        User user = new User();
        user.setUsername(req.getUsername().trim());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setFirstName(req.getFirstName().trim());
        user.setLastName(req.getLastName().trim());
        user.setEmail(req.getEmail().trim().toLowerCase());
        user.setRole(parseRole(req.getRole()));
        user.setShippingAddress(mapAddress(req.getShippingAddress()));

        return userRepository.save(user);
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByUsername(req.getUsername().trim())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user);
        return new AuthResponse(
                token,
                jwtUtil.getExpirationSeconds(),
                user.getId(),
                user.getUsername(),
                user.getRole().name()
        );
    }

    public ValidateTokenResponse validateToken(String token) {
        if (!jwtUtil.isValid(token)) {
            return new ValidateTokenResponse(false, null, null, null);
        }
        Claims claims = jwtUtil.parseClaims(token);
        return new ValidateTokenResponse(
                true,
                claims.getSubject(),
                claims.get("username", String.class),
                claims.get("role", String.class)
        );
    }

    public String createPasswordResetToken(ForgotPasswordRequest req) {
        User user = userRepository.findByUsername(req.getUsername().trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = "RST-" + UUID.randomUUID();
        PasswordResetToken prt = new PasswordResetToken(
                user.getId(),
                token,
                Instant.now().plusSeconds(15 * 60) // 15 min
        );
        resetTokenRepository.save(prt);
        return token;
    }

    public void resetPassword(ResetPasswordRequest req) {
        validatePasswordRules(req.getNewPassword());

        PasswordResetToken tokenRecord = resetTokenRepository.findByToken(req.getResetToken().trim())
                .orElseThrow(() -> new InvalidTokenException("Invalid reset token"));

        if (tokenRecord.isUsed()) {
            throw new InvalidTokenException("Reset token already used");
        }
        if (tokenRecord.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidTokenException("Reset token expired");
        }

        User user = userRepository.findById(tokenRecord.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        tokenRecord.setUsed(true);
        resetTokenRepository.save(tokenRecord);
    }

    public boolean authorizeRole(String token, Role requiredRole) {
        ValidateTokenResponse v = validateToken(token);
        if (!v.isValid()) return false;
        if (v.getRole() == null) return false;
        return requiredRole.name().equalsIgnoreCase(v.getRole());
    }

    private void validatePasswordRules(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        boolean hasSpecial = password.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));
        if (!hasSpecial) {
            throw new IllegalArgumentException("Password must contain at least one special character");
        }
    }

    private Role parseRole(String role) {
        if (role == null || role.isBlank()) return Role.BUYER;
        try {
            return Role.valueOf(role.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role. Allowed: BUYER, SELLER, ADMIN");
        }
    }

    private Address mapAddress(AddressDto dto) {
        return new Address(
                dto.getStreetNumber().trim(),
                dto.getStreetName().trim(),
                dto.getCity().trim(),
                dto.getCountry().trim(),
                dto.getPostalCode().trim()
        );
    }
}
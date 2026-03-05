package com.yorku.eecs4413.platform.iam_service.controller;

import com.yorku.eecs4413.platform.iam_service.dto.UserProfileResponse;
import com.yorku.eecs4413.platform.iam_service.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/{userId}")
    public UserProfileResponse getUserProfile(@PathVariable String userId) {
        return service.getUserProfile(userId);
    }
}
package com.yorku.eecs4413.platform.gateway_service.controller;

import com.yorku.eecs4413.platform.gateway_service.client.IamClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UsersGatewayController {

    private final IamClient iam;

    public UsersGatewayController(IamClient iam) {
        this.iam = iam;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<String> getUser(@PathVariable String userId) {
        return iam.get("/users/" + userId);
    }
}
package com.url.shortener.controller;
import com.url.shortener.dtos.LoginRequest;
import com.url.shortener.dtos.RegisterRequest;
import com.url.shortener.models.User;
import com.url.shortener.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Handles authentication endpoints: login and register
// Base URL: /api/auth — "public" routes are accessible without JWT
@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor // Lombok: injects UserService via constructor (no @Autowired needed)
public class AuthController {

    private UserService userService;

    // POST /api/auth/public/login
    // Accepts username+password, returns JWT token on success
    @PostMapping("/public/login")
    public ResponseEntity<?> LoginUser(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(userService.authenticateUser(loginRequest));
    }

    // POST /api/auth/public/register
    // Builds a User object from request data, assigns default role, saves to DB
    @PostMapping("/public/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(registerRequest.getPassword()); // hashed in service layer
        user.setEmail(registerRequest.getEmail());
        user.setRole("ROLE_USER"); // default role for all new users
        userService.registerUser(user);
        return ResponseEntity.ok("User registered successfully");
    }
}

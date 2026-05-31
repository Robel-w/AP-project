package com.civicpulse.backend.controller;

import com.civicpulse.backend.dto.LoginRequest;
import com.civicpulse.backend.dto.RegisterRequest;
import com.civicpulse.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.civicpulse.backend.dto.UserResponse;
import com.civicpulse.backend.model.User;
import com.civicpulse.backend.model.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register (@Valid @RequestBody RegisterRequest request){
        String message = authService.register(request);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest){
        User user = authService.login(request);
        
        // Store user in session
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute("user", user);

        UserResponse response = new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole().name());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok("Logout successful");
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute("user");
            if (user != null) {
                UserResponse response = new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole().name());
                return ResponseEntity.ok(response);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/create-admin")
    public ResponseEntity<String> createAdmin(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: No session active.");
        }
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getRole() != UserRole.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden: Only administrators can create other admin accounts.");
        }

        String message = authService.registerAdmin(request);
        return ResponseEntity.ok(message);
    }
}

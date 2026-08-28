package com.workspace.controller;

import com.workspace.dto.AuthDTOs;
import com.workspace.dto.UserDTO;
import com.workspace.security.SecurityUtils;
import com.workspace.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthDTOs.AuthResponse> register(@Valid @RequestBody AuthDTOs.RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDTOs.AuthResponse> login(@Valid @RequestBody AuthDTOs.LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUserDTO(SecurityUtils.getCurrentUserId()));
    }
}

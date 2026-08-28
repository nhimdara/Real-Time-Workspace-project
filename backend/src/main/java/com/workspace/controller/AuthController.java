package com.workspace.controller;

import com.workspace.dto.AuthDTOs;
import com.workspace.dto.UserDTO;
import com.workspace.security.SecurityUtils;
import com.workspace.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthDTOs.AuthResponse> register(@Valid @RequestBody AuthDTOs.RegisterRequest request) {
        AuthDTOs.AuthResponse response = authService.register(request);
        ResponseCookie cookie = createJwtCookie(response.getToken(), 86400);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDTOs.AuthResponse> login(@Valid @RequestBody AuthDTOs.LoginRequest request) {
        AuthDTOs.AuthResponse response = authService.login(request);
        ResponseCookie cookie = createJwtCookie(response.getToken(), 86400);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie cookie = createJwtCookie("", 0);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUserDTO(SecurityUtils.getCurrentUserId()));
    }

    private ResponseCookie createJwtCookie(String token, long maxAgeSeconds) {
        return ResponseCookie.from("jwt_token", token)
                .httpOnly(true)
                .secure(false) // Set to true in HTTPS production
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite("Lax")
                .build();
    }
}

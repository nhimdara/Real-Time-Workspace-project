package com.workspace.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class AuthDTOs {

    @Data
    public static class LoginRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;
    }

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        @NotBlank(message = "Name is required")
        private String name;

        private String avatarUrl;
    }

    @Data
    public static class AuthResponse {
        private String token;
        private String type = "Bearer";
        private UserDTO user;

        public AuthResponse(String token, UserDTO user) {
            this.token = token;
            this.user = user;
        }
    }
}

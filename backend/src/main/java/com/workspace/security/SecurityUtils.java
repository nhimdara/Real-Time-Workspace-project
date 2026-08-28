package com.workspace.security;

import com.workspace.entity.User;
import com.workspace.exception.Exceptions;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public class SecurityUtils {

    public static UserDetailsServiceImpl.SecurityUser getCurrentUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserDetailsServiceImpl.SecurityUser)) {
            throw new Exceptions.UnauthorizedException("User is not authenticated");
        }
        return (UserDetailsServiceImpl.SecurityUser) auth.getPrincipal();
    }

    public static User getCurrentUser() {
        return getCurrentUserDetails().getUser();
    }

    public static UUID getCurrentUserId() {
        return getCurrentUserDetails().getId();
    }
}

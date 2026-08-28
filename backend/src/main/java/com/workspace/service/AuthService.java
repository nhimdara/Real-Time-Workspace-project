package com.workspace.service;

import com.workspace.dto.AuthDTOs;
import com.workspace.dto.UserDTO;
import com.workspace.entity.User;
import com.workspace.entity.Workspace;
import com.workspace.entity.WorkspaceMember;
import com.workspace.exception.Exceptions;
import com.workspace.repository.UserRepository;
import com.workspace.repository.WorkspaceMemberRepository;
import com.workspace.repository.WorkspaceRepository;
import com.workspace.security.JwtService;
import com.workspace.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;

    @Transactional
    public AuthDTOs.AuthResponse register(AuthDTOs.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new Exceptions.BadRequestException("Email is already registered");
        }

        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName().trim())
                .avatarUrl(request.getAvatarUrl() != null && !request.getAvatarUrl().isBlank()
                        ? request.getAvatarUrl()
                        : "https://api.dicebear.com/7.x/bottts/svg?seed=" + request.getEmail())
                .build();

        user = userRepository.save(user);

        // Create a default Personal Workspace for the new user
        String slug = "workspace-" + user.getId().toString().substring(0, 8);
        Workspace defaultWorkspace = Workspace.builder()
                .name(user.getName() + "'s Workspace")
                .slug(slug)
                .owner(user)
                .build();
        defaultWorkspace = workspaceRepository.save(defaultWorkspace);

        WorkspaceMember ownerMember = WorkspaceMember.builder()
                .workspace(defaultWorkspace)
                .user(user)
                .role("OWNER")
                .build();
        workspaceMemberRepository.save(ownerMember);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails, user.getId(), user.getName());

        return new AuthDTOs.AuthResponse(token, mapToDTO(user));
    }

    public AuthDTOs.AuthResponse login(AuthDTOs.LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase().trim(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("User not found"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails, user.getId(), user.getName());

        return new AuthDTOs.AuthResponse(token, mapToDTO(user));
    }

    public UserDTO getCurrentUserDTO(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("User not found"));
        return mapToDTO(user);
    }

    public UserDTO mapToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

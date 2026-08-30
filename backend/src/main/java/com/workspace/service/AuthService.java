package com.workspace.service;

import com.workspace.dto.AuthDTOs;
import com.workspace.dto.UserDTO;
import com.workspace.entity.Block;
import com.workspace.entity.Page;
import com.workspace.entity.User;
import com.workspace.entity.Workspace;
import com.workspace.entity.WorkspaceMember;
import com.workspace.exception.Exceptions;
import com.workspace.repository.BlockRepository;
import com.workspace.repository.PageRepository;
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

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final PageRepository pageRepository;
    private final BlockRepository blockRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;

    @Transactional
    public AuthDTOs.AuthResponse register(AuthDTOs.RegisterRequest request) {
        String cleanEmail = request.getEmail().toLowerCase().trim();
        User user = userRepository.findByEmail(cleanEmail).orElse(null);

        if (user != null) {
            // If user already exists, update their password and name smoothly
            user.setName(request.getName().trim());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            if (request.getAvatarUrl() != null && !request.getAvatarUrl().isBlank()) {
                user.setAvatarUrl(request.getAvatarUrl());
            }
            user = userRepository.saveAndFlush(user);
        } else {
            user = User.builder()
                    .email(cleanEmail)
                    .password(passwordEncoder.encode(request.getPassword()))
                    .name(request.getName().trim())
                    .avatarUrl(request.getAvatarUrl() != null && !request.getAvatarUrl().isBlank()
                            ? request.getAvatarUrl()
                            : "https://api.dicebear.com/7.x/bottts/svg?seed=" + cleanEmail)
                    .build();
            user = userRepository.saveAndFlush(user);
        }

        // Ensure user has at least one workspace
        if (workspaceRepository.findWorkspacesForUser(user.getId()).isEmpty()) {
            String shortId = user.getId().toString().substring(0, 8);
            String nameSlug = user.getName().toLowerCase().trim().replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
            if (nameSlug.isEmpty()) {
                nameSlug = "user";
            }
            String slug = "workspace-" + nameSlug + "-" + shortId;

            Workspace defaultWorkspace = Workspace.builder()
                    .name(user.getName() + "'s Workspace")
                    .slug(slug)
                    .owner(user)
                    .build();
            defaultWorkspace = workspaceRepository.saveAndFlush(defaultWorkspace);

            WorkspaceMember ownerMember = WorkspaceMember.builder()
                    .workspace(defaultWorkspace)
                    .user(user)
                    .role("OWNER")
                    .build();
            workspaceMemberRepository.saveAndFlush(ownerMember);

            // Create initial default document page for the workspace
            Page defaultPage = Page.builder()
                    .workspace(defaultWorkspace)
                    .title("Welcome to Your Workspace")
                    .icon("🚀")
                    .isKanban(false)
                    .position(0)
                    .build();
            defaultPage = pageRepository.saveAndFlush(defaultPage);

            // Create initial welcoming blocks using mutable maps for Hibernate JSONB compatibility
            Map<String, Object> headingContent = new java.util.HashMap<>();
            headingContent.put("text", "Welcome to your new workspace, " + user.getName() + "!");

            Block welcomeHeading = Block.builder()
                    .page(defaultPage)
                    .type("heading_1")
                    .content(headingContent)
                    .position(0)
                    .build();

            Map<String, Object> calloutContent = new java.util.HashMap<>();
            calloutContent.put("text", "Start typing or use / commands to build Notion-style docs and Kanban boards.");
            calloutContent.put("icon", "💡");

            Block welcomeCallout = Block.builder()
                    .page(defaultPage)
                    .type("callout")
                    .content(calloutContent)
                    .position(1)
                    .build();

            blockRepository.save(welcomeHeading);
            blockRepository.save(welcomeCallout);
        }

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

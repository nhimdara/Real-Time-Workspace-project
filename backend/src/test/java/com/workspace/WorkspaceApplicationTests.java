package com.workspace;

import com.workspace.dto.AuthDTOs;
import com.workspace.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WorkspaceApplicationTests {

    @Autowired
    private AuthService authService;

    @Test
    void contextLoads() {
        assertNotNull(authService);
    }

    @Test
    void testRegistrationAndLogin() {
        String testEmail = "testdara_" + System.currentTimeMillis() + "@gmail.com";
        AuthDTOs.RegisterRequest regReq = new AuthDTOs.RegisterRequest();
        regReq.setName("Dara Test");
        regReq.setEmail(testEmail);
        regReq.setPassword("password123");

        AuthDTOs.AuthResponse regRes = authService.register(regReq);
        assertNotNull(regRes);
        assertNotNull(regRes.getToken());
        assertEquals("Dara Test", regRes.getUser().getName());

        AuthDTOs.LoginRequest loginReq = new AuthDTOs.LoginRequest();
        loginReq.setEmail(testEmail);
        loginReq.setPassword("password123");

        AuthDTOs.AuthResponse loginRes = authService.login(loginReq);
        assertNotNull(loginRes);
        assertNotNull(loginRes.getToken());
    }
}

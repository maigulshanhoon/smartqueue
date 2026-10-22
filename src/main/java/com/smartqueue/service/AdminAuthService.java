package com.smartqueue.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthService {

    public static final String SESSION_KEY = "SMARTQUEUE_ADMIN_LOGGED_IN";

    private final String adminUsername;
    private final String adminPassword;

    public AdminAuthService(
            @Value("${ADMIN_USERNAME:admin}") String adminUsername,
            @Value("${ADMIN_PASSWORD:admin123}") String adminPassword) {
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    public boolean authenticate(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        return adminUsername.equals(username.trim()) && adminPassword.equals(password);
    }

    public boolean isLoggedIn(HttpSession session) {
        if (session == null) {
            return false;
        }
        Object attr = session.getAttribute(SESSION_KEY);
        return Boolean.TRUE.equals(attr);
    }

    public void login(HttpSession session) {
        session.setAttribute(SESSION_KEY, Boolean.TRUE);
    }

    public void logout(HttpSession session) {
        if (session != null) {
            session.removeAttribute(SESSION_KEY);
            session.invalidate();
        }
    }
}

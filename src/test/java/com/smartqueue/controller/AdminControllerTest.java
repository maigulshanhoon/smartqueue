package com.smartqueue.controller;

import com.smartqueue.model.QueueToken;
import com.smartqueue.model.ServiceType;
import com.smartqueue.service.AdminAuthService;
import com.smartqueue.service.QueueService;
import com.smartqueue.repository.QueueTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private QueueService queueService;

    @Autowired
    private QueueTokenRepository tokenRepository;

    @BeforeEach
    void setUp() {
        tokenRepository.deleteAll();
    }

    @Test
    void unauthenticatedAccessToAdminDashboardRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/login"));
    }

    @Test
    void loginWithInvalidCredentialsFails() throws Exception {
        mockMvc.perform(post("/admin/login")
                        .param("username", "admin")
                        .param("password", "wrongpassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/login"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void loginWithValidCredentialsSucceeds() throws Exception {
        mockMvc.perform(post("/admin/login")
                        .param("username", "admin")
                        .param("password", "admin123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));
    }

    @Test
    void authenticatedUserCanViewDashboard() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AdminAuthService.SESSION_KEY, Boolean.TRUE);

        mockMvc.perform(get("/admin").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("stats"))
                .andExpect(model().attributeExists("servingTokens"))
                .andExpect(model().attributeExists("waitingTokens"));
    }

    @Test
    void authenticatedUserCanCallNextAndComplete() throws Exception {
        QueueToken t1 = queueService.issueToken(ServiceType.GENERAL_ENQUIRY, "Alice");

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AdminAuthService.SESSION_KEY, Boolean.TRUE);

        // Call Next
        mockMvc.perform(post("/admin/call-next")
                        .param("counterName", "Counter 1")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("successMessage"));

        // Complete Token
        mockMvc.perform(post("/admin/tokens/" + t1.getId() + "/complete")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    void authenticatedUserCanSkipToken() throws Exception {
        QueueToken t1 = queueService.issueToken(ServiceType.BILLING, "Bob");

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AdminAuthService.SESSION_KEY, Boolean.TRUE);

        mockMvc.perform(post("/admin/tokens/" + t1.getId() + "/skip")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("infoMessage"));
    }
}

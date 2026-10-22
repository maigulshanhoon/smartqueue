package com.smartqueue.controller;

import com.smartqueue.model.QueueToken;
import com.smartqueue.model.ServiceType;
import com.smartqueue.service.QueueService;
import com.smartqueue.repository.QueueTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class QueueApiControllerTest {

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
    void shouldReturnQueueStatus() throws Exception {
        queueService.issueToken(ServiceType.GENERAL_ENQUIRY, "Alice");

        mockMvc.perform(get("/api/queue/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.stats.totalWaiting", is(1)))
                .andExpect(jsonPath("$.waiting", hasSize(1)))
                .andExpect(jsonPath("$.waiting[0].tokenNumber", is("GEN-001")));
    }

    @Test
    void shouldCreateTokenViaRestApi() throws Exception {
        String json = """
                {
                    "serviceType": "BILLING",
                    "customerName": "John Doe"
                }
                """;

        mockMvc.perform(post("/api/queue/tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.token.tokenNumber", is("BIL-001")))
                .andExpect(jsonPath("$.token.customerName", is("John Doe")))
                .andExpect(jsonPath("$.peopleAhead", is(0)));
    }

    @Test
    void shouldGetTokenDetailsAndReturnNotFoundForInvalid() throws Exception {
        QueueToken token = queueService.issueToken(ServiceType.TECHNICAL_SUPPORT, "Bob");

        mockMvc.perform(get("/api/queue/tokens/" + token.getTokenNumber()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token.tokenNumber", is(token.getTokenNumber())))
                .andExpect(jsonPath("$.token.status", is("WAITING")));

        mockMvc.perform(get("/api/queue/tokens/INVALID-999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("Token not found")));
    }

    @Test
    void shouldCallCompleteAndSkipViaRestApi() throws Exception {
        QueueToken t1 = queueService.issueToken(ServiceType.GENERAL_ENQUIRY, "Customer 1");

        // Call Next
        mockMvc.perform(post("/api/queue/call-next")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"counterName\":\"Counter 1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.called", is(true)))
                .andExpect(jsonPath("$.token.tokenNumber", is(t1.getTokenNumber())));

        // Complete Token
        mockMvc.perform(post("/api/queue/tokens/" + t1.getId() + "/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")));

        // Issue another and skip
        QueueToken t2 = queueService.issueToken(ServiceType.BILLING, "Customer 2");
        mockMvc.perform(post("/api/queue/tokens/" + t2.getId() + "/skip"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SKIPPED")));

        // Issue another and cancel
        QueueToken t3 = queueService.issueToken(ServiceType.TECHNICAL_SUPPORT, "Customer 3");
        mockMvc.perform(post("/api/queue/tokens/" + t3.getId() + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }
}

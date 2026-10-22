package com.smartqueue.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void homePageReturnsIndexViewAndOkStatus() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("appName", "SmartQueue"))
                .andExpect(content().string(containsString("SmartQueue")));
    }

    @Test
    void submittingTokenFormCreatesTicketAndRedirects() throws Exception {
        mockMvc.perform(post("/tokens")
                        .param("serviceType", "BILLING")
                        .param("customerName", "Jane Doe"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/?tokenNumber=BIL-*"))
                .andExpect(flash().attributeExists("successMessage"));
    }
}

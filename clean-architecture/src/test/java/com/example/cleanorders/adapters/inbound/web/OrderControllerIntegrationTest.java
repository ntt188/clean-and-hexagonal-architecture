package com.example.cleanorders.adapters.inbound.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test tich hop: di qua adapter web that -> use case that -> JPA that (H2 + Flyway).
 * Chung minh toan bo cac adapter duoc cam dung vao cac port.
 */
@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("POST /api/orders hop le -> 201 va don duoc luu vao H2")
    void placeOrderReturns201() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId":"C-01","items":[{"productId":"P-01","quantity":2}]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.totalAmount").value("240.00"))
                .andExpect(jsonPath("$.customerName").value("Nguyen Van A"));
    }

    @Test
    @DisplayName("POST /api/orders vuot han muc -> 422")
    void placeOrderOverCreditLimitReturns422() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId":"C-02","items":[{"productId":"P-01","quantity":1}]}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    @DisplayName("POST /api/orders thieu items -> 400 do bean validation")
    void invalidRequestReturns400() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId":"C-01","items":[]}
                                """))
                .andExpect(status().isBadRequest());
    }
}

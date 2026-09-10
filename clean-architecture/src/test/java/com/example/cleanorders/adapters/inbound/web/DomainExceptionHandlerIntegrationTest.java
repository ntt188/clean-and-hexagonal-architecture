package com.example.cleanorders.adapters.inbound.web;

import com.example.cleanorders.adapters.inbound.web.mapper.OrderWebMapper;
import com.example.cleanorders.domain.order.exception.InvalidOrderException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Chung minh {@code advice/DomainExceptionHandler} that su hoat dong.
 *
 * Ngoai le domain thoat ra khoi controller phai duoc dich thanh 400 kem than JSON,
 * chu khong duoc bien thanh loi 500 khong ai hieu.
 */
@SpringBootTest
@AutoConfigureMockMvc
class DomainExceptionHandlerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderWebMapper mapper;

    @Test
    @DisplayName("Ngoai le domain thoat ra ngoai -> 400 kem than JSON, khong phai 500")
    void translateDomainExceptionToBadRequest() throws Exception {
        given(mapper.toRequestModel(any()))
                .willThrow(new InvalidOrderException("Don hang khong hop le"));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId":"C-01","items":[{"productId":"P-01","quantity":1}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.message").value("Don hang khong hop le"));
    }
}

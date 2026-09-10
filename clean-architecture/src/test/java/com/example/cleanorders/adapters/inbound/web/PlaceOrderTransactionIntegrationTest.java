package com.example.cleanorders.adapters.inbound.web;

import com.example.cleanorders.adapters.outbound.persistence.springdata.SpringDataOrderJpaRepository;
import com.example.cleanorders.application.order.port.out.InventoryGateway;
import com.example.cleanorders.domain.order.entity.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Chung minh TINH NGUYEN TU that su tren H2:
 * neu mot buoc phia sau hong thi ban ghi don da luo truoc do phai bi rollback.
 *
 * Neu {@code TransactionalPlaceOrderInputBoundary} bi go ra, moi lan goi cong ra se tu mo
 * giao dich rieng va bai test nay se that bai vi don van nam lai trong CSDL.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PlaceOrderTransactionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SpringDataOrderJpaRepository orderJpaRepository;

    @MockBean
    private InventoryGateway inventoryGateway;

    @Test
    @DisplayName("Giu hang that bai -> don hang KHONG duoc luu lai (rollback toan bo)")
    void rollbackOrderWhenReserveFails() throws Exception {
        given(inventoryGateway.findProduct("P-01")).willReturn(Optional.of(
                new InventoryGateway.ProductInfo("P-01", "Ban phim co", Money.of("120.00"), 10)));
        willThrow(new IllegalStateException("He thong kho loi"))
                .given(inventoryGateway).reserve(anyString(), anyInt());

        long before = orderJpaRepository.count();

        assertThrows(Exception.class, () -> mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"customerId":"C-01","items":[{"productId":"P-01","quantity":2}]}
                        """)));

        assertEquals(before, orderJpaRepository.count(),
                "Don hang phai bi rollback khi buoc giu hang that bai");
    }
}

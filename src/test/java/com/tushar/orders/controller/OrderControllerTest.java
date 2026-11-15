package com.tushar.orders.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tushar.orders.dto.CreateOrderRequest;
import com.tushar.orders.exception.InvalidOrderStateException;
import com.tushar.orders.exception.OrderNotFoundException;
import com.tushar.orders.model.Order;
import com.tushar.orders.model.OrderItem;
import com.tushar.orders.model.OrderStatus;
import com.tushar.orders.service.OrderService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService service;

    private static Order sampleOrder() {
        return new Order("Ravi Kumar", List.of(
                new OrderItem("whitening kit", 2, new BigDecimal("10.00"))));
    }

    @Test
    void create_validRequest_returns201WithComputedTotal() throws Exception {
        when(service.createOrder(any(CreateOrderRequest.class))).thenReturn(sampleOrder());

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerName": "Ravi Kumar", "items": [
                                  {"productName": "whitening kit", "quantity": 2, "unitPrice": 10.00}
                                ]}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerName").value("Ravi Kumar"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(20.00));
    }

    @Test
    void create_emptyItems_returns400() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerName": "Ravi Kumar", "items": []}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void create_blankCustomerName_returns400() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerName": "", "items": [
                                  {"productName": "whitening kit", "quantity": 1, "unitPrice": 10.00}
                                ]}"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_zeroQuantity_returns400() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerName": "Ravi Kumar", "items": [
                                  {"productName": "whitening kit", "quantity": 0, "unitPrice": 10.00}
                                ]}"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_existing_returns200() throws Exception {
        when(service.getOrderById(1L)).thenReturn(sampleOrder());

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value("Ravi Kumar"));
    }

    @Test
    void getById_missing_returns404() throws Exception {
        when(service.getOrderById(99L)).thenThrow(new OrderNotFoundException(99L));

        mockMvc.perform(get("/api/orders/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("order not found: 99"));
    }

    @Test
    void list_returnsPageOfOrders() throws Exception {
        when(service.listOrders(eq(null), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(sampleOrder())));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].customerName").value("Ravi Kumar"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void updateStatus_legalTransition_returns200() throws Exception {
        Order confirmed = sampleOrder();
        confirmed.setStatus(OrderStatus.CONFIRMED);
        when(service.updateStatus(eq(1L), eq(OrderStatus.CONFIRMED))).thenReturn(confirmed);

        mockMvc.perform(put("/api/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "CONFIRMED"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void updateStatus_illegalTransition_returns409() throws Exception {
        when(service.updateStatus(eq(1L), eq(OrderStatus.DELIVERED)))
                .thenThrow(new InvalidOrderStateException(OrderStatus.PENDING, OrderStatus.DELIVERED));

        mockMvc.perform(put("/api/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "DELIVERED"}"""))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("cannot move order from PENDING to DELIVERED"));
    }
}

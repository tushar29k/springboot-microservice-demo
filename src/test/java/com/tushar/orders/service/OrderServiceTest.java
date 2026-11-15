package com.tushar.orders.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tushar.orders.dto.CreateOrderRequest;
import com.tushar.orders.dto.OrderItemRequest;
import com.tushar.orders.exception.InvalidOrderStateException;
import com.tushar.orders.exception.OrderNotFoundException;
import com.tushar.orders.model.Order;
import com.tushar.orders.model.OrderItem;
import com.tushar.orders.model.OrderStatus;
import com.tushar.orders.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository repository;

    @InjectMocks
    private OrderService service;

    private static CreateOrderRequest twoItemRequest() {
        return new CreateOrderRequest("Ravi Kumar", List.of(
                new OrderItemRequest("whitening kit", 2, new BigDecimal("10.00")),
                new OrderItemRequest("toothpaste", 1, new BigDecimal("5.50"))));
    }

    @Test
    void createOrder_computesTotalFromLineItems() {
        when(repository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order saved = service.createOrder(twoItemRequest());

        assertEquals(new BigDecimal("25.50"), saved.getTotalAmount());
        assertEquals(OrderStatus.PENDING, saved.getStatus());
        assertEquals(2, saved.getItems().size());
    }

    @Test
    void createOrder_persistsTheOrder() {
        when(repository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        service.createOrder(twoItemRequest());

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(repository).save(captor.capture());
        assertEquals("Ravi Kumar", captor.getValue().getCustomerName());
    }

    @Test
    void getOrderById_returnsOrderWhenPresent() {
        Order order = new Order("Ravi Kumar", List.of(
                new OrderItem("whitening kit", 1, new BigDecimal("10.00"))));
        when(repository.findById(7L)).thenReturn(Optional.of(order));

        assertEquals(order, service.getOrderById(7L));
    }

    @Test
    void getOrderById_throwsWhenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> service.getOrderById(99L));
    }

    @Test
    void updateStatus_allowsLegalTransition() {
        Order order = new Order("Ravi Kumar", List.of(
                new OrderItem("whitening kit", 1, new BigDecimal("10.00"))));
        when(repository.findById(1L)).thenReturn(Optional.of(order));
        when(repository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order updated = service.updateStatus(1L, OrderStatus.CONFIRMED);

        assertEquals(OrderStatus.CONFIRMED, updated.getStatus());
    }

    @Test
    void updateStatus_rejectsIllegalTransition() {
        Order order = new Order("Ravi Kumar", List.of(
                new OrderItem("whitening kit", 1, new BigDecimal("10.00"))));
        when(repository.findById(1L)).thenReturn(Optional.of(order));

        // PENDING -> DELIVERED skips CONFIRMED and SHIPPED: not allowed.
        assertThrows(InvalidOrderStateException.class,
                () -> service.updateStatus(1L, OrderStatus.DELIVERED));
    }

    @Test
    void updateStatus_throwsWhenOrderMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
                () -> service.updateStatus(99L, OrderStatus.CONFIRMED));
    }

    @Test
    void listOrders_withoutStatus_listsAll() {
        Page<Order> page = new PageImpl<>(List.of());
        when(repository.findAll(any(PageRequest.class))).thenReturn(page);

        assertEquals(page, service.listOrders(null, PageRequest.of(0, 20)));
    }

    @Test
    void listOrders_withStatus_filtersByStatus() {
        Page<Order> page = new PageImpl<>(List.of());
        when(repository.findByStatus(OrderStatus.PENDING, PageRequest.of(0, 20))).thenReturn(page);

        assertEquals(page, service.listOrders(OrderStatus.PENDING, PageRequest.of(0, 20)));
    }
}

package com.tushar.orders.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tushar.orders.model.Order;
import com.tushar.orders.model.OrderItem;
import com.tushar.orders.model.OrderStatus;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class OrderRepositoryTest {

    @Autowired
    private OrderRepository repository;

    private static Order order(String customer, OrderStatus status) {
        Order o = new Order(customer, List.of(
                new OrderItem("whitening kit", 2, new BigDecimal("10.00"))));
        o.setStatus(status);
        return o;
    }

    @Test
    void save_persistsOrderWithItemsAndTotal() {
        Order saved = repository.save(order("Ravi Kumar", OrderStatus.PENDING));

        assertTrue(saved.getId() != null);
        assertEquals(new BigDecimal("20.00"), saved.getTotalAmount());
        assertEquals(1, saved.getItems().size());
        assertEquals("whitening kit", saved.getItems().get(0).getProductName());
    }

    @Test
    void findByStatus_returnsOnlyMatchingOrders() {
        repository.save(order("Ravi Kumar", OrderStatus.PENDING));
        repository.save(order("Anita Sharma", OrderStatus.CONFIRMED));
        repository.save(order("Vikram Rao", OrderStatus.PENDING));

        List<Order> pending = repository
                .findByStatus(OrderStatus.PENDING, PageRequest.of(0, 10)).getContent();

        assertEquals(2, pending.size());
        assertTrue(pending.stream().allMatch(o -> o.getStatus() == OrderStatus.PENDING));
    }
}

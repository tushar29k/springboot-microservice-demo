package com.tushar.orders.service;

import com.tushar.orders.dto.CreateOrderRequest;
import com.tushar.orders.dto.OrderItemRequest;
import com.tushar.orders.exception.InvalidOrderStateException;
import com.tushar.orders.exception.OrderNotFoundException;
import com.tushar.orders.model.Order;
import com.tushar.orders.model.OrderItem;
import com.tushar.orders.model.OrderStatus;
import com.tushar.orders.repository.OrderRepository;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderService {

    /** Legal status transitions. Anything else is a 409. */
    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = new EnumMap<>(OrderStatus.class);

    static {
        TRANSITIONS.put(OrderStatus.PENDING, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED));
        TRANSITIONS.put(OrderStatus.CONFIRMED, Set.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED));
        TRANSITIONS.put(OrderStatus.SHIPPED, Set.of(OrderStatus.DELIVERED));
        TRANSITIONS.put(OrderStatus.DELIVERED, Set.of());
        TRANSITIONS.put(OrderStatus.CANCELLED, Set.of());
    }

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    public Order createOrder(CreateOrderRequest request) {
        List<OrderItem> items = request.items().stream()
                .map(this::toEntity)
                .toList();
        return repository.save(new Order(request.customerName(), items));
    }

    /** Single-order reads are cached (Redis in prod, in-memory in tests). */
    @Transactional(readOnly = true)
    @Cacheable(value = "orders", key = "#id")
    public Order getOrderById(Long id) {
        return repository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<Order> listOrders(OrderStatus status, Pageable pageable) {
        if (status == null) {
            return repository.findAll(pageable);
        }
        return repository.findByStatus(status, pageable);
    }

    /** Status change evicts the cached copy so the next read hits the db. */
    @CacheEvict(value = "orders", key = "#id")
    public Order updateStatus(Long id, OrderStatus newStatus) {
        Order order = repository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        if (!TRANSITIONS.getOrDefault(order.getStatus(), Set.of()).contains(newStatus)) {
            throw new InvalidOrderStateException(order.getStatus(), newStatus);
        }
        order.setStatus(newStatus);
        return repository.save(order);
    }

    private OrderItem toEntity(OrderItemRequest item) {
        return new OrderItem(item.productName(), item.quantity(), item.unitPrice());
    }
}

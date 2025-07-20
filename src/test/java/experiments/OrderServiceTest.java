package experiments;

import experiments.dto.OrderResponse;
import experiments.repository.OrderRepository;
import experiments.services.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OrderServiceTest {
    private OrderRepository orderRepository;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepository = new OrderRepository();
        orderService = new OrderService(orderRepository);
    }

    @Test
    @DisplayName("Test create order")
    void givenOrder_whenCreateOrder_thenOrderCreated() {
        OrderResponse response = orderService.createOrder("Alice", 100.0);

        assertNotNull(response);
        assertEquals("Alice", response.getCustomerName());
        assertEquals(BigDecimal.valueOf(100.0), response.getTotalAmount());
        System.out.println("Created order: " + response.getCustomerName() + " - " + response.getTotalAmount());
    }

    @Test
    @DisplayName("Test create two orders")
    void givenTwoOrders_whenProcessOrders_thenTwoOrders() throws InterruptedException {
        orderService.createOrder("Alice", 100.0);
        orderService.createOrder("Bob", 200.0);

        List<OrderResponse> responses = orderService.processOrders();

        for (OrderResponse res : responses) {
            assertTrue(res.isProcessed());
        }

        System.out.println("Total processed orders: " + responses.size());
    }
}

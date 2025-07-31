package experiments;

import experiments.dto.OrderResponse;
import experiments.repository.OrderRepository;
import experiments.services.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class OrderServiceTest {
    private static final Logger log = LoggerFactory.getLogger(OrderServiceTest.class);

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

    @Test
    @DisplayName("Create 10_000_000 orders")
    void givenThousandOrders_whenProcessOrders_thenThousandOrders() throws InterruptedException {
        long start = System.currentTimeMillis();
        log.info("Создание заказов");
        for (int i = 0; i < 10000000; i++) {
            orderService.createOrder("Customer",150.0);
        }
        log.info("Начало проверки обработанных заказов");
        List<OrderResponse> responses = orderService.processOrders();

        System.out.println("Всего заказов: " + responses.size());
        log.info("Заказы обработались за " + (System.currentTimeMillis() - start) + " ms");
    }
}

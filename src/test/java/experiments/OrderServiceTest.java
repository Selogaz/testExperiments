package experiments;

import config.PrometheusRegistryTestConfig;
import experiments.dto.OrderResponse;
import experiments.repository.OrderRepository;
import experiments.services.OrderService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@Import(PrometheusRegistryTestConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
//@ActiveProfiles("test")
public class OrderServiceTest {
    private static final Logger log = LoggerFactory.getLogger(OrderServiceTest.class);

    @Autowired
    private OrderService orderService;
    @Autowired
    private MeterRegistry meterRegistry;



//    @BeforeEach
//    void setUp() {
//        orderRepository = new OrderRepository();
//        orderService = new OrderService(orderRepository, meterRegistry);
//    }

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
        for (int i = 0; i < 300000; i++) {
            orderService.createOrder("Customer",150.0);
        }
        log.info("Начало проверки обработанных заказов");
        List<OrderResponse> responses = orderService.processOrders();

        System.out.println("Всего заказов: " + responses.size());
        log.info("Заказы обработались за " + (System.currentTimeMillis() - start) + " ms");
        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Test metrics for create order")
    void givenOrder_whenCreateOrder_thenMetricsUpdated() {

        orderService.createOrder("Alice", 100.0);

        Counter counter = meterRegistry.counter("orders.created");
        assertEquals(1, counter.count(), "Счетчик должен быть равен 1");
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

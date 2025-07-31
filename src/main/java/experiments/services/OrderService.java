package experiments.services;

import experiments.dto.OrderResponse;
import experiments.model.OrderModel;
import experiments.repository.OrderRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ExecutorService executor = Executors.newWorkStealingPool();
    AtomicInteger orderCounter = new AtomicInteger(0);
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final Counter createdOrdersCounter;
    private final Timer processOrdersTimer;


    public OrderService(OrderRepository orderRepository, MeterRegistry meterRegistry) {
        this.orderRepository = orderRepository;
        this.createdOrdersCounter = Counter.builder("orders.created")
                .description("Total number of orders created")
                .register(meterRegistry);
        this.processOrdersTimer = Timer.builder("orders.processing.time")
                .description("Time taken to process orders")
                .register(meterRegistry);
    }

    public OrderResponse createOrder(String customerName, double amount) {
        OrderModel savedOrder = orderRepository.save(new OrderModel(null,
                customerName, BigDecimal.valueOf(amount)));
        createdOrdersCounter.increment();
        return mapToResponse(savedOrder);
    }

    public List<OrderResponse> processOrders() throws InterruptedException {
        List<OrderModel> orders = orderRepository.findAll();
        CountDownLatch latch = new CountDownLatch(orders.size());

        return processOrdersTimer.record(() -> {
        for (OrderModel order : orders) {
            executor.submit(() -> {
                try {
                    processOrder(order);
                } catch (Exception e) {
                    System.out.println("Ошибка при обработке заказа c id " + order.getId());
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        });
    }

    private void processOrder(OrderModel order) {
        int currentCounter = orderCounter.incrementAndGet();
        if (currentCounter % 10000 == 0) {
            log.info("Обработано {} заказов", currentCounter);
        }
        order.setProcessed(true);

    }

    private OrderResponse mapToResponse(OrderModel order) {
        return new OrderResponse(order.getId(), order.getCustomerName(),
                order.getTotalAmount(), order.isProcessed());
    }
}

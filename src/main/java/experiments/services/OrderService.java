package experiments.services;

import experiments.dto.OrderResponse;
import experiments.model.OrderModel;
import experiments.repository.OrderRepository;
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


    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public OrderResponse createOrder(String customerName, double amount) {
        OrderModel savedOrder = orderRepository.save(new OrderModel(null,
                customerName, BigDecimal.valueOf(amount)));
        return mapToResponse(savedOrder);
    }

    public List<OrderResponse> processOrders() throws InterruptedException {
        List<OrderModel> orders = orderRepository.findAll();
        CountDownLatch latch = new CountDownLatch(orders.size());

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

        latch.await();
        return orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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

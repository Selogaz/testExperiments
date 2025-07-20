package experiments.services;

import experiments.dto.OrderResponse;
import experiments.model.OrderModel;
import experiments.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class OrderService {
    private final OrderRepository orderRepository;
    private final ExecutorService executor = Executors.newWorkStealingPool();

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
        order.setProcessed(true);
        System.out.println("Processed order ID: " + order.getId());
    }

    private OrderResponse mapToResponse(OrderModel order) {
        return new OrderResponse(order.getId(), order.getCustomerName(),
                order.getTotalAmount(), order.isProcessed());
    }
}

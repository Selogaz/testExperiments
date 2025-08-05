package experiments.controllers;

import experiments.dto.OrderRequest;
import experiments.dto.OrderResponse;
import experiments.model.OrderModel;
import experiments.repository.OrderRepository;
import experiments.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @Autowired
    public OrderController(OrderService orderService, OrderRepository orderRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
    }

    @GetMapping
    public List<OrderModel> getAllOrders() {
        return orderRepository.findAll();
    }


    @PostMapping
    public OrderResponse createOrder(@RequestBody OrderRequest request) {
        return orderService.createOrder(request.getCustomerName(), request.getTotalAmount().doubleValue());
    }

    @GetMapping("/process")
    public List<OrderResponse> getOrdersList() throws InterruptedException {
        return orderService.processOrders();
    }
}

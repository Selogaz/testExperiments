package experiments.controllers;

import experiments.dto.OrderRequest;
import experiments.dto.OrderResponse;
import experiments.services.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public OrderResponse createOrder(@RequestBody OrderRequest request) {
        return orderService.createOrder(request.getCustomerName(), request.getTotalAmount().doubleValue());
    }

    @GetMapping("/process")
    public List<OrderResponse> processOrders() throws InterruptedException {
        return orderService.processOrders();
    }
}

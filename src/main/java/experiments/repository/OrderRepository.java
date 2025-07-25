package experiments.repository;

import experiments.model.OrderModel;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class OrderRepository {
    private final ConcurrentHashMap<Long, OrderModel> orders = new ConcurrentHashMap();
    private final AtomicLong idCounter = new AtomicLong(1);

    public OrderModel save(OrderModel order) {
        try {
            Long id = order.getId();
            OrderModel newOrder = new OrderModel(idCounter.getAndIncrement(), order.getCustomerName(), order.getTotalAmount());
            orders.put(id, newOrder);
            return newOrder;
        } catch (Exception e) {
            System.out.println("Ошибка при сохранении заказа с id " + order.getId());
            e.printStackTrace();
            return null;
        }
    }

    public List<OrderModel> findAll() {
        return new ArrayList<>(orders.values());
    }
}

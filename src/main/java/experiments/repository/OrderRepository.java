package experiments.repository;

import experiments.model.OrderModel;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

@Repository
public class OrderRepository {
    private final List<OrderModel> orders = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);
    private final ReentrantLock lock = new ReentrantLock();

    public OrderModel save(OrderModel order) {
        lock.lock();
        try {
            OrderModel newOrder = new OrderModel(idCounter.getAndIncrement(), order.getCustomerName(), order.getTotalAmount());
            orders.add(newOrder);
            return newOrder;
        } finally {
            lock.unlock();
        }
    }

    public List<OrderModel> findAll() {
        return new ArrayList<>(orders);
    }
}

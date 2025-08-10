package experiments.repository;

import experiments.dto.OrderRowMapper;
import experiments.model.OrderModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class OrderRepository  {
    private final ConcurrentHashMap<Long, OrderModel> orders = new ConcurrentHashMap();
    private final AtomicLong idCounter = new AtomicLong(1);
    private final JdbcTemplate jdbcTemplate;
    private TransactionTemplate transactionTemplate;

    @Autowired
    public OrderRepository(JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
    }

    public List<OrderModel> findAll() {
        String sql = "Select id, customer_name, total_amount, is_processed FROM orders";
        return jdbcTemplate.query(sql, new OrderRowMapper());
    }


    public void save(OrderModel order) {
        String sql = "INSERT INTO orders (customer_name, total_amount) VALUES (?,?)";
        jdbcTemplate.update(sql, order.getCustomerName(), order.getTotalAmount());
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM orders WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public void deleteAll() {
        String sql = "DELETE FROM orders";
        jdbcTemplate.update(sql);
    }

    public void executeWithIsolation(int isolationLevel, Runnable operation) {
        TransactionTemplate template = new TransactionTemplate(transactionTemplate.getTransactionManager());
        template.setIsolationLevel(isolationLevel);
        template.execute(status -> {
            operation.run();
            return null;
        });
    }

    public OrderModel saveOld(OrderModel order) {
        try {
            OrderModel newOrder = new OrderModel(idCounter.getAndIncrement(),
                    order.getCustomerName(), order.getTotalAmount());
            Long id = newOrder.getId();
            orders.put(id, newOrder);
            return newOrder;
        } catch (Exception e) {
            System.out.println("Ошибка при сохранении заказа с id " + order.getId());
            e.printStackTrace();
            return new OrderModel(null,null,null);
        }
    }

    public List<OrderModel> findAllOld() {
        return new ArrayList<>(orders.values());
    }


}

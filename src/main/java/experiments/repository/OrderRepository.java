package experiments.repository;

import com.zaxxer.hikari.util.IsolationLevel;
import experiments.config.TransactionTemplateConfig;
import experiments.dto.OrderRowMapper;
import experiments.model.OrderModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.Transactional;
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
    private final TransactionTemplate transactionTemplate;

    @Autowired
    public OrderRepository(JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
    }

    public List<OrderModel> findAll() {
        String sql = "Select id, customer_name, total_amount, is_processed FROM orders";
        return jdbcTemplate.query(sql, new OrderRowMapper());
    }

    public OrderModel findById(Long id) {
        String sql = "Select id, customer_name, total_amount, is_processed FROM orders WHERE id = ? FOR UPDATE";
        return jdbcTemplate.queryForObject(sql, new OrderRowMapper(),id);
    }

    public void save(OrderModel order) {
        String sql = "INSERT INTO orders (customer_name, total_amount) VALUES (?,?)";
//        String sql = "INSERT INTO orders (id, customer_name, total_amount)" +
//                "        VALUES (?, ?, ?)" +
//                "        ON CONFLICT (id)" +
//                "        DO UPDATE SET" +
//                "            customer_name = EXCLUDED.customer_name";
        jdbcTemplate.update(sql, order.getId(), order.getCustomerName(), order.getTotalAmount());
    }

    @Transactional
    public void update(OrderModel order) {
        String sql = "UPDATE orders " +
                "SET customer_name = ?, total_amount = ? " +
                "WHERE id = ?";
        jdbcTemplate.update(sql, order.getCustomerName(), order.getTotalAmount(),order.getId());
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
        TransactionTemplate transactionTemplateCustom = new TransactionTemplate(transactionTemplate.getTransactionManager());
        transactionTemplateCustom.setIsolationLevel(isolationLevel);
        transactionTemplateCustom.execute(status -> {
            try {
                operation.run();
                return null;
            } catch (Exception e) {
                status.setRollbackOnly();
                throw e;
            }

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

package experiments;

import experiments.model.OrderModel;
import experiments.repository.OrderRepository;
import experiments.services.OrderService;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class OrderRepositoryTest {

    private OrderRepository orderRepository;


    @Autowired
    public OrderRepositoryTest(OrderRepository orderRepository, TransactionTemplate transactionTemplate) {
        this.orderRepository = orderRepository;
    }

    @Test
    @DisplayName("jdbc test")
    void givenRequest_whenCreateQuery_thenTableChanged() {
        String url = "jdbc:postgresql://172.17.0.3:5432/mydatabase";
        String user = "myuser";
        String pass = "mypassword";
        try {
            Connection connection = DriverManager.getConnection(url, user, pass);
            Statement statement = connection.createStatement();
            statement.executeUpdate("INSERT INTO orders VALUES (3,'customer',55)");
            ResultSet resultSet = statement.executeQuery("SELECT * FROM orders");
            while (resultSet.next()) {
                String id = resultSet.getString("id");
                System.out.println(id);
            }
            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    @DisplayName("repo test")
    void givenQuery_whenFindAll_Old_thenTableChanged() throws SQLException {
        OrderModel newOrder = new OrderModel(5L,"new order",new BigDecimal(150));
        orderRepository.save(newOrder);
        List<OrderModel> newList = orderRepository.findAll();
        for (OrderModel order : newList) {
            System.out.printf("%d, %s, ",order.getId(), order.getCustomerName());
            System.out.println(order.getTotalAmount());
        }
        //orderRepository.deleteById(10L);
        //orderRepository.deleteAll();


    }

    @Test
    @DisplayName("One sees changes")
    void givenTwoReads_whenTransactionTwoCommitsChanges_thenOneSeesChanges() throws InterruptedException {
        testIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    @Test
    @DisplayName("One does not see changes")
    public void givenTwoReads_whenTransactionTwoCommitsChanges_thenOneDoesNotSeeChanges() throws InterruptedException {
        testIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
    }

    @Test
    @DisplayName("Exception or One does not see changes")
    public void givenTwoTransactions_whenSerializableConflict_thenThrowsSerializationException() throws InterruptedException {
        testIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
    }

    private void testIsolationLevel(int isolationLevel) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        //orderRepository.save(new OrderModel(55L,"new customer",new BigDecimal(600)));
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        AtomicInteger successCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);

        executor.submit(() -> {
            orderRepository.executeWithIsolation(isolationLevel, () -> {
                try {
                    System.out.println("BEGIN ISOLATION LEVEL SERIALIZABLE");
                    Thread.sleep(100);

                    System.out.println("Transaction A - SELECT");
                    OrderModel newOrderModel = orderRepository.findById(55L);
                    System.out.println("Transaction A - READ " + newOrderModel.getTotalAmount());
                    Thread.sleep(500);

                    latch.await();
                    newOrderModel.setTotalAmount(new BigDecimal(200));
                    orderRepository.update(newOrderModel);
                    System.out.println("Transaction A - UPDATE "+ newOrderModel.getTotalAmount());

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    System.err.println("Transaction A - ERROR: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            successCount.incrementAndGet();
        });

        executor.submit(() -> {
            try {
                orderRepository.executeWithIsolation(isolationLevel, () -> {
                try {
                    System.out.println("BEGIN ISOLATION LEVEL SERIALIZABLE");
                    Thread.sleep(100);

                    System.out.println("Transaction B - SELECT");
                    OrderModel newOrderModel = orderRepository.findById(55L);
                    System.out.println("Transaction B - READ " + newOrderModel.getTotalAmount());

                    latch.countDown();
                    Thread.sleep(5000);
                    newOrderModel.setTotalAmount(new BigDecimal(200));
                    orderRepository.update(newOrderModel);
                    System.out.println("Transaction B - UPDATE "+ newOrderModel.getTotalAmount());

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            successCount.incrementAndGet();
            } catch (Exception e) {
                System.err.println("Transaction B - ERROR: " + e.getMessage());
                if (e.getMessage().contains("could not serialize")) {
                    System.out.println("✅ SerializationException caught! ROLLBACK occurred.");
                }
                e.printStackTrace();
            }
        });

        executor.shutdown();
        executor.awaitTermination(100, TimeUnit.SECONDS);

        assertEquals(1, successCount.get());

    }
}



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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class OrderRepositoryTest {

    private OrderRepository orderRepository;
    private TransactionTemplate transactionTemplate;

    @Autowired
    public OrderRepositoryTest(OrderRepository orderRepository, TransactionTemplate transactionTemplate) {
        this.orderRepository = orderRepository;
        this.transactionTemplate = transactionTemplate;
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
    @Transactional(isolation = Isolation.READ_COMMITTED)
    void givenTwoReads_whenTransactionTwoCommitsChanges_thenOneSeesChanges() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.submit(()-> {
           List<OrderModel> firstList = orderRepository.findAll();
           for (OrderModel order : firstList) {
               System.out.println("Transaction A - First Read: " + order.getId() + order.getCustomerName());
           }
           try {
                Thread.sleep(5000);
           } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
           }
           List<OrderModel> secondList = orderRepository.findAll();
            for (OrderModel order : secondList) {
                System.out.println("Transaction A - Second Read: " + order.getId() + " " + order.getCustomerName());
            }
        });

        executor.submit(()-> {
            try {
                Thread.sleep(2000);
                OrderModel bOrder = new OrderModel();
                bOrder.setCustomerName("read_commited");
                bOrder.setTotalAmount(new BigDecimal(400));
                orderRepository.save(bOrder);
                System.out.println("Transaction B - New order saved");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
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
        TransactionTemplate templateA = new TransactionTemplate(transactionTemplate.getTransactionManager());
        templateA.setIsolationLevel(isolationLevel);

        TransactionTemplate templateB = new TransactionTemplate(transactionTemplate.getTransactionManager());
        templateB.setIsolationLevel(isolationLevel);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.submit(() -> {
            templateA.execute(status -> {
                List<OrderModel> firstList = orderRepository.findAll();
                for (OrderModel order : firstList) {
                    System.out.println("Transaction A - First Read: " + order.getId() + " " + order.getCustomerName());
                }
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                List<OrderModel> secondList = orderRepository.findAll();
                for (OrderModel order : secondList) {
                    System.out.println("Transaction A - Second Read: " + order.getId() + " " + order.getCustomerName());
                }

                return null;
            });
        });

        executor.submit(() -> {
            templateB.execute(status -> {
                try {
                    Thread.sleep(1000); // Ждём, пока транзакция A начнёт чтение
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                OrderModel bOrder = new OrderModel();
                bOrder.setCustomerName("serializable");
                bOrder.setTotalAmount(new BigDecimal(400));
                orderRepository.save(bOrder);
                System.out.println("Transaction B - New order saved");

                return null;
            });
        });

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }
}



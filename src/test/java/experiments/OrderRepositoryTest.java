package experiments;

import experiments.model.OrderModel;
import experiments.repository.OrderRepository;
import experiments.services.OrderService;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class OrderRepositoryTest {

    private OrderRepository orderRepository;

    @Autowired
    public OrderRepositoryTest(OrderRepository orderRepository) {
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
}

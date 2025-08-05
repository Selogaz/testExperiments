package experiments;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

@SpringBootTest
public class OrderRepositoryTest {

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

}

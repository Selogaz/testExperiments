import experiments.dto.HelloResponse;
import experiments.services.HelloService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HelloServiceTest {
    private HelloService helloService;

    @BeforeEach
    void setUp() {
        helloService = new HelloService();
    }

    @Test
    void testSayHello() {
        String name = "Alice";
        HelloResponse response = helloService.sayHello(name);

        System.out.println("Response message: " + response.getMessage());

        assertNotNull(response);
        assertEquals("Hello, Alice!", response.getMessage());
    }
}

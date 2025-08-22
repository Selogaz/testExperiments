package hello;

import hello.dto.HelloResponse;
import hello.services.HelloService;
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
    void givenAlice_whenSayHello_thenHelloAlice() {
        String name = "Alice";
        HelloResponse response = helloService.sayHello(name);

        System.out.println("Response message: " + response.getMessage());

        assertNotNull(response);
        assertEquals("Hello, Alice!", response.getMessage());
    }
}

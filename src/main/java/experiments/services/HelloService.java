package experiments.services;

import experiments.dto.HelloResponse;
import org.springframework.stereotype.Service;

@Service
public class HelloService {
    public HelloResponse sayHello(String name) {
        return new HelloResponse("Hello, " + name + "!");
    }
}

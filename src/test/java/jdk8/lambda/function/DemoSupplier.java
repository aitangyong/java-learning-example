package jdk8.lambda.function;

import java.util.UUID;
import java.util.function.Supplier;

public class DemoSupplier {
    
    public static void main(String[] args) {
        Supplier<String> uuidGenerator = () -> UUID.randomUUID().toString();
        
        System.out.println(uuidGenerator.get());
        System.out.println(uuidGenerator.get());
    }
}

package com.quant.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class QuantBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuantBackendApplication.class, args);
    }
}


package com.example.jpaoptimizationlab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class JpaOptimizationLabApplication {

    public static void main(String[] args) {
        SpringApplication.run(JpaOptimizationLabApplication.class, args);
    }

}

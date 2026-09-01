package com.bmaas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class BmaasApplication {

    public static void main(String[] args) {
        SpringApplication.run(BmaasApplication.class, args);
    }
}

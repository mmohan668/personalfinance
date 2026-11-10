package com.pf.flyway;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = {"com.pf.common.config.vault", "com.pf.common.config.datasource"})
public class FlywayApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlywayApplication.class, args);
    }

    @Bean
    CommandLineRunner shutdownApplication(ConfigurableApplicationContext context) {
        return args -> SpringApplication.exit(context, () -> 0);
    }

}

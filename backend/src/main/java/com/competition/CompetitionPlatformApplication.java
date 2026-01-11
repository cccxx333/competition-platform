package com.competition;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableJpaRepositories
@EnableTransactionManagement
public class CompetitionPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompetitionPlatformApplication.class, args);
    }
}
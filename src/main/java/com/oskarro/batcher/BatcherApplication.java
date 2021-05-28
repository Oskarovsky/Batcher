package com.oskarro.batcher;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableBatchProcessing
@SpringBootApplication
public class BatcherApplication {

    public static void main(String[] args) {
        SpringApplication.run(BatcherApplication.class, args);
    }

}

package com.oskarro.batcher.batch.synchronizeProducts;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.listener.ItemListenerSupport;
import org.springframework.batch.item.ItemProcessor;

import java.util.Objects;

public class ProductItemClassifier implements JobExecutionDecider {


    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        String productType = jobExecution.getJobParameters().getString("productType");
        if (Objects.requireNonNull(productType).equalsIgnoreCase("COMPUTER")) {
            return new FlowExecutionStatus("COMPUTER");
        } else if (Objects.requireNonNull(productType).equalsIgnoreCase("CONSOLE")) {
            return new FlowExecutionStatus("CONSOLE");
        } else {
            return new FlowExecutionStatus("PRODUCT");
        }
    }

}

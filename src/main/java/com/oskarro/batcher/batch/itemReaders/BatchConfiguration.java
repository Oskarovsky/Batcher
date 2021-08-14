package com.oskarro.batcher.batch.itemReaders;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

@EnableAutoConfiguration
@EnableBatchProcessing
@Configuration
public class BatchConfiguration {

    public final JobBuilderFactory jobBuilderFactory;
    public final StepBuilderFactory stepBuilderFactory;

    public BatchConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Job readCustomersFromFileJob() {
        return this.jobBuilderFactory
                .get("readCustomersFromFileJob")
                .incrementer(new RunIdIncrementer())
                .start(copyFileStep())
                .build();
    }

    @Bean
    public Step copyFileStep() {
        return this.stepBuilderFactory
                .get("copyFileStep")
                .<Customer, Customer>chunk(10)
                .reader(customerFlatFileItemReader(null))
                .writer(itemWriter())
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<Customer> customerFlatFileItemReader(@Value("#{jobParameters['customerFile']}")String inputFile) {
        return new FlatFileItemReaderBuilder<Customer>()
                .name("customerFlatFileItemReader")
                .resource(new ByteArrayResource(inputFile.getBytes()))
                .delimited()
                .names("firstName", "lastName", "addressNumber", "street", "city", "zipCode")
                .linesToSkip(1)
                .targetType(Customer.class)
                .build();
    }

    @Bean
    public ItemWriter<Customer> itemWriter() {
        return (items) -> items.forEach(System.out::println);
    }
}

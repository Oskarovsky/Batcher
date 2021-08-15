package com.oskarro.batcher.batch.itemReaders;

import com.oskarro.batcher.environment.main.model.cargo.Computer;
import com.oskarro.batcher.environment.main.model.cargo.Console;
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
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.mapping.PatternMatchingCompositeLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.batch.item.file.transform.PatternMatchingCompositeLineTokenizer;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

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
                .<CustomerTemp, CustomerTemp>chunk(10)
                .reader(customerFlatFileItemReader(null))
                .writer(itemWriter())
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<CustomerTemp> customerFlatFileItemReader(@Value("#{jobParameters['fileContent']}")String inputFile) {
        return new FlatFileItemReaderBuilder<CustomerTemp>()
                .name("customerFlatFileItemReader")
                .resource(new ByteArrayResource(inputFile.getBytes()))
                .delimited()
                .names("firstName", "lastName", "addressNumber", "street", "city", "zipCode")
                .linesToSkip(1)
                .fieldSetMapper(new CustomerFieldSetMapper())
                .build();
    }

    @Bean
    public ItemWriter<CustomerTemp> itemWriter() {
        return (items) -> items.forEach(System.out::println);
    }




    @Bean
    public Job readMultipleRecordsFromFileJob() {
        return this.jobBuilderFactory
                .get("readMultipleRecordsFromFileJob")
                .incrementer(new RunIdIncrementer())
                .start(getMultipleRecordsFromFileStep())
                .build();
    }

    @Bean
    public Step getMultipleRecordsFromFileStep() {
        return this.stepBuilderFactory
                .get("getMultipleRecordsFromFileStep")
                .chunk(10)
                .reader(customerFlatFileItemReader(null))
                .writer(multipleRecordWriter())
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader multipleRecordReader(@Value("#{jobParameters['fileContent']}") String content) {
        return new FlatFileItemReaderBuilder<Object>()
                .name("multipleRecordReader")
                .lineMapper(lineTokenizer())
                .resource(new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8)))
                .build();
    }

    @Bean
    public ItemWriter<Object> multipleRecordWriter() {
        return (items) -> items.forEach(System.out::println);
    }

    @Bean
    public PatternMatchingCompositeLineMapper lineTokenizer() {
        Map<String, LineTokenizer> lineTokenizers = new HashMap<>(2);
        lineTokenizers.put("COMP*", computerLineTokenizer());
        lineTokenizers.put("CONS*", consoleLineTokenizer());

        BeanWrapperFieldSetMapper<Computer> computerFieldSetMapper = new BeanWrapperFieldSetMapper<>();
        computerFieldSetMapper.setTargetType(Computer.class);

        Map<String, FieldSetMapper> fieldSetMappers = new HashMap<>(2);
        fieldSetMappers.put("COMP*", computerFieldSetMapper);
        fieldSetMappers.put("CONS*", new ConsoleFieldMapper());

        PatternMatchingCompositeLineMapper lineMappers = new PatternMatchingCompositeLineMapper();
        lineMappers.setTokenizers(lineTokenizers);
        lineMappers.setFieldSetMappers(fieldSetMappers);
        return lineMappers;
    }

    private DelimitedLineTokenizer consoleLineTokenizer() {
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setNames("prefix", "consoleId", "model", "producer", "price", "productStatus", "orderDate");
        lineTokenizer.setIncludedFields(1,2,3,4,5,6);
        return lineTokenizer;
    }

    private DelimitedLineTokenizer computerLineTokenizer() {
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setNames("prefix", "computerId", "name", "description", "model", "price", "productStatus", "orderDate");
        lineTokenizer.setIncludedFields(1,2,3,4,5,6,7);
        return lineTokenizer;
    }





}

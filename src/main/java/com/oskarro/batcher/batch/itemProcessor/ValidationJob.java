package com.oskarro.batcher.batch.itemProcessor;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.validator.BeanValidatingItemProcessor;
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.batch.jsr.item.ItemProcessorAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;

@EnableAutoConfiguration
@EnableBatchProcessing
@Configuration
public class ValidationJob {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Bean
    @StepScope
    public FlatFileItemReader<Client> clientItemReader(@Value("#{jobParameters['clientFile']}")String pathToFile) {
        return new FlatFileItemReaderBuilder<Client>()
                .name("clientItemReader")
                .resource(new ClassPathResource(pathToFile))
                .delimited()
                .names("firstName","lastName", "address", "city", "state", "zip", "phone")
                .targetType(Client.class)
                .build();
    }

    @Bean
    public UniqueLastNameValidator validator() {
        UniqueLastNameValidator validator = new UniqueLastNameValidator();
        validator.setName("validator");
        return validator;
    }

    @Bean
    public ValidatingItemProcessor<Client> clientValidatingItemProcessor() {
        ValidatingItemProcessor<Client> validatingItemProcessor = new ValidatingItemProcessor<>(validator());
        validatingItemProcessor.setFilter(true);
        return validatingItemProcessor;
    }


    @Bean
    public ItemWriter<Client> clientItemWriter() {
        return items -> items.forEach(System.out::println);
    }

    @Bean
    public BeanValidatingItemProcessor<Client> clientBeanValidatingItemProcessor() {
        return new BeanValidatingItemProcessor<>();
    }

    @Bean
    public Step copyClientFileStep() {
        return stepBuilderFactory
                .get("copyClientFileStep")
                .<Client, Client>chunk(5)
                .reader(clientItemReader(null))
                .processor(clientBeanValidatingItemProcessor())
                .writer(clientItemWriter())
                .stream(validator())
                .build();
    }

    @Bean
    public Job validationClientFileJob() throws Exception {
        return this.jobBuilderFactory
                .get("validationClientFileJob")
                .start(copyClientFileStep())
                .build();
    }
}

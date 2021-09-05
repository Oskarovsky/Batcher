package com.oskarro.batcher.batch.itemWriters;


import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;

@EnableAutoConfiguration
@EnableBatchProcessing
@Configuration
public class WriterConfig {


    public final JobBuilderFactory jobBuilderFactory;
    public final StepBuilderFactory stepBuilderFactory;

    public WriterConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    @StepScope
    public FlatFileItemReader<Player> playerFlatFileItemReader(
            @Value("#{jobParameters['playerFile']}")String inputFile) {
        return new FlatFileItemReaderBuilder<Player>()
                .name("playerFlatFileItemReader")
                .resource(new PathResource(inputFile))
                .delimited()
                .names("firstName", "lastName", "address", "city", "state", "zip", "phone")
                .targetType(Player.class)
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<Player> playerFlatFileItemWriter(
            @Value("#{jobParameters['outputFile']}")String outputFile) {
        return new FlatFileItemWriterBuilder<Player>()
                .name("playerFlatFileItemWriter")
                .resource(new PathResource(outputFile))
                .formatted()
                .format("%s %s lives at %s %s in %s, %s. (contact: %s)")
                .names("firstName", "lastName", "address", "city", "state", "zip", "phone")
                .build();
    }

    @Bean
    public Step formatStep() {
        return stepBuilderFactory
                .get("formatStep")
                .<Player, Player>chunk(5)
                .reader(playerFlatFileItemReader(null))
                .writer(playerFlatFileItemWriter(null))
                .build();
    }

    @Bean
    public Job formatPlayerJob() {
        return jobBuilderFactory
                .get("formatPlayerJob")
                .start(formatStep())
                .incrementer(new RunIdIncrementer())
                .build();
    }


}

package com.oskarro.batcher.batch.requestToDatabase;

import com.oskarro.batcher.model.main.Track;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class RequestToDatabaseJobConfig {

    private static final String WILL_BE_INJECTED = null;

    public final JobBuilderFactory jobBuilderFactory;
    public final StepBuilderFactory stepBuilderFactory;
    public final DataSource dataSource;

    public RequestToDatabaseJobConfig(JobBuilderFactory jobBuilderFactory,
                                      StepBuilderFactory stepBuilderFactory,
                                      DataSource dataSource) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.dataSource = dataSource;
    }

    @Bean(name = "requestToDatabaseJob")
    public Job requestToDatabaseJob() {
        return jobBuilderFactory.get("requestToDatabaseJob")
                .flow(requestToDatabaseStep())
                .end()
                .build();
    }

    @Bean(name = "requestToDatabaseStep")
    public Step requestToDatabaseStep() {
        return stepBuilderFactory.get("requestToDatabaseStep")
                .<Track, Track>chunk(5)
                .reader(requestTrackReader())
                .processor(requestTrackProcessor())
                .writer(requestTrackWriter())
                .build();
    }


    @Bean
    @StepScope
    public ItemReader<Track> requestTrackReader() {
        return null;
    }

    @Bean
    public ItemProcessor<Track, Track> requestTrackProcessor() {
        return null;
    }

    @Bean
    public ItemWriter<Track> requestTrackWriter() {
        JdbcBatchItemWriter<Track> requestTrackWriter = new JdbcBatchItemWriter<>();
        requestTrackWriter.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        requestTrackWriter.setSql(
                "INSERT INTO tracks (id, title, artist, version, url) " +
                        "VALUES (nextval('track_id_seq'), :title, :artist, :version, :url)");
        requestTrackWriter.setDataSource(dataSource);
        return requestTrackWriter;
    }

}

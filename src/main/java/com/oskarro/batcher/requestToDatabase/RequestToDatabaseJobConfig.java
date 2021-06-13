package com.oskarro.batcher.requestToDatabase;

import com.oskarro.batcher.batch.TrackItemReader;
import com.oskarro.batcher.model.Track;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class RequestToDatabaseJobConfig {

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

    @Bean
    public ItemReader<Track> itemReader() {
        return new TrackItemReader("1312312");
    }

    @Bean
    public ItemProcessor<Track, Track> itemProcessor() {
        //         return new CustomItemProcessor();
        return null;
    }

    @Bean
    public ItemWriter<Track> itemWriter() {
        return null;
    }

//    @Bean(name = "firstBatchJob")
//    public Job job1(@Qualifier("step1") Step step1) {
//        return null;
//    }

    @Bean
    protected Step step1() {
        return null;
    }
}

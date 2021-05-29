package com.oskarro.batcher.config;

import com.oskarro.batcher.batch.JobCompletionNotificationListener;
import com.oskarro.batcher.batch.TrackProcessor;
import com.oskarro.batcher.model.Track;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;

@EnableBatchProcessing
@Configuration
public class CsvFileToDatabaseConfig {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    public DataSource dataSource;

    @Bean
    public FlatFileItemReader<Track> csvTrackReader() {
        FlatFileItemReader<Track> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("tracklist.csv"));
        reader.setLineMapper(new DefaultLineMapper<>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames("title", "artist", "version", "url");
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                setTargetType(Track.class);
            }});
        }});
        return reader;
    }

    @Bean
    ItemProcessor<Track, Track> csvTrackProcessor() {
        return new TrackProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<Track> csvTrackWriter() {
        JdbcBatchItemWriter<Track> csvTrackWriter = new JdbcBatchItemWriter<>();
        csvTrackWriter.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        csvTrackWriter.setSql("INSERT INTO tracks (id, title, artist, version, url) VALUES (nextval('track_id_seq'), :title, :artist, :version, :url)");
        csvTrackWriter.setDataSource(dataSource);
        return csvTrackWriter;
    }

    // begin job info
    @Bean
    public Step csvFileToDatabaseStep() {
        return stepBuilderFactory.get("csvFileToDatabaseStep")
                .<Track, Track>chunk(1)
                .reader(csvTrackReader())
                .processor(csvTrackProcessor())
                .writer(csvTrackWriter())
                .build();
    }

    @Bean
    Job csvFileToDatabaseJob(JobCompletionNotificationListener listener) {
        return jobBuilderFactory.get("csvFileToDatabaseJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(csvFileToDatabaseStep())
                .end()
                .build();
    }


}

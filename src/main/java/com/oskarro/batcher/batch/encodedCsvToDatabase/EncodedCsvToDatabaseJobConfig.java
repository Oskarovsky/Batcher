package com.oskarro.batcher.batch.encodedCsvToDatabase;

import com.oskarro.batcher.batch.csvToDatabase.JobCompletionNotificationListener;
import com.oskarro.batcher.batch.csvToDatabase.TrackProcessor;
import com.oskarro.batcher.batch.synchronizeDatabase.config.MainDatabaseConfiguration;
import com.oskarro.batcher.config.DailyJobTimestamper;
import com.oskarro.batcher.environment.main.model.Track;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.listener.JobListenerFactoryBean;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableBatchProcessing
public class EncodedCsvToDatabaseJobConfig {

    private static final String WILL_BE_INJECTED = null;

    public final JobBuilderFactory jobBuilderFactory;
    public final StepBuilderFactory stepBuilderFactory;
    public final DataSource dataSource;
    public final MainDatabaseConfiguration mainDatabaseConfiguration;
    private final JdbcTemplate jdbcTemplate;

    public EncodedCsvToDatabaseJobConfig(JobBuilderFactory jobBuilderFactory,
                                         StepBuilderFactory stepBuilderFactory,
                                         DataSource dataSource,
                                         MainDatabaseConfiguration mainDatabaseConfiguration,
                                         JdbcTemplate jdbcTemplate) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.dataSource = dataSource;
        this.mainDatabaseConfiguration = mainDatabaseConfiguration;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Bean(name = "encodedCsvToDatabaseJob")
    public Job encodedCsvToDatabaseJob() {
        return jobBuilderFactory
                .get("encodedCsvToDatabaseJob")
                .incrementer(new DailyJobTimestamper())
                .listener(JobListenerFactoryBean.getListener(
                        new JobCompletionNotificationListener(
                                jdbcTemplate, "Decoding file content and saving tracks to database")))
                .flow(encodedCsvToDatabaseStep())
                .end()
                .build();
    }

    @Bean
    public Step encodedCsvToDatabaseStep() {
        return stepBuilderFactory
                .get("encodedCsvToDatabaseStep")
                .<Track, Track>chunk(5)
                .reader(encodedCsvReader(WILL_BE_INJECTED))
                .processor(encodedCsvProcessor())
                .writer(encodedCsvWriter())
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<Track> encodedCsvReader(@Value("#{jobParameters['decodedContent']}") String decodedContent) {
        FlatFileItemReader<Track> reader = new FlatFileItemReader<>();
        reader.setResource(new ByteArrayResource(decodedContent.getBytes(StandardCharsets.UTF_8)));
        reader.setLineMapper(new DefaultLineMapper<>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames("title", "artist", "version", "url", "code");
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                setTargetType(Track.class);
            }});
        }});
        return reader;
    }

    @Bean
    ItemProcessor<Track, Track> encodedCsvProcessor() {
        return new TrackProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<Track> encodedCsvWriter() {
        JdbcBatchItemWriter<Track> csvTrackWriter = new JdbcBatchItemWriter<>();
        csvTrackWriter.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        csvTrackWriter.setSql(
                "INSERT INTO tracks (id, title, artist, version, url, code) " +
                        "VALUES (nextval('track_id_seq'), :title, :artist, :version, :url, :code)");
        csvTrackWriter.setDataSource(mainDatabaseConfiguration.mainDataSource());
        return csvTrackWriter;
    }
}

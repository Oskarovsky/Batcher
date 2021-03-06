package com.oskarro.batcher.batch.csvToDatabase;

import com.oskarro.batcher.batch.synchronizeDatabase.config.MainDatabaseConfiguration;
import com.oskarro.batcher.config.DailyJobTimestamper;
import com.oskarro.batcher.config.LoggingStepStartStopListener;
import com.oskarro.batcher.environment.main.model.Track;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.CompositeJobParametersValidator;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Arrays;

@Configuration
@EnableBatchProcessing
public class CsvToDatabaseJobConfig {

    private static final String WILL_BE_INJECTED = null;

    public final JobBuilderFactory jobBuilderFactory;
    public final StepBuilderFactory stepBuilderFactory;
    public final DataSource dataSource;
    public final MainDatabaseConfiguration mainDatabaseConfiguration;
    private final JdbcTemplate jdbcTemplate;

    public CsvToDatabaseJobConfig(JobBuilderFactory jobBuilderFactory,
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


    @Bean(name = "csvToDatabaseJob")
    public Job csvToDatabaseJob() {
        return jobBuilderFactory
                .get("csvToDatabaseJob")
                .validator(csvTrackValidator())
                .incrementer(new DailyJobTimestamper())
                .listener(JobListenerFactoryBean.getListener(
                        new JobCompletionNotificationListener(jdbcTemplate, "Saving CSV content to database")))
                .flow(csvToDatabaseStep())
                .end()
                .build();
    }

    @Bean
    public Step csvToDatabaseStep() {
        return stepBuilderFactory
                .get("csvToDatabaseStep")
                .<Track, Track>chunk(5)
                .reader(csvTrackReader(WILL_BE_INJECTED))
                .processor(csvTrackProcessor())
                .writer(csvTrackWriter())
                .build();
    }

    @Bean
    public CompositeJobParametersValidator csvTrackValidator() {
        CompositeJobParametersValidator validator = new CompositeJobParametersValidator();
        DefaultJobParametersValidator defaultJobParametersValidator = new DefaultJobParametersValidator(
                new String[] {"fileName"},
                new String[] {"uniqueName", "currentDate"}
        );
        defaultJobParametersValidator.afterPropertiesSet();
        validator.setValidators(Arrays.asList(new ContentParameterValidator(), defaultJobParametersValidator));
        return validator;
    }

    @Bean
    @StepScope
    public FlatFileItemReader<Track> csvTrackReader(@Value("#{jobParameters['fileName']}") String csvFileName) {
        FlatFileItemReader<Track> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource(csvFileName));
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
    ItemProcessor<Track, Track> csvTrackProcessor() {
        return new TrackProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<Track> csvTrackWriter() {
        JdbcBatchItemWriter<Track> csvTrackWriter = new JdbcBatchItemWriter<>();
        csvTrackWriter.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        csvTrackWriter.setSql(
                "INSERT INTO tracks (id, title, artist, version, url, code) " +
                        "VALUES (nextval('track_id_seq'), :title, :artist, :version, :url, :code)");
        csvTrackWriter.setDataSource(mainDatabaseConfiguration.mainDataSource());
        return csvTrackWriter;
    }
}

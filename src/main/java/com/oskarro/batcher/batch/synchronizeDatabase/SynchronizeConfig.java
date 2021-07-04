package com.oskarro.batcher.batch.synchronizeDatabase;

import com.oskarro.batcher.batch.csvToDatabase.TrackProcessor;
import com.oskarro.batcher.batch.synchronizeDatabase.config.BackupDatabaseConfiguration;
import com.oskarro.batcher.batch.synchronizeDatabase.config.MainDatabaseConfiguration;
import com.oskarro.batcher.batch.synchronizeDatabase.service.BackupDatabaseService;
import com.oskarro.batcher.batch.synchronizeDatabase.service.MainDatabaseService;
import com.oskarro.batcher.environment.backup.repo.SongRepository;
import com.oskarro.batcher.environment.main.model.Track;
import com.oskarro.batcher.environment.main.model.TrackRowMapper;
import com.oskarro.batcher.environment.main.repo.TrackRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.tasklet.CallableTaskletAdapter;
import org.springframework.batch.core.step.tasklet.MethodInvokingTaskletAdapter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.concurrent.Callable;

@EnableAutoConfiguration
@EnableBatchProcessing
@Configuration
@Import({BackupDatabaseConfiguration.class, MainDatabaseConfiguration.class})
public class SynchronizeConfig {

    public final JobBuilderFactory jobBuilderFactory;
    public final StepBuilderFactory stepBuilderFactory;

    public final JdbcTemplate jdbcTemplate;

    public final BackupDatabaseConfiguration backupDatabaseConfiguration;
    public final MainDatabaseConfiguration mainDatabaseConfiguration;

    public final TrackRepository trackRepository;
    public final SongRepository songRepository;



    public SynchronizeConfig(JobBuilderFactory jobBuilderFactory,
                             StepBuilderFactory stepBuilderFactory,
                             JdbcTemplate jdbcTemplate,
                             BackupDatabaseConfiguration backupDatabaseConfiguration,
                             MainDatabaseConfiguration mainDatabaseConfiguration,
                             TrackRepository trackRepository,
                             SongRepository songRepository) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.jdbcTemplate = jdbcTemplate;
        this.backupDatabaseConfiguration = backupDatabaseConfiguration;
        this.mainDatabaseConfiguration = mainDatabaseConfiguration;
        this.trackRepository = trackRepository;
        this.songRepository = songRepository;
    }

    @Bean
    public Job synchronizeDatabaseJob() {
        return this.jobBuilderFactory
                .get("synchronizeDatabaseJob")
//                .start(printStep())
                .start(methodInvokingBackupStep())
                .next(methodInvokingMainStep())
//                .next(methodInvokingBackupStep())
                .next(databaseSynchronizationStep())
                .build();
    }

    @Bean
    public Step printStep() {
        return this.stepBuilderFactory
                .get("printStep")
                .tasklet(tasklet())
                .transactionManager(mainDatabaseConfiguration.mainTransactionManager())
                .build();
    }

    @Bean
    public Step methodInvokingMainStep() {
        return this.stepBuilderFactory
                .get("methodInvokingMainStep")
                .tasklet(methodInvokingTaskletAdapterForCountingMainRecords())
                .transactionManager(mainDatabaseConfiguration.mainTransactionManager())
                .build();
    }

    @Bean
    public Step methodInvokingBackupStep() {
        return this.stepBuilderFactory
                .get("methodInvokingBackupStep")
                .tasklet(methodInvokingTaskletAdapterForCountingBackupRecords())
                .transactionManager(backupDatabaseConfiguration.backupTransactionManager())
                .build();
    }

    @Bean
    MethodInvokingTaskletAdapter methodInvokingTaskletAdapterForCountingMainRecords() {
        MethodInvokingTaskletAdapter methodInvokingTaskletAdapter = new MethodInvokingTaskletAdapter();
        methodInvokingTaskletAdapter.setTargetObject(mainDatabaseService());
        methodInvokingTaskletAdapter.setTargetMethod("getNumberOfRecords");
        return methodInvokingTaskletAdapter;
    }

    @Bean
    MethodInvokingTaskletAdapter methodInvokingTaskletAdapterForCountingBackupRecords() {
        MethodInvokingTaskletAdapter methodInvokingTaskletAdapter = new MethodInvokingTaskletAdapter();
        methodInvokingTaskletAdapter.setTargetObject(backupDatabaseService());
        methodInvokingTaskletAdapter.setTargetMethod("getNumberOfSongs");
        return methodInvokingTaskletAdapter;
    }

    @Bean
    public CallableTaskletAdapter tasklet() {
        CallableTaskletAdapter callableTaskletAdapter = new CallableTaskletAdapter();
        callableTaskletAdapter.setCallable(callableObject());
        return callableTaskletAdapter;
    }

    @Bean
    public Callable<RepeatStatus> callableObject() {
        return () -> {
            List<Track> tracks = mainDatabaseService().processTracks();
            tracks.forEach(t -> backupDatabaseService().validateSong(t.getCode()));
            System.out.println("This was executed in another thread");
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public MainDatabaseService mainDatabaseService() {
        return new MainDatabaseService(trackRepository);
    }

    @Bean
    public BackupDatabaseService backupDatabaseService() {
        return new BackupDatabaseService(songRepository);
    }


    @Bean
    public Step databaseSynchronizationStep() {
        return stepBuilderFactory
                .get("databaseSynchronizationStep")
                .<Track, Track>chunk(10)
                .reader(trackItemReader())
                .processor(trackProcessor())
                .writer(trackItemWriter())
                .build();
    }

    @Bean
    public JdbcCursorItemReader<Track> trackItemReader(){
        JdbcCursorItemReader<Track> reader = new JdbcCursorItemReader<>();
        reader.setSql("SELECT id, title, artist, version, url, code FROM tracks");
        reader.setDataSource(mainDatabaseConfiguration.mainDataSource());
        reader.setFetchSize(100);
        reader.setRowMapper(new TrackRowMapper());
        return reader;
    }

    @Bean
    ItemProcessor<Track, Track> trackProcessor() {
        return new DatabaseProcessor(backupDatabaseService(), mainDatabaseService());
    }

    @Bean
    public ItemWriter<Track> trackItemWriter(){
        JdbcBatchItemWriter<Track> itemWriter = new JdbcBatchItemWriter<>();
        itemWriter.setDataSource(backupDatabaseConfiguration.backupDataSource());
        itemWriter.setSql(
                "INSERT INTO songs (id, title, artist, version, url, code) " +
                        "VALUES (nextval('song_id_seq'), :title, :artist, :version, :url, :code)");
        itemWriter.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        itemWriter.afterPropertiesSet();
        return itemWriter;
    }



}



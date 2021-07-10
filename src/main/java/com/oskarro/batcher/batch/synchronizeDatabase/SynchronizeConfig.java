package com.oskarro.batcher.batch.synchronizeDatabase;

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
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.step.tasklet.CallableTaskletAdapter;
import org.springframework.batch.core.step.tasklet.MethodInvokingTaskletAdapter;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;

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
    public MainDatabaseService mainDatabaseService() {
        return new MainDatabaseService(trackRepository);
    }

    @Bean
    public BackupDatabaseService backupDatabaseService() {
        return new BackupDatabaseService(songRepository);
    }

    @Bean
    public Job synchronizeDatabaseJob() {
        return this.jobBuilderFactory
                .get("synchronizeDatabaseJob")
                .start(printStartNotificationStep())
                .next(methodCheckingMainDatabaseStep())
                .on("FAILED").to(failureCheckingDatabaseStep())
                .from(methodCheckingMainDatabaseStep()).on("*").to(methodCheckingBackupDatabaseStep())
//                .next(methodCheckingBackupDatabaseStep())
                .next(databaseSynchronizationStep())
                .next(resultProcessingFlow())
                .end()
                .build();
    }

    // region START PROCESS
    @Bean
    public Step printStartNotificationStep() {
        return this.stepBuilderFactory
                .get("printStep")
                .tasklet(tasklet())
                .transactionManager(mainDatabaseConfiguration.mainTransactionManager())
                .build();
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
            System.out.println("STARTING BATCH PROCESS FOR DATABASE SYNCHRONIZATION");
            return RepeatStatus.FINISHED;
        };
    }
    // endregion

    // region MAIN DATABASE (POSTGRES)
    @Bean
    public Step methodCheckingMainDatabaseStep() {
        return this.stepBuilderFactory
                .get("methodCheckingMainDatabaseStep")
                .tasklet(methodInvokingTaskletAdapterForCountingMainRecords())
                .transactionManager(mainDatabaseConfiguration.mainTransactionManager())
                .build();
    }

    @Bean
    MethodInvokingTaskletAdapter methodInvokingTaskletAdapterForCountingMainRecords() {
        MethodInvokingTaskletAdapter methodInvokingTaskletAdapter = new MethodInvokingTaskletAdapter();
        methodInvokingTaskletAdapter.setTargetObject(mainDatabaseService());
        methodInvokingTaskletAdapter.setTargetMethod("getNumberOfRecords");
        return methodInvokingTaskletAdapter;
    }
    // endregion


    // region BACKUP DATABASE (POSTGRES)
    @Bean
    public Step methodCheckingBackupDatabaseStep() {
        return this.stepBuilderFactory
                .get("methodCheckingBackupDatabaseStep")
                .tasklet(methodInvokingTaskletAdapterForCountingBackupRecords())
                .transactionManager(backupDatabaseConfiguration.backupTransactionManager())
                .build();
    }

    @Bean
    MethodInvokingTaskletAdapter methodInvokingTaskletAdapterForCountingBackupRecords() {
        MethodInvokingTaskletAdapter methodInvokingTaskletAdapter = new MethodInvokingTaskletAdapter();
        methodInvokingTaskletAdapter.setTargetObject(backupDatabaseService());
        methodInvokingTaskletAdapter.setTargetMethod("getNumberOfSongsInBackup");
        return methodInvokingTaskletAdapter;
    }
    // endregion

    // region COMMONS
    @Bean
    public TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor("async");
    }

    @Bean
    public Step failureCheckingDatabaseStep() {
        return this.stepBuilderFactory
                .get("failureCheckingDatabaseStep")
                .tasklet(failCheckingDatabaseTasklet())
                .build();
    }

    @Bean
    public Tasklet failCheckingDatabaseTasklet() {
        return (contribution, context) -> {
            System.out.println("Failure while checking database!");
            return RepeatStatus.FINISHED;
        };
    }
    // endregion

    // region DATABASE SYNCHRONIZATION
    @Bean
    public Step databaseSynchronizationStep() {
        return stepBuilderFactory
                .get("databaseSynchronizationStep")
                .<Track, Track>chunk(20)
                .reader(trackItemReader())
                .processor(trackProcessor())
                .writer(trackItemWriter())
                .taskExecutor(taskExecutor())
                .throttleLimit(20)
                .build();
    }

    @Bean
    public JdbcCursorItemReader<Track> trackItemReader(){
        return new JdbcCursorItemReaderBuilder<Track>()
                .dataSource(mainDatabaseConfiguration.mainDataSource())
                .rowMapper(new TrackRowMapper())
                .name("track-reader")
                .fetchSize(200)
//                .saveState(false)
                .verifyCursorPosition(false)
                .sql("SELECT id, title, artist, version, url, code FROM tracks")
                .build();
    }

    @Bean
    ItemProcessor<Track, Track> trackProcessor() {
        return new DatabaseProcessor(backupDatabaseService(), mainDatabaseService());
    }

    @Bean
    public ItemWriter<Track> trackItemWriter(){
        return new JdbcBatchItemWriterBuilder<Track>()
                .dataSource(backupDatabaseConfiguration.backupDataSource())
                .sql("INSERT INTO songs (id, title, artist, version, url, code) " +
                        "VALUES (nextval('song_id_seq'), :title, :artist, :version, :url, :code)")
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .build();
    }
    // endregion

    // region PROCESS RESULTS
    @Bean
    public Step methodCheckingFinalResultsStep() {
        return this.stepBuilderFactory
                .get("methodCheckingFinalResultsStep")
                .tasklet(methodInvokingTaskletAdapterForCountingMainRecords())
                .transactionManager(mainDatabaseConfiguration.mainTransactionManager())
                .build();
    }

    @Bean
    public Flow resultProcessingFlow() {
        return new FlowBuilder<Flow>("resultProcessingFlow")
                .start(methodCheckingMainDatabaseStep())
                .next(methodCheckingBackupDatabaseStep())
                .build();
    }
    // endregion

}



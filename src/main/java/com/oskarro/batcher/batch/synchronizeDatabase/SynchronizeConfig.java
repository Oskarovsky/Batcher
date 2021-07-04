package com.oskarro.batcher.batch.synchronizeDatabase;

import com.oskarro.batcher.batch.synchronizeDatabase.service.BackupDatabaseService;
import com.oskarro.batcher.batch.synchronizeDatabase.service.MainDatabaseService;
import com.oskarro.batcher.repository.backup.SongRepository;
import com.oskarro.batcher.repository.main.TrackRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.tasklet.CallableTaskletAdapter;
import org.springframework.batch.core.step.tasklet.MethodInvokingTaskletAdapter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.concurrent.Callable;

@EnableAutoConfiguration
@EnableBatchProcessing
@Configuration
@Import({BackupConfiguration.class, MainConfiguration.class})
public class SynchronizeConfig {

    public final JobBuilderFactory jobBuilderFactory;
    public final StepBuilderFactory stepBuilderFactory;

    public final JdbcTemplate jdbcTemplate;

    public final BackupConfiguration backupConfiguration;
    public final MainConfiguration mainConfiguration;

    public final TrackRepository trackRepository;
    public final SongRepository songRepository;



    public SynchronizeConfig(JobBuilderFactory jobBuilderFactory,
                             StepBuilderFactory stepBuilderFactory,
                             JdbcTemplate jdbcTemplate,
                             BackupConfiguration backupConfiguration,
                             MainConfiguration mainConfiguration,
                             TrackRepository trackRepository,
                             SongRepository songRepository) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.jdbcTemplate = jdbcTemplate;
        this.backupConfiguration = backupConfiguration;
        this.mainConfiguration = mainConfiguration;
        this.trackRepository = trackRepository;
        this.songRepository = songRepository;
    }

    @Bean
    public Job synchronizeDatabaseJob() {
        return this.jobBuilderFactory
                .get("synchronizeDatabaseJob")
                .start(printStep())
                .next(methodInvokingMainStep())
                .next(methodInvokingBackupStep())
                .build();
    }

    @Bean
    public Step printStep() {
        return this.stepBuilderFactory
                .get("printStep")
                .tasklet(tasklet())
                .build();
    }

    @Bean
    public Step methodInvokingMainStep() {
        return this.stepBuilderFactory
                .get("methodInvokingMainStep")
                .tasklet(methodInvokingTaskletAdapterForCountingMainRecords())
                .transactionManager(mainConfiguration.mainTransactionManager())
                .build();
    }

    @Bean
    public Step methodInvokingBackupStep() {
        return this.stepBuilderFactory
                .get("methodInvokingBackupStep")
                .tasklet(methodInvokingTaskletAdapterForCountingBackupRecords())
                .transactionManager(backupConfiguration.backupTransactionManager())
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


}



package com.oskarro.batcher.batch.synchronizeDatabase;

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
import org.springframework.jdbc.core.JdbcTemplate;

import com.oskarro.batcher.batch.synchronizeDatabase.service.DatabaseService;

import javax.sql.DataSource;
import java.util.concurrent.Callable;

@EnableAutoConfiguration
@EnableBatchProcessing
@Configuration
public class SynchronizeConfig {

    public final JobBuilderFactory jobBuilderFactory;
    public final StepBuilderFactory stepBuilderFactory;
    public final DataSource dataSource;
    public final JdbcTemplate jdbcTemplate;

    public final TrackRepository trackRepository;

    public SynchronizeConfig(JobBuilderFactory jobBuilderFactory,
                             StepBuilderFactory stepBuilderFactory,
                             DataSource dataSource,
                             JdbcTemplate jdbcTemplate,
                             TrackRepository trackRepository) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
        this.trackRepository = trackRepository;
    }

    @Bean
    public Job synchronizeDatabaseJob() {
        return this.jobBuilderFactory
                .get("synchronizeDatabaseJob")
                .start(printStep())
                .next(methodInvokingStep())
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
    public Step methodInvokingStep() {
        return this.stepBuilderFactory
                .get("methodInvokingStep")
                .tasklet(methodInvokingTaskletAdapter())
                .build();
    }

    @Bean
    MethodInvokingTaskletAdapter methodInvokingTaskletAdapter() {
        MethodInvokingTaskletAdapter methodInvokingTaskletAdapter = new MethodInvokingTaskletAdapter();
        methodInvokingTaskletAdapter.setTargetObject(databaseService());
        methodInvokingTaskletAdapter.setTargetMethod("getNumberOfRecords");
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
    public DatabaseService databaseService() {
        return new DatabaseService(trackRepository);
    }

}



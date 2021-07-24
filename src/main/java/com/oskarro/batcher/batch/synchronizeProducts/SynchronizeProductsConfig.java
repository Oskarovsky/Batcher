package com.oskarro.batcher.batch.synchronizeProducts;

import com.oskarro.batcher.batch.synchronizeDatabase.config.BackupDatabaseConfiguration;
import com.oskarro.batcher.batch.synchronizeDatabase.config.MainDatabaseConfiguration;
import com.oskarro.batcher.batch.synchronizeDatabase.service.BackupDatabaseService;
import com.oskarro.batcher.batch.synchronizeDatabase.service.MainDatabaseService;
import com.oskarro.batcher.environment.backup.repo.ProductRepository;
import com.oskarro.batcher.environment.main.repo.ComputerRepository;
import com.oskarro.batcher.environment.main.repo.ConsoleRepository;
import com.oskarro.batcher.environment.main.repo.SmartphoneRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@EnableAutoConfiguration
@EnableBatchProcessing
@Configuration
public class SynchronizeProductsConfig {

    public final JobBuilderFactory jobBuilderFactory;
    public final StepBuilderFactory stepBuilderFactory;

    public final JdbcTemplate jdbcTemplate;

    public final BackupDatabaseConfiguration backupDatabaseConfiguration;
    public final MainDatabaseConfiguration mainDatabaseConfiguration;

    public final ProductRepository productRepository;
    public final ComputerRepository computerRepository;
    public final SmartphoneRepository smartphoneRepository;
    public final ConsoleRepository consoleRepository;

    public SynchronizeProductsConfig(JobBuilderFactory jobBuilderFactory,
                                     StepBuilderFactory stepBuilderFactory,
                                     JdbcTemplate jdbcTemplate,
                                     BackupDatabaseConfiguration backupDatabaseConfiguration,
                                     MainDatabaseConfiguration mainDatabaseConfiguration,
                                     ProductRepository productRepository,
                                     ComputerRepository computerRepository,
                                     SmartphoneRepository smartphoneRepository,
                                     ConsoleRepository consoleRepository) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.jdbcTemplate = jdbcTemplate;
        this.backupDatabaseConfiguration = backupDatabaseConfiguration;
        this.mainDatabaseConfiguration = mainDatabaseConfiguration;
        this.productRepository = productRepository;
        this.computerRepository = computerRepository;
        this.smartphoneRepository = smartphoneRepository;
        this.consoleRepository = consoleRepository;
    }

    @Bean
    public MainDatabaseService mainProductDatabaseService() {
        return new MainDatabaseService(computerRepository, consoleRepository, smartphoneRepository);
    }

    @Bean
    public BackupDatabaseService backupProductDatabaseService() {
        return new BackupDatabaseService(productRepository);
    }

    @Bean
    public Job synchronizeProductsJob() {
        return this.jobBuilderFactory
                .get("synchronizeProductsJob")
                .incrementer(new RunIdIncrementer())
                .start(printInformationBeforeStep())
                .build();
    }

    @Bean
    public Step printInformationBeforeStep() {
        return stepBuilderFactory
                .get("printInformationBeforeStep")
                .tasklet(((stepContribution, chunkContext) -> {
                    System.out.println("SYNCHRONIZE PRODUCTS IN DATABASES ALREADY RAN!");
                    return RepeatStatus.FINISHED;
                }))
                .build();
    }
}

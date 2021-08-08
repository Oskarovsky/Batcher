package com.oskarro.batcher.batch.synchronizeProducts;

import com.oskarro.batcher.batch.csvToDatabase.JobCompletionNotificationListener;
import com.oskarro.batcher.batch.synchronizeDatabase.config.BackupDatabaseConfiguration;
import com.oskarro.batcher.batch.synchronizeDatabase.config.MainDatabaseConfiguration;
import com.oskarro.batcher.batch.synchronizeDatabase.service.BackupDatabaseService;
import com.oskarro.batcher.batch.synchronizeDatabase.service.MainDatabaseService;
import com.oskarro.batcher.environment.backup.repo.ProductRepository;
import com.oskarro.batcher.environment.main.dao.ComputerDao;
import com.oskarro.batcher.environment.main.dao.ComputerDaoSupport;
import com.oskarro.batcher.environment.main.model.cargo.Computer;
import com.oskarro.batcher.environment.main.model.cargo.Department;
import com.oskarro.batcher.environment.main.repo.ComputerRepository;
import com.oskarro.batcher.environment.main.repo.ConsoleRepository;
import com.oskarro.batcher.environment.main.repo.SmartphoneRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.JobListenerFactoryBean;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.PassThroughFieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Date;

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

    // region SYNCHRONIZATION PROCESS
    @Bean
    public Job synchronizeProductsJob() {
        return this.jobBuilderFactory
                .get("synchronizeProductsJob")
                .listener(JobListenerFactoryBean.getListener(
                        new JobCompletionNotificationListener(
                                jdbcTemplate, "Job synchronization products in database")
                ))
                .incrementer(new RunIdIncrementer())
                .start(printInformationBeforeStep())
                .build();
    }

    @Bean
    public Step printInformationBeforeStep() {
        return stepBuilderFactory
                .get("printInformationBeforeStep")
                .tasklet(((stepContribution, chunkContext) -> {
                    System.out.println("JOB SYNCHRONIZATION PRODUCTS IN DATABASES ALREADY RAN!");
                    return RepeatStatus.FINISHED;
                }))
                .build();
    }
    // endregion

    // region COMPUTER ITEM
    @Bean
    public Job computerUpdateJob() {
        return this.jobBuilderFactory
                .get("computerUpdateJob")
                .incrementer(new RunIdIncrementer())
                .start(computerUpdateStep())
//                .on("STOPPED").stopAndRestart(computerUpdateStep())
//                .from(computerUpdateStep()).on("*").to(null)
                .build();
    }

    @Bean
    public Step computerUpdateStep() {
        return this.stepBuilderFactory
                .get("computerUpdateStep")
                .<Computer, Computer>chunk(100)
                .reader(computerCsvReader())
                .writer(computerUpdateWriter(mainDatabaseConfiguration.mainDataSource()))
                .allowStartIfComplete(true)
                .listener(computerCsvReader())
                .build();
    }

    @Bean
    @StepScope
    public ComputerCsvReader computerCsvReader() {
        return new ComputerCsvReader(computerUpdateReader(null));
    }

    @Bean
    @StepScope
    public FlatFileItemReader<FieldSet> computerUpdateReader(@Value("#{jobParameters['fileContent']}") String fileContent) {
        return new FlatFileItemReaderBuilder<FieldSet>()
                .name("computerUpdateReader")
                .resource(new ByteArrayResource(fileContent.getBytes()))
                .lineTokenizer(new DelimitedLineTokenizer())
                .fieldSetMapper(new PassThroughFieldSetMapper())
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<Computer> computerUpdateWriter(DataSource dataSource) {
        try {
            File resource = null;
            resource = new ClassPathResource("products/upsert_computer.sql").getFile();
            String sql = new String(Files.readAllBytes(resource.toPath()));
            return new JdbcBatchItemWriterBuilder<Computer>()
                    .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                    .sql(sql)
                    .dataSource(dataSource)
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error caused by computerUpdateWriter (problem with reading files)");
            return null;
        }
    }

    @Bean
    public Step applyComputerUpdateStep() {
        return this.stepBuilderFactory
                .get("applyComputerUpdateStep")
                .<Department, Department>chunk(100)
                .reader(departmentReader(null))
//                .processor(computerApplierProcessor())
                .writer(departmentWriter(mainDatabaseConfiguration.mainDataSource()))
                .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<Department> departmentReader(DataSource dataSource) {
        try {
            File resource = null;
            resource = new ClassPathResource("products/select_department.sql").getFile();
            String sql = new String(Files.readAllBytes(resource.toPath()));
            return new JdbcCursorItemReaderBuilder<Department>()
                    .name("departmentReader")
                    .dataSource(dataSource)
                    .sql(sql)
                    .rowMapper((resultSet, rowNumber) -> {
                        Department department = new Department();
                        department.setDepartmentId(resultSet.getInt("department_id"));
                        department.setCurrentBalance(resultSet.getBigDecimal("current_balance"));
                        return department;
                    }).build();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Error caused by departmentReader (problem with reading files)");
            return null;
        }
    }

    @Bean
    public JdbcBatchItemWriter<Department> departmentWriter(DataSource dataSource) {
        File resource = null;
        try {
            resource = new ClassPathResource("products/upsert_department.sql").getFile();
            String sql = new String(Files.readAllBytes(resource.toPath()));
            return new JdbcBatchItemWriterBuilder<Department>()
                    .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                    .sql(sql)
                    .dataSource(dataSource)
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error caused by departmentWriter (problem with reading files)");
            return null;
        }
    }

    @Bean
    public ComputerDao computerDao(DataSource dataSource) {
        return new ComputerDaoSupport(dataSource);
    }

    @Bean
    public ComputerApplierProcessor computerApplierProcessor() {
        return new ComputerApplierProcessor(computerDao(null));
    }


}

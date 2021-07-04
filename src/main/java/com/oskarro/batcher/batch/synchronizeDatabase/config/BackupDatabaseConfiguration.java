package com.oskarro.batcher.batch.synchronizeDatabase.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Objects;


@Configuration
@PropertySource({"classpath:backup-database.properties"})
@EnableJpaRepositories(
        basePackages = {"com.oskarro.batcher.model.backup", "com.oskarro.batcher.repository.backup"},
        entityManagerFactoryRef = "backupEntityManager",
        transactionManagerRef = "backupTransactionManager")
public class BackupDatabaseConfiguration {

    private final Environment env;

    public BackupDatabaseConfiguration(Environment env) {
        this.env = env;
    }

    @Bean
    public DataSource backupDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(Objects.requireNonNull(env.getProperty("backup-datasource.driver-class-name")));
        dataSource.setUrl(Objects.requireNonNull(env.getProperty("backup-datasource.url")));
        dataSource.setUsername(Objects.requireNonNull(env.getProperty("backup-datasource.username")));
        dataSource.setPassword(Objects.requireNonNull(env.getProperty("backup-datasource.password")));
        return dataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean backupEntityManager() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(backupDataSource());
        em.setPackagesToScan("com.oskarro.batcher.model.backup");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
        properties.put("hibernate.dialect", env.getProperty("hibernate.dialect"));
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Bean
    public PlatformTransactionManager backupTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(backupEntityManager().getObject());
        return transactionManager;
    }
}

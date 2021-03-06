package com.oskarro.batcher.batch.synchronizeDatabase.config;

import org.springframework.beans.factory.annotation.Autowired;
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
@PropertySource({"classpath:main-database.properties"})
@EnableJpaRepositories(
        basePackages = {
                "com.oskarro.batcher.environment.main.model",
                "com.oskarro.batcher.environment.main.repo"
        },
        entityManagerFactoryRef = "mainEntityManager",
        transactionManagerRef = "mainTransactionManager")
public class MainDatabaseConfiguration {

    private final Environment env;

    public MainDatabaseConfiguration(Environment env) {
        this.env = env;
    }

    @Bean
    @Primary
    public DataSource mainDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(Objects.requireNonNull(env.getProperty("datasource.driver-class-name")));
        dataSource.setUrl(Objects.requireNonNull(env.getProperty("datasource.url")));
        dataSource.setUsername(Objects.requireNonNull(env.getProperty("datasource.username")));
        dataSource.setPassword(Objects.requireNonNull(env.getProperty("datasource.password")));
        return dataSource;
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean mainEntityManager() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(mainDataSource());
        em.setPackagesToScan("com.oskarro.batcher.environment.main.model");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
        properties.put("hibernate.dialect", env.getProperty("hibernate.dialect"));
        em.setJpaPropertyMap(properties);
        return em;
    }

    @Bean
    @Primary
    public PlatformTransactionManager mainTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(mainEntityManager().getObject());
        return transactionManager;
    }
}

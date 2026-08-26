package com.mango.fukuoka.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.mango.fukuoka",
        entityManagerFactoryRef = "fukuokaEntityManagerFactory",
        transactionManagerRef = "fukuokaTransactionManager"
)
public class FukuokaJpaConfig {

    @Bean
    @ConfigurationProperties("fukuoka.datasource")
    public DataSourceProperties fukuokaDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource fukuokaDataSource(
            @Qualifier("fukuokaDataSourceProperties")
            DataSourceProperties properties
    ) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean fukuokaEntityManagerFactory(
            @Qualifier("fukuokaDataSource")
            DataSource dataSource
    ) {
        LocalContainerEntityManagerFactoryBean factory =
                new LocalContainerEntityManagerFactoryBean();

        factory.setDataSource(dataSource);

        factory.setPackagesToScan(
                "com.mango.fukuoka"
        );

        factory.setJpaVendorAdapter(
                new HibernateJpaVendorAdapter()
        );

        Map<String, Object> properties = new HashMap<>();

        properties.put(
                "hibernate.hbm2ddl.auto",
                "validate"
        );

        properties.put(
                "hibernate.dialect",
                "org.hibernate.dialect.MariaDBDialect"
        );

        properties.put(
                "hibernate.show_sql",
                true
        );

        properties.put(
                "hibernate.format_sql",
                true
        );

        factory.setJpaPropertyMap(properties);

        return factory;
    }

    @Bean
    public PlatformTransactionManager fukuokaTransactionManager(
            @Qualifier("fukuokaEntityManagerFactory")
            LocalContainerEntityManagerFactoryBean entityManagerFactory
    ) {
        return new JpaTransactionManager(
                entityManagerFactory.getObject()
        );
    }
}
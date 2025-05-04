package com.example.demo;

import javax.sql.DataSource;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import jakarta.persistence.EntityManagerFactory;

@Configuration
public class DataSourceConfiguration {

	@Bean
	@Primary
	@ConfigurationProperties(prefix = "spring.datasource")
	public DataSource dataSource() {
		return org.springframework.boot.jdbc.DataSourceBuilder.create().build();
	}

	@Bean
	@Primary
	public DataSourceTransactionManager transactionManager() {
		return new DataSourceTransactionManager(dataSource());
	}

	@Bean
	@Qualifier(value = "dynamicDataSource")
	@StepScope
	public DataSource dynamicDataSource() {
		return dynamicRoutingDataSource().determineTargetDataSource();
	}

	@Bean
	@Qualifier(value = "dynamicTransactionManager")
	public DataSourceTransactionManager dynamicTransactionManager() {
		return new DataSourceTransactionManager(dynamicDataSource());
	}

	@Bean
	@Qualifier(value = "dynamicRoutingDataSource")
	public DynamicRoutingDataSource dynamicRoutingDataSource() {
		return new DynamicRoutingDataSource();
	}
	
	@Bean
	@Qualifier(value = "entityManagerFactory")
	@StepScope
	public EntityManagerFactory entityManagerFactory() {
		LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();

		localContainerEntityManagerFactoryBean.setDataSource(dynamicDataSource());
		localContainerEntityManagerFactoryBean.setPackagesToScan("com.example.demo");
		localContainerEntityManagerFactoryBean.setPersistenceUnitName("businessEntityManagerFactory");
		localContainerEntityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
		localContainerEntityManagerFactoryBean.afterPropertiesSet();

		return localContainerEntityManagerFactoryBean.getObject();
	}

	@Bean
	@Qualifier(value = "businessJpaTransactionManager")
	public JpaTransactionManager businessJpaTransactionManager() {
		return new JpaTransactionManager(entityManagerFactory());
	}

}

package com.example.demo;

import javax.sql.DataSource;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class DataSourceConfiguration {

	@Bean
	@Primary
	@ConfigurationProperties(prefix = "spring.datasource")
	public DataSource dataSource() {
		return org.springframework.boot.jdbc.DataSourceBuilder.create().build();
	}

	@Bean
	@Qualifier("transactionManager")
	@Primary
	public PlatformTransactionManager transactionManager() {
		return new ResourcelessTransactionManager();
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

}

package com.example.demo;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

@Configuration
public class BusinessDataSourceConfiguration {

	@Bean
	@ConfigurationProperties(prefix = "spring.business.datasource")
	public DataSource businessDataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean
	public DataSourceTransactionManager businessTransactionManager() {
		return new DataSourceTransactionManager(businessDataSource());
	}

}

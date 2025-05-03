package com.example.demo;

import org.springframework.batch.core.repository.support.ResourcelessJobRepository;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JobRepositoryConfiguration {

	@Bean
	@Primary
	public ResourcelessTransactionManager platformTransactionManager() {
		return new ResourcelessTransactionManager();
	}

	@Bean(name = "jobRepository")
	@Primary
	public ResourcelessJobRepository jobRepository() {
		return new ResourcelessJobRepository();
	}

}

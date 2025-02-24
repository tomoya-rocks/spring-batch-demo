package com.example.demo;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.ResourcelessJobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfiguration {

	@Bean
	public ResourcelessTransactionManager transactionManager() {
		return new ResourcelessTransactionManager();
	}

	@Bean(name = "jobRepository")
	@Primary
	public JobRepository jobRepository() {
		return new ResourcelessJobRepository();
	}

	@Bean
	public Job importMemberJob(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
		return new JobBuilder("importMemberJob" + System.currentTimeMillis(), jobRepository)
				.start(memberCsvTaskletStep(jobRepository, platformTransactionManager)).build();
	}

	@Bean
	public Step memberCsvTaskletStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new StepBuilder("memberCsvTasklet", jobRepository).tasklet(memberCsvTasklet(), transactionManager)
				.build();
	}

	@Bean
	public Tasklet memberCsvTasklet() {
		return new MemberCsvTasklet(exectionContext());
	}

	@Bean
	public ExecutionContext exectionContext() {
		return new ExecutionContext();
	}

}

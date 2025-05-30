package com.example.demo;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@Import(value = { DataSourceConfiguration.class, StrategyConfiguration.class })
public class BatchConfiguration {

	@Bean
	public Job importMemberJob(JobRepository jobRepository, HandleCsvStrategyFactory handleCsvStrategyFactory,
			@Qualifier(value = "transactionManager") PlatformTransactionManager transactionManager,
			@Qualifier(value = "dynamicTransactionManager") DataSourceTransactionManager dynamicTransactionManager,
			@Qualifier(value = "dynamicRoutingDataSource") DynamicRoutingDataSource dynamicRoutingDataSource) {
		return new JobBuilder("importMemberJob" + System.currentTimeMillis(), jobRepository)
				.start(memberCsvTaskletStep(jobRepository, transactionManager)).next(masterStep(jobRepository,
						handleCsvStrategyFactory, dynamicTransactionManager, dynamicRoutingDataSource))
				.build();
	}

	@Bean
	public Step memberCsvTaskletStep(JobRepository jobRepository,
			@Qualifier(value = "transactionManager") PlatformTransactionManager transactionManager) {
		return new StepBuilder("memberCsvTasklet", jobRepository).tasklet(memberCsvTasklet(), transactionManager)
				.build();
	}

	@Bean
	public Step masterStep(JobRepository jobRepository, HandleCsvStrategyFactory handleCsvStrategyFactory,
			@Qualifier(value = "dynamicTransactionManager") DataSourceTransactionManager dynamicTransactionManager,
			@Qualifier(value = "dynamicRoutingDataSource") DynamicRoutingDataSource dynamicRoutingDataSource) {
		return new StepBuilder("master", jobRepository)
				.partitioner("slaveStep", memberCsvPartitioner(dynamicRoutingDataSource)).gridSize(10)
				.step(slaveStep(jobRepository, handleCsvStrategyFactory, dynamicTransactionManager))
				.taskExecutor(taskExecutor()).build();
	}

	@SuppressWarnings({ "unchecked" })
	@Bean
	public Step slaveStep(JobRepository jobRepository, HandleCsvStrategyFactory handleCsvStrategyFactory,
			@Qualifier(value = "dynamicTransactionManager") DataSourceTransactionManager dynamicTransactionManager) {
		HandleCsvStrategy handleCsvStrategy = handleCsvStrategyFactory
				.createHandleCsvStrategy("handleMemberCsvStrategy");
		return new StepBuilder("slave", jobRepository).<Member, FullNameMember>chunk(5, dynamicTransactionManager)
				.listener(slaveStepExecutionListener())
				.reader((ItemReader<? extends Member>) handleCsvStrategy.itemReader())
				.processor((ItemProcessor<? super Member, ? extends FullNameMember>) handleCsvStrategy.itemProcessor())
				.writer((ItemWriter<? super FullNameMember>) handleCsvStrategy.itemWriter()).build();
	}

	@Bean
	public Tasklet memberCsvTasklet() {
		return new MemberCsvTasklet(exectionContext());
	}

	@Bean
	public ExecutionContext exectionContext() {
		return new ExecutionContext();
	}

	@Bean
	public Partitioner memberCsvPartitioner(
			@Qualifier(value = "dynamicRoutingDataSource") DynamicRoutingDataSource dynamicRoutingDataSource) {
		return new MemberCsvPartitioner(exectionContext(), dynamicRoutingDataSource);
	}

	@Bean
	public TaskExecutor taskExecutor() {
		return new VirtualThreadTaskExecutor();
	}

	@Bean
	public SlaveStepExecutionListener slaveStepExecutionListener() {
		return new SlaveStepExecutionListener();
	}

}

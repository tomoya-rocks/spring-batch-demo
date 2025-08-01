package com.example.demo;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfiguration {

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
	public Job importMemberJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new JobBuilder("importMemberJob" + System.currentTimeMillis(), jobRepository)
				.start(memberCsvTaskletStep(jobRepository, transactionManager))
				.next(masterStep(jobRepository, transactionManager)).build();
	}

	@Bean
	public Step memberCsvTaskletStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new StepBuilder("memberCsvTasklet", jobRepository).tasklet(memberCsvTasklet(), transactionManager)
				.build();
	}

	@Bean
	public Step masterStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new StepBuilder("master", jobRepository).partitioner("slaveStep", memberCsvPartitioner()).gridSize(10)
				.step(slaveStep(jobRepository, transactionManager)).taskExecutor(taskExecutor()).build();
	}

	@Bean
	public Step slaveStep(JobRepository jobRepository, PlatformTransactionManager dynamicTransactionManager) {
		return new StepBuilder("slave", jobRepository).<Member, FullNameMember>chunk(5000, dynamicTransactionManager())
				.listener(slaveStepExecutionListener()).reader(itemReader(null)).processor(itemProcessor())
				.writer(itemWriter()).faultTolerant().retryLimit(9).retry(CannotCreateTransactionException.class)
				.backOffPolicy(new ExponentialBackOffPolicy()).build();
	}

	@Bean
	@StepScope
	public FlatFileItemReader<Member> itemReader(@Value("#{stepExecutionContext['csvFile']}") String csvFile) {
		return new FlatFileItemReaderBuilder<Member>().name("memberItemReader").resource(new ClassPathResource(csvFile))
				.delimited().names("id", "firstName", "lastName").linesToSkip(1).targetType(Member.class).build();
	}

	@Bean
	@StepScope
	public ItemProcessor<Member, FullNameMember> itemProcessor() {
		return new MemberItemProcessor();
	}

	@Bean
	@StepScope
	public ItemWriter<FullNameMember> itemWriter() {
		return new MemberItemWriter(dynamicDataSource());
	}

	@Bean
	@StepScope
	public DataSource dynamicDataSource() {
		DatabaseConfig databaseConfig = SlaveStepExecutionListener.databaseConfig.get();

		return DataSourceBuilder.create()
				.url(String.format("jdbc:postgresql://%s/%s", databaseConfig.host(), databaseConfig.dbName()))
				.username(databaseConfig.username()).password(databaseConfig.password())
				.driverClassName("org.postgresql.Driver").build();
	}

	@Bean
	public DataSourceTransactionManager dynamicTransactionManager() {
		return new DataSourceTransactionManager(dynamicDataSource());
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
	public Partitioner memberCsvPartitioner() {
		return new MemberCsvPartitioner(exectionContext());
	}

	@Bean
	public TaskExecutor taskExecutor() {
		return new SyncTaskExecutor();
	}

	@Bean
	public SlaveStepExecutionListener slaveStepExecutionListener() {
		return new SlaveStepExecutionListener();
	}

}

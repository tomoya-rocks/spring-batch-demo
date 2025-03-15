package com.example.demo;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.ResourcelessJobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
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
				.start(memberCsvTaskletStep(jobRepository, platformTransactionManager)).next(masterStep(jobRepository))
				.build();
	}

	@Bean
	public Step memberCsvTaskletStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new StepBuilder("memberCsvTasklet", jobRepository).tasklet(memberCsvTasklet(), transactionManager)
				.build();
	}

	@Bean
	public Step masterStep(JobRepository jobRepository) {
		return new StepBuilder("master", jobRepository).partitioner("slaveStep", memberCsvPartitioner()).gridSize(10)
				.step(slaveStep(jobRepository)).taskExecutor(taskExecutor()).build();
	}

	@Bean
	public Step slaveStep(JobRepository jobRepository) {
		return new StepBuilder("slave", jobRepository).<Member, FullNameMember>chunk(5000, transactionManager())
				.reader(itemReader(null)).processor(itemProcessor()).writer(itemWriter(null)).build();
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
	public ItemWriter<FullNameMember> itemWriter(
			@Value("#{stepExecutionContext['databaseConfig']}") DatabaseConfig databaseConfig) {
		String url = String.format("jdbc:postgresql://%s/%s", databaseConfig.host(), databaseConfig.dbName());
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setUrl(url);
		dataSource.setUsername(databaseConfig.username());
		dataSource.setPassword(databaseConfig.password());

		return new MemberItemWriter(dataSource);
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
		return new SimpleAsyncTaskExecutor();
	}

}

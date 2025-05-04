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
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@Import(value = { DataSourceConfiguration.class })
public class BatchConfiguration {

	@Bean
	public Job importMemberJob(JobRepository jobRepository, PlatformTransactionManager transactionManager,
			@Qualifier(value = "entityManagerFactory") EntityManagerFactory emf,
			@Qualifier(value = "businessJpaTransactionManager") JpaTransactionManager jpaTransactionManager,
			@Qualifier(value = "dynamicRoutingDataSource") DynamicRoutingDataSource dynamicRoutingDataSource) {
		return new JobBuilder("importMemberJob" + System.currentTimeMillis(), jobRepository)
				.start(memberCsvTaskletStep(jobRepository, transactionManager))
				.next(masterStep(jobRepository, emf, jpaTransactionManager, dynamicRoutingDataSource)).build();
	}

	@Bean
	public Step memberCsvTaskletStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new StepBuilder("memberCsvTasklet", jobRepository).tasklet(memberCsvTasklet(), transactionManager)
				.build();
	}

	@Bean
	public Step masterStep(JobRepository jobRepository,
			@Qualifier(value = "entityManagerFactory") EntityManagerFactory emf,
			@Qualifier(value = "businessJpaTransactionManager") JpaTransactionManager jpaTransactionManager,
			@Qualifier(value = "dynamicRoutingDataSource") DynamicRoutingDataSource dynamicRoutingDataSource) {
		return new StepBuilder("master", jobRepository)
				.partitioner("slaveStep", memberCsvPartitioner(dynamicRoutingDataSource)).gridSize(10)
				.step(slaveStep(jobRepository, emf, jpaTransactionManager)).taskExecutor(taskExecutor()).build();
	}

	@Bean
	public Step slaveStep(JobRepository jobRepository,
			@Qualifier(value = "entityManagerFactory") EntityManagerFactory emf,
			@Qualifier(value = "businessJpaTransactionManager") JpaTransactionManager jpaTransactionManager) {
		return new StepBuilder("slave", jobRepository).<Member, FullNameMember>chunk(5, jpaTransactionManager)
				.listener(slaveStepExecutionListener()).reader(itemReader(null)).processor(itemProcessor())
				.writer(jpaItemWriter(emf)).build();
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
	public ItemWriter<FullNameMember> itemWriter(@Qualifier(value = "dynamicDataSource") DataSource dynamicDataSource) {
		return new MemberItemWriter(dynamicDataSource);
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
		return new SimpleAsyncTaskExecutor();
	}

	@Bean
	public SlaveStepExecutionListener slaveStepExecutionListener() {
		return new SlaveStepExecutionListener();
	}

	@Bean
	@Qualifier(value = "jpaItemWriter")
	@StepScope
	public ItemWriter<FullNameMember> jpaItemWriter(
			@Qualifier(value = "entityManagerFactory") EntityManagerFactory emf) {
		return new JpaItemWriterBuilder<FullNameMember>().entityManagerFactory(emf).usePersist(true).build();
	}

}

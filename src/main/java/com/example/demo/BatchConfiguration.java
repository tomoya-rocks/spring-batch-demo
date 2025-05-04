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
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;

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
				.start(memberCsvTaskletStep(jobRepository, transactionManager)).next(masterStep(jobRepository)).build();
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
		return new StepBuilder("slave", jobRepository).<Member, FullNameMember>chunk(5, businessJpaTransactionManager())
				.listener(slaveStepExecutionListener()).reader(itemReader(null)).processor(itemProcessor())
				.writer(jpaItemWriter()).build();
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
		return dynamicRoutingDataSource().determineTargetDataSource();
	}

	@Bean
	public DataSourceTransactionManager dynamicTransactionManager() {
		return new DataSourceTransactionManager(dynamicDataSource());
	}

	@Bean
	public DynamicRoutingDataSource dynamicRoutingDataSource() {
		return new DynamicRoutingDataSource();
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
		return new MemberCsvPartitioner(exectionContext(), dynamicRoutingDataSource());
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

	@Bean
	@Qualifier(value = "jpaItemWriter")
	@StepScope
	public ItemWriter<FullNameMember> jpaItemWriter() {
		return new JpaItemWriterBuilder<FullNameMember>().entityManagerFactory(entityManagerFactory()).usePersist(true)
				.build();
	}

}

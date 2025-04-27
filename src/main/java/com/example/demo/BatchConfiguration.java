package com.example.demo;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@Import(value = { JobRepositoryConfiguration.class, BusinessDataSourceConfiguration.class })
public class BatchConfiguration {

	@Bean
	public ItemReader<Member> itemReader() {
		return new FlatFileItemReaderBuilder<Member>().name("memberItemReader")
				.resource(new ClassPathResource("member.csv")).delimited().names("id", "firstName", "lastName")
				.linesToSkip(1).targetType(Member.class).build();
	}

	@Bean
	public ItemProcessor<Member, FullNameMember> itemProcessor() {
		return new MemberItemProcessor();
	}

	@Bean
	public ItemWriter<FullNameMember> itemWriter(DataSource businessDataSource) {
		return new MemberItemWriter(businessDataSource);
	}

	@Bean
	public StepExecutionListener stepExecutionListener(DataSource businessDataSource) {
		return new MemberStepListener(businessDataSource);
	}

	@Bean
	public Job importMemberJob(JobRepository jobRepository, Step step1) {
		return new JobBuilder("importMemberJob" + System.currentTimeMillis(), jobRepository).start(step1).build();
	}

	@Bean
	public Step memberStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
			ItemReader<Member> reader, ItemProcessor<Member, FullNameMember> processor,
			ItemWriter<FullNameMember> writer, StepExecutionListener listener) {
		return new StepBuilder("step1", jobRepository).<Member, FullNameMember>chunk(3, transactionManager)
				.reader(reader).processor(processor).writer(writer).listener(listener).build();
	}

}

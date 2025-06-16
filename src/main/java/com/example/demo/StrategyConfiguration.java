package com.example.demo;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;

@Configuration
@Import(value = { DataSourceConfiguration.class })
public class StrategyConfiguration {

	@Bean
	public HandleMemberCsvStrategy handleMemberCsvStrategy(FlatFileItemReader<Member> itemReader,
			ItemProcessor<Member, FullNameMember> itemProcessor, ItemWriter<FullNameMember> itemWriter) {
		return new HandleMemberCsvStrategy(itemReader, itemProcessor, itemWriter);
	}

	@Bean
	public HandleCsvStrategyFactory handleCsvStrategyFactory(HandleMemberCsvStrategy handleMemberCsvStrategy) {
		List<HandleCsvStrategy> strategies = List.of(handleMemberCsvStrategy);
		HandleCsvStrategyFactory handleCsvStrategyFactory = new HandleCsvStrategyFactory(strategies);

		return handleCsvStrategyFactory;
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

}

package com.example.demo;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.stereotype.Component;

@Component("handleMemberCsvStrategy")
public class HandleMemberCsvStrategy implements HandleCsvStrategy {

	private final FlatFileItemReader<Member> itemReader;

	private final ItemProcessor<Member, FullNameMember> itemProcessor;

	private final ItemWriter<FullNameMember> itemWriter;

	public HandleMemberCsvStrategy(FlatFileItemReader<Member> itemReader,
			ItemProcessor<Member, FullNameMember> itemProcessor, ItemWriter<FullNameMember> itemWriter) {
		this.itemReader = itemReader;
		this.itemProcessor = itemProcessor;
		this.itemWriter = itemWriter;
	}

	@Override
	public FlatFileItemReader<Member> itemReader() {
		return this.itemReader;
	}

	@Override
	public ItemProcessor<Member, FullNameMember> itemProcessor() {
		return this.itemProcessor;
	}

	@Override
	public ItemWriter<FullNameMember> itemWriter() {
		return this.itemWriter;
	}

}

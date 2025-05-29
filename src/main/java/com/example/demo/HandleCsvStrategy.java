package com.example.demo;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;

public interface HandleCsvStrategy<I, O> {

	FlatFileItemReader<I> itemReader();

	ItemProcessor<I, O> itemProcessor();

	ItemWriter<O> itemWriter();

}

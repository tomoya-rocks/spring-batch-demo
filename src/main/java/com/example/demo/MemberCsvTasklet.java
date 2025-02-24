package com.example.demo;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;

public class MemberCsvTasklet implements Tasklet {

	private static final Logger logger = LoggerFactory.getLogger(MemberCsvTasklet.class);

	private final ExecutionContext executionContext;

	public MemberCsvTasklet(ExecutionContext executionContext) {
		this.executionContext = executionContext;
	}

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		logger.info("MemberCsvTasklet start!");

		List<String> csvList = List.of("bon_jovi.csv", "iron_maiden.csv", "metallica.csv");
		this.executionContext.put("csvList", csvList);

		return RepeatStatus.FINISHED;
	}

}

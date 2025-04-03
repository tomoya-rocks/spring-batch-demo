package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

public class SlaveStepExecutionListener implements StepExecutionListener {

	private static final Logger logger = LoggerFactory.getLogger(SlaveStepExecutionListener.class);

	@Override
	public void beforeStep(StepExecution stepExecution) {
		logger.info("SlaveStepExecutionListener#beforeStep called.");

		DatabaseConfig thisStepDatabaseConfig = (DatabaseConfig) stepExecution.getExecutionContext()
				.get("databaseConfig");
		DatabaseIdContextHolder.setThreadLocalDatabaseId(thisStepDatabaseConfig.dbName());
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		logger.info("SlaveStepExecutionListener#afterStep called.");

		DatabaseIdContextHolder.removeThreadLocalDatabaseId();

		return ExitStatus.COMPLETED;
	}

}

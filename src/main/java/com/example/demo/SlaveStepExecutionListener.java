package com.example.demo;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

public class SlaveStepExecutionListener implements StepExecutionListener {

	public static ThreadLocal<DatabaseConfig> databaseConfig = new ThreadLocal<DatabaseConfig>();

	public SlaveStepExecutionListener() {
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		DatabaseConfig thisStepDatabaseConfig = (DatabaseConfig) stepExecution.getExecutionContext()
				.get("databaseConfig");

		databaseConfig.set(thisStepDatabaseConfig);
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		databaseConfig.remove();

		return ExitStatus.COMPLETED;
	}

}

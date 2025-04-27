package com.example.demo;

import javax.sql.DataSource;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;

public class MemberStepListener implements StepExecutionListener {

	private final JdbcOperations jdbcTemplate;

	public MemberStepListener(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	@Override
	public void beforeStep(StepExecution stepExecution) {
		String sql = "select count(*) from member;";

		int count = this.jdbcTemplate.queryForObject(sql, Integer.class);
		System.out.println("count = " + count);
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		String sql = "select count(*) from member;";

		int count = this.jdbcTemplate.queryForObject(sql, Integer.class);
		System.out.println("count = " + count);

		return ExitStatus.COMPLETED;
	}

}

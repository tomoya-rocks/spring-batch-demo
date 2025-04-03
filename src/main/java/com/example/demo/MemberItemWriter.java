package com.example.demo;

import javax.sql.DataSource;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;

public class MemberItemWriter implements ItemWriter<FullNameMember> {

	private final JdbcOperations jdbcTemplate;

	public MemberItemWriter(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public void write(Chunk<? extends FullNameMember> chunk) throws Exception {
		for (FullNameMember fullNameMember : chunk) {
			String sql = "insert into member values (?, ?, ?, ?)";

			this.jdbcTemplate.update(sql, fullNameMember.id(), fullNameMember.firstName(),
					fullNameMember.lastName(), fullNameMember.fullName());
		}
	}

}

package com.example.demo;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

public class SpringDataJdbcMemberItemWriter implements ItemWriter<FullNameMember> {

	private final String databaseId;

	public SpringDataJdbcMemberItemWriter(String databaseId) {
		this.databaseId = databaseId;
	}

	@Autowired
	private MemberRepostitory memberRepostitory;

	@Override
	public void write(Chunk<? extends FullNameMember> chunk) throws Exception {
		try {
			DatabaseIdContextHolder.setThreadLocalDatabaseId(this.databaseId);
			for (FullNameMember fullNameMember : chunk) {
				this.memberRepostitory.insert(fullNameMember.id(), fullNameMember.firstName(),
						fullNameMember.lastName(), fullNameMember.fullName());
			}
		} finally {
			DatabaseIdContextHolder.removeThreadLocalDatabaseId();
		}
	}

}

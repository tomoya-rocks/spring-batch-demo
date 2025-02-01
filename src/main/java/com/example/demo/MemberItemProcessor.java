package com.example.demo;

import org.springframework.batch.item.ItemProcessor;

public class MemberItemProcessor implements ItemProcessor<Member, FullNameMember> {

	@Override
	public FullNameMember process(Member item) throws Exception {
		String fullName = item.firstName() + " " + item.lastName();

		return new FullNameMember(item.id(), item.firstName(), item.lastName(), fullName);
	}

}

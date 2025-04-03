package com.example.demo;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepostitory extends ListCrudRepository<FullNameMember, String> {

	@Modifying
	@Query("INSERT INTO member VALUES (:id, :first_name, :last_name, :full_name)")
	void insert(@Param("id") String id, @Param("first_name") String firstName, @Param("last_name") String lastName,
			@Param("full_name") String fullName);

}

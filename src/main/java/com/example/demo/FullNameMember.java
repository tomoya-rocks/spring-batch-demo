package com.example.demo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "member")
public record FullNameMember(@Id @Column(name = "id") String id, @Column(name = "first_name") String firstName,
		@Column(name = "last_name") String lastName, @Column(name = "full_name") String fullName) {
}

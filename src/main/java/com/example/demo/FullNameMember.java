package com.example.demo;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("MEMBER")
public record FullNameMember(@Id String id, String firstName, String lastName, String fullName) {
}

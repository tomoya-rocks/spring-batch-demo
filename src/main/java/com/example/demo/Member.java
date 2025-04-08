package com.example.demo;

import jakarta.validation.constraints.NotBlank;

public record Member(@NotBlank String id, @NotBlank String firstName, @NotBlank String lastName) {
}

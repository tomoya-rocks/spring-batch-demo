package com.example.demo;

import java.io.Serializable;

public record DatabaseConfig(String host, String username, String password, String dbName) implements Serializable {
}

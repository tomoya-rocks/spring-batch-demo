package com.example.demo;

public class DatabaseIdContextHolder {

	private static final ThreadLocal<String> threadLocalDatabaseId = new ThreadLocal<String>();

	public static void setThreadLocalDatabaseId(String databaseId) {
		threadLocalDatabaseId.set(databaseId);
	}

	public static String getThreadLocalDatabaseId() {
		return threadLocalDatabaseId.get();
	}

	public static void removeThreadLocalDatabaseId() {
		threadLocalDatabaseId.remove();
	}

}

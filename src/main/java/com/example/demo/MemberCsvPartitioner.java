package com.example.demo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

public class MemberCsvPartitioner implements Partitioner {

	private final ExecutionContext executionContext;

	public MemberCsvPartitioner(ExecutionContext executionContext) {
		this.executionContext = executionContext;
	}

	@Override
	public Map<String, ExecutionContext> partition(int gridSize) {
		Map<String, ExecutionContext> contextMap = new HashMap<>();

		@SuppressWarnings("unchecked")
		List<String> csvList = (List<String>) this.executionContext.get("csvList");

		int partitionIndex = 0;
		for (String csvFile : csvList) {
			DatabaseConfig databaseConfig = new DatabaseConfig("localhost", "testuser", "testpass",
					"testdb" + (partitionIndex + 1));

			ExecutionContext context = new ExecutionContext();
			context.put("csvFile", csvFile);
			context.put("databaseConfig", databaseConfig);

			contextMap.put("partition" + partitionIndex, context);
			
			partitionIndex++;
		}

		return contextMap;
	}

}

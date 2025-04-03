package com.example.demo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.boot.jdbc.DataSourceBuilder;

public class MemberCsvPartitioner implements Partitioner {

	private final ExecutionContext executionContext;
	
	private final DynamicRoutingDataSource dynamicRoutingDataSource;

	public MemberCsvPartitioner(ExecutionContext executionContext, DynamicRoutingDataSource dynamicRoutingDataSource) {
		this.executionContext = executionContext;
		this.dynamicRoutingDataSource = dynamicRoutingDataSource;
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

			DataSource dataSoruce = DataSourceBuilder.create()
					.url(String.format("jdbc:postgresql://%s/%s", databaseConfig.host(), databaseConfig.dbName()))
					.username(databaseConfig.username()).password(databaseConfig.password())
					.driverClassName("org.postgresql.Driver").build();
			this.dynamicRoutingDataSource.addDataSource(databaseConfig.dbName(), dataSoruce);

			partitionIndex++;
		}

		return contextMap;
	}

}

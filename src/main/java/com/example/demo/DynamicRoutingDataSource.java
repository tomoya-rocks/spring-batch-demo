package com.example.demo;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class DynamicRoutingDataSource extends AbstractRoutingDataSource {
		
	private static final Logger logger = LoggerFactory.getLogger(DynamicRoutingDataSource.class);
	
	private final Map<String, DataSource> dataSourceMap = new HashMap<String, DataSource>();

	@Override
	public void afterPropertiesSet() {
		logger.info("DynamicRoutingDataSource afterPropertiesSet called.");
	}

	public void addDataSource(String shopId, DataSource dataSource) {
		this.dataSourceMap.put(shopId, dataSource);
	}
	
	@Override
	protected DataSource determineTargetDataSource() {
		Object loopUpKey = determineCurrentLookupKey();
		DataSource dataSource = this.dataSourceMap.get(loopUpKey);
		if (dataSource == null) {
			throw new IllegalStateException("DataSource is not found. lookup key = " + loopUpKey);
		}

		return dataSource;
	}

	@Override
	protected Object determineCurrentLookupKey() {
		return DatabaseIdContextHolder.getThreadLocalDatabaseId();
	}

}

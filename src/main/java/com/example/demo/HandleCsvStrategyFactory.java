package com.example.demo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

public class HandleCsvStrategyFactory {

	private final Map<String, HandleCsvStrategy> strategyMap;

	public HandleCsvStrategyFactory(List<HandleCsvStrategy> strategies) {
		this.strategyMap = strategies.stream()
				.collect(Collectors.toMap(s -> s.getClass().getAnnotation(Component.class).value(), s -> s));
	}

	public HandleCsvStrategy createHandleCsvStrategy(String name) {
		return this.strategyMap.get(name);
	}

}

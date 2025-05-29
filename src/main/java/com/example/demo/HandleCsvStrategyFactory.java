package com.example.demo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

public class HandleCsvStrategyFactory<I, O> {

	private final Map<Object, Object> strategyMap;

	public HandleCsvStrategyFactory(List<HandleCsvStrategy<I, O>> strategies) {
		this.strategyMap = strategies.stream()
				.collect(Collectors.toMap(s -> s.getClass().getAnnotation(Component.class).value(), s -> s));
	}

	@SuppressWarnings("unchecked")
	public HandleCsvStrategy<I, O> createHandleCsvStrategy(String name) {
		return (HandleCsvStrategy<I, O>) this.strategyMap.get(name);
	}

}

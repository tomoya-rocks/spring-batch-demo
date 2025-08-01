package com.example.demo;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class JobRepositoryLoggingDenyFilter extends Filter<ILoggingEvent> {

	private final String value = "BATCH_";

	@Override
	public FilterReply decide(ILoggingEvent event) {
		if (event.getMessage().contains(this.value)) {
			return FilterReply.DENY;
		} else {
			return FilterReply.NEUTRAL;
		}
	}

}

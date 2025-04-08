package com.example.demo;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.validator.BeanValidatingItemProcessor;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.context.MessageSource;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

public class LoggingBeanValidatingItemProcessor<T> extends BeanValidatingItemProcessor<T> {

	private static final Logger logger = LoggerFactory.getLogger(LoggingBeanValidatingItemProcessor.class);

	private final MessageSource messageSource;

	public LoggingBeanValidatingItemProcessor(LocalValidatorFactoryBean localValidatorFactoryBean,
			MessageSource messageSource) {
		super(localValidatorFactoryBean);
		this.messageSource = messageSource;
	}

	@Override
	public T process(T item) throws ValidationException {
		try {
			return super.process(item);
		} catch (ValidationException ex) {
			BindException cause = (BindException) ex.getCause();
			BindingResult result = cause.getBindingResult();
			List<ObjectError> objectErrors = result.getAllErrors();
			for (ObjectError error : objectErrors) {
				String[] codes = error.getCodes();
				Object[] arguments = error.getArguments();
				String message = this.messageSource.getMessage(codes[codes.length - 1], arguments, null);

				logger.warn(message);
			}

			return null;
		}
	}

}

package com.example.demo;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import jakarta.persistence.EntityManagerFactory;

@Configuration
public class BusinessDataSourceConfiguration {

	@Bean
	@Qualifier(value = "businessDataSource")
	@ConfigurationProperties(prefix = "spring.datasource.business")
	public DataSource businessDataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean
	@Qualifier(value = "businessTransactionManager")
	public DataSourceTransactionManager businessTransactionManager() {
		return new DataSourceTransactionManager(businessDataSource());
	}

	@Bean
	@Qualifier(value = "entityManagerFactory")
	public EntityManagerFactory entityManagerFactory() {
		LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();

		localContainerEntityManagerFactoryBean.setDataSource(businessDataSource());
		localContainerEntityManagerFactoryBean.setPackagesToScan("com.example.demo");
		localContainerEntityManagerFactoryBean.setPersistenceUnitName("businessEntityManagerFactory");
		localContainerEntityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
		localContainerEntityManagerFactoryBean.afterPropertiesSet();

		return localContainerEntityManagerFactoryBean.getObject();
	}

	@Bean
	@Qualifier(value = "businessJpaTransactionManager")
	public JpaTransactionManager businessJpaTransactionManager() {
		return new JpaTransactionManager(entityManagerFactory());
	}

}

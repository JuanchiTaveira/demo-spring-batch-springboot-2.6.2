package org.example.config;

import org.example.mysql.entity.StudentMysql;
import org.example.postgresql.entity.StudentPostgres;
import org.example.processor.StudentItemProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
public class SampleJob {

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;
	
	@Autowired
	private StudentItemProcessor studentItemProcessor;
	
	@Autowired
	@Qualifier("universitydatasource")
	private DataSource universitydatasource;
	
	@Autowired
	@Qualifier("postgresdatasource")
	private DataSource postgresdatasource;
	
	@Autowired
	@Qualifier("postgresqlEntityManagerFactory")
	private EntityManagerFactory postgresqlEntityManagerFactory;
	
	@Autowired
	@Qualifier("mysqlEntityManagerFactory")
	private EntityManagerFactory mysqlEntityManagerFactory;
	
	@Autowired
	private JpaTransactionManager jpaTransactionManager;

	@Bean
	public Job studentMigrationJob() {
		return jobBuilderFactory.get("student migration job")
				.incrementer(new RunIdIncrementer())
				.start(firstStudentMigrationStep())
				.build();
	}
	
	private Step firstStudentMigrationStep() {
		return stepBuilderFactory.get("first StudentPostgres Migration Step")
				.<StudentPostgres, StudentMysql>chunk(3)
				.reader(jpaPagingItemReader())
				.processor(studentItemProcessor)
				.writer(jpaItemWriter())
				.faultTolerant()
				.skip(Throwable.class)
				.skipLimit(100)
				.retryLimit(3)
				.retry(Throwable.class)
				.transactionManager(jpaTransactionManager)
				.build();
	}
	
	public JpaPagingItemReader<StudentPostgres> jpaPagingItemReader() {
		JpaPagingItemReader<StudentPostgres> jpaPagingItemReader =
				new JpaPagingItemReader<>();
		
		jpaPagingItemReader.setEntityManagerFactory(postgresqlEntityManagerFactory);
		
		jpaPagingItemReader.setQueryString("Select s From StudentPostgres s");
		
		return jpaPagingItemReader;
	}
	
	public JpaItemWriter<StudentMysql> jpaItemWriter() {
		JpaItemWriter<StudentMysql> jpaItemWriter = 
				new JpaItemWriter<>();
		
		jpaItemWriter.setEntityManagerFactory(mysqlEntityManagerFactory);
		
		return jpaItemWriter;
	}
}

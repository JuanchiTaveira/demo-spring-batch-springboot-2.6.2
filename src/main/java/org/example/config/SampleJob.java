package org.example.config;

import org.example.postgresql.entity.Student;
import org.example.processor.StudentItemProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
public class SampleJob {

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
	public Job studentMigrationJob(Step step, JobRepository jobRepository) {
		return new JobBuilder("studentMigrationJob", jobRepository)
				.incrementer(new RunIdIncrementer())
				.start(step)
				.build();
	}

	@Bean
	public Step firstStudentMigrationStep(JobRepository jobRepository) {
		return new StepBuilder("firstStudentMigrationStep", jobRepository)
				.<Student, org.example.mysql.entity.Student>chunk(3, jpaTransactionManager)
				.reader(jpaPagingItemReader())
				.processor(studentItemProcessor)
				.writer(jpaItemWriter())
				.faultTolerant()
				.skip(Throwable.class)
				.skipLimit(100)
				.retryLimit(3)
				.retry(Throwable.class)
				.build();
	}
	
	public JpaPagingItemReader<Student> jpaPagingItemReader() {
		JpaPagingItemReader<Student> jpaPagingItemReader =
				new JpaPagingItemReader<>();
		
		jpaPagingItemReader.setEntityManagerFactory(postgresqlEntityManagerFactory);
		
		jpaPagingItemReader.setQueryString("Select s From Student s");
		
		return jpaPagingItemReader;
	}
	
	public JpaItemWriter<org.example.mysql.entity.Student> jpaItemWriter() {
		JpaItemWriter<org.example.mysql.entity.Student> jpaItemWriter = 
				new JpaItemWriter<>();
		
		jpaItemWriter.setEntityManagerFactory(mysqlEntityManagerFactory);
		
		return jpaItemWriter;
	}
}

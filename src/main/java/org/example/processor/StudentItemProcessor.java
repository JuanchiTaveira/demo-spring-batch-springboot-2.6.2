package org.example.processor;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import org.example.postgresql.entity.Student;

@Component
public class StudentItemProcessor implements ItemProcessor<Student, org.example.mysql.entity.Student> {

	@Override
	public org.example.mysql.entity.Student process(Student item) throws Exception {
		
		System.out.println(item.getId());
		
		org.example.mysql.entity.Student student = new 
				org.example.mysql.entity.Student();
		
		student.setId(item.getId());
		student.setFirstName(item.getFirstName());
		student.setLastName(item.getLastName());
		student.setEmail(item.getEmail());
		student.setDeptId(item.getDeptId());
		student.setIsActive(item.getIsActive() != null &&
				Boolean.parseBoolean(item.getIsActive()));
		
		return student;
		
	}

}

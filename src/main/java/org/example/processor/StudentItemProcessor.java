package org.example.processor;

import org.example.mysql.entity.StudentMysql;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import org.example.postgresql.entity.StudentPostgres;

@Component
public class StudentItemProcessor implements ItemProcessor<StudentPostgres, StudentMysql> {

    @Override
    public StudentMysql process(StudentPostgres item) throws Exception {

        System.out.println(item.getId());

        return StudentMysql.builder()
                .id(item.getId())
                .firstName(item.getFirstName())
                .lastName(item.getLastName())
                .email(item.getEmail())
                .deptId(item.getDeptId())
                .isActive(item.getIsActive() != null &&
                        Boolean.parseBoolean(item.getIsActive()))
                .build();
    }

}

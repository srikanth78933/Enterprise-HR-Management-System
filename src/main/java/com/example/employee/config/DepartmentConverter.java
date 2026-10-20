package com.example.employee.config;

import com.example.employee.entity.Department;
import com.example.employee.repository.DepartmentRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Converts the department ID selected in the employee form (a String such as "3")
 * into a Department entity. Spring Boot registers Converter beans with Spring MVC automatically.
 * An empty or unknown ID becomes null, so the @NotNull validation on Employee.department shows an error.
 */
@Component
public class DepartmentConverter implements Converter<String, Department> {

    private final DepartmentRepository departmentRepository;

    public DepartmentConverter(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    public Department convert(String source) {
        if (source.isBlank()) {
            return null;
        }
        try {
            return departmentRepository.findById(Long.valueOf(source.trim())).orElse(null);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}

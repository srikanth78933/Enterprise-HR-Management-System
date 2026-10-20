package com.example.employee.service;

import com.example.employee.entity.Department;
import com.example.employee.exception.DuplicateValueException;
import com.example.employee.exception.OperationNotAllowedException;
import com.example.employee.exception.ResourceNotFoundException;
import com.example.employee.repository.DepartmentRepository;
import com.example.employee.repository.EmployeeRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    public DepartmentService(DepartmentRepository departmentRepository, EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
    }

    public List<Department> findAll() {
        return departmentRepository.findAll(Sort.by("name"));
    }

    public Department findById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID " + id));
    }

    public long count() {
        return departmentRepository.count();
    }

    /**
     * Returns a map of department ID to number of employees in that department.
     */
    public Map<Long, Long> getEmployeeCounts() {
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : employeeRepository.countEmployeesPerDepartment()) {
            counts.put((Long) row[0], (Long) row[1]);
        }
        return counts;
    }

    @Transactional
    public Department create(Department department) {
        String name = department.getName().trim();
        if (departmentRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateValueException("name", "A department named '" + name + "' already exists");
        }
        department.setId(null);
        department.setName(name);
        return departmentRepository.save(department);
    }

    @Transactional
    public Department update(Long id, Department form) {
        Department existing = findById(id);
        String name = form.getName().trim();
        if (departmentRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateValueException("name", "A department named '" + name + "' already exists");
        }
        existing.setName(name);
        existing.setDescription(form.getDescription());
        return departmentRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Department department = findById(id);
        long employeeCount = employeeRepository.countByDepartmentId(id);
        if (employeeCount > 0) {
            throw new OperationNotAllowedException("Cannot delete department '" + department.getName()
                    + "' because it has " + employeeCount + " employee(s). Move or delete them first.");
        }
        departmentRepository.delete(department);
    }
}

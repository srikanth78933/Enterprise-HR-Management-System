package com.example.employee.service;

import com.example.employee.entity.Employee;
import com.example.employee.entity.EmployeeStatus;
import com.example.employee.exception.DuplicateValueException;
import com.example.employee.exception.ResourceNotFoundException;
import com.example.employee.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    /**
     * Searches employees by name or employee ID, optionally limited to one department.
     * An empty keyword matches all employees.
     */
    public List<Employee> search(String keyword, Long departmentId) {
        String trimmedKeyword = keyword == null ? "" : keyword.trim();
        if (departmentId == null) {
            return employeeRepository.search(trimmedKeyword);
        }
        return employeeRepository.searchInDepartment(trimmedKeyword, departmentId);
    }

    public Employee findById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID " + id));
    }

    public List<Employee> findRecent() {
        return employeeRepository.findTop5ByOrderByIdDesc();
    }

    public long count() {
        return employeeRepository.count();
    }

    public long countActive() {
        return employeeRepository.countByStatus(EmployeeStatus.ACTIVE);
    }

    @Transactional
    public Employee create(Employee employee) {
        normalize(employee);
        if (employeeRepository.existsByEmployeeCodeIgnoreCase(employee.getEmployeeCode())) {
            throw new DuplicateValueException("employeeCode",
                    "Employee ID '" + employee.getEmployeeCode() + "' is already in use");
        }
        if (employeeRepository.existsByEmailIgnoreCase(employee.getEmail())) {
            throw new DuplicateValueException("email", "Email '" + employee.getEmail() + "' is already in use");
        }
        employee.setId(null);
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee update(Long id, Employee form) {
        Employee existing = findById(id);
        normalize(form);
        if (employeeRepository.existsByEmployeeCodeIgnoreCaseAndIdNot(form.getEmployeeCode(), id)) {
            throw new DuplicateValueException("employeeCode",
                    "Employee ID '" + form.getEmployeeCode() + "' is already in use");
        }
        if (employeeRepository.existsByEmailIgnoreCaseAndIdNot(form.getEmail(), id)) {
            throw new DuplicateValueException("email", "Email '" + form.getEmail() + "' is already in use");
        }
        existing.setEmployeeCode(form.getEmployeeCode());
        existing.setFirstName(form.getFirstName());
        existing.setLastName(form.getLastName());
        existing.setEmail(form.getEmail());
        existing.setPhone(form.getPhone());
        existing.setDepartment(form.getDepartment());
        existing.setJobTitle(form.getJobTitle());
        existing.setJoiningDate(form.getJoiningDate());
        existing.setSalary(form.getSalary());
        existing.setStatus(form.getStatus());
        return employeeRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Employee employee = findById(id);
        employeeRepository.delete(employee);
    }

    // Trims text input and stores employee IDs in upper case and emails in lower case
    private void normalize(Employee employee) {
        employee.setEmployeeCode(employee.getEmployeeCode().trim().toUpperCase());
        employee.setFirstName(employee.getFirstName().trim());
        employee.setLastName(employee.getLastName().trim());
        employee.setEmail(employee.getEmail().trim().toLowerCase());
        employee.setJobTitle(employee.getJobTitle().trim());
        if (employee.getPhone() != null) {
            String phone = employee.getPhone().trim();
            employee.setPhone(phone.isEmpty() ? null : phone);
        }
    }
}

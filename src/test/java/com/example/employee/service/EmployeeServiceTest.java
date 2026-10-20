package com.example.employee.service;

import com.example.employee.entity.Department;
import com.example.employee.entity.Employee;
import com.example.employee.entity.EmployeeStatus;
import com.example.employee.exception.DuplicateValueException;
import com.example.employee.exception.ResourceNotFoundException;
import com.example.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Department itDepartment;

    @BeforeEach
    void setUp() {
        itDepartment = new Department("IT", "Information Technology");
        itDepartment.setId(1L);
    }

    private Employee newEmployee() {
        Employee employee = new Employee();
        employee.setEmployeeCode(" emp100 ");
        employee.setFirstName(" John ");
        employee.setLastName("Doe");
        employee.setEmail("John.Doe@Example.com");
        employee.setPhone("  ");
        employee.setDepartment(itDepartment);
        employee.setJobTitle("Developer");
        employee.setJoiningDate(LocalDate.of(2024, 1, 15));
        employee.setSalary(new BigDecimal("50000.00"));
        employee.setStatus(EmployeeStatus.ACTIVE);
        return employee;
    }

    @Test
    void createSavesEmployeeWithNormalizedValues() {
        Employee employee = newEmployee();
        when(employeeRepository.existsByEmployeeCodeIgnoreCase("EMP100")).thenReturn(false);
        when(employeeRepository.existsByEmailIgnoreCase("john.doe@example.com")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        Employee saved = employeeService.create(employee);

        assertThat(saved.getId()).isEqualTo(10L);
        assertThat(saved.getEmployeeCode()).isEqualTo("EMP100");
        assertThat(saved.getFirstName()).isEqualTo("John");
        assertThat(saved.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(saved.getPhone()).isNull();
        verify(employeeRepository).save(employee);
    }

    @Test
    void createRejectsDuplicateEmployeeCode() {
        Employee employee = newEmployee();
        when(employeeRepository.existsByEmployeeCodeIgnoreCase("EMP100")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.create(employee))
                .isInstanceOf(DuplicateValueException.class)
                .hasMessageContaining("EMP100")
                .extracting("field").isEqualTo("employeeCode");
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void createRejectsDuplicateEmail() {
        Employee employee = newEmployee();
        when(employeeRepository.existsByEmployeeCodeIgnoreCase("EMP100")).thenReturn(false);
        when(employeeRepository.existsByEmailIgnoreCase("john.doe@example.com")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.create(employee))
                .isInstanceOf(DuplicateValueException.class)
                .extracting("field").isEqualTo("email");
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void findByIdReturnsEmployee() {
        Employee employee = newEmployee();
        employee.setId(5L);
        when(employeeRepository.findById(5L)).thenReturn(Optional.of(employee));

        assertThat(employeeService.findById(5L)).isSameAs(employee);
    }

    @Test
    void findByIdThrowsWhenEmployeeDoesNotExist() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void updateCopiesFormValuesToExistingEmployee() {
        Employee existing = newEmployee();
        existing.setId(5L);
        when(employeeRepository.findById(5L)).thenReturn(Optional.of(existing));

        Employee form = newEmployee();
        form.setJobTitle("Senior Developer");
        form.setSalary(new BigDecimal("65000.00"));
        form.setStatus(EmployeeStatus.INACTIVE);
        when(employeeRepository.existsByEmployeeCodeIgnoreCaseAndIdNot("EMP100", 5L)).thenReturn(false);
        when(employeeRepository.existsByEmailIgnoreCaseAndIdNot("john.doe@example.com", 5L)).thenReturn(false);
        when(employeeRepository.save(existing)).thenReturn(existing);

        Employee updated = employeeService.update(5L, form);

        assertThat(updated.getId()).isEqualTo(5L);
        assertThat(updated.getJobTitle()).isEqualTo("Senior Developer");
        assertThat(updated.getSalary()).isEqualByComparingTo("65000.00");
        assertThat(updated.getStatus()).isEqualTo(EmployeeStatus.INACTIVE);
    }

    @Test
    void deleteRemovesExistingEmployee() {
        Employee employee = newEmployee();
        employee.setId(5L);
        when(employeeRepository.findById(5L)).thenReturn(Optional.of(employee));

        employeeService.delete(5L);

        verify(employeeRepository).delete(employee);
    }

    @Test
    void searchWithoutDepartmentUsesTrimmedKeyword() {
        List<Employee> result = List.of(newEmployee());
        when(employeeRepository.search("john")).thenReturn(result);

        assertThat(employeeService.search("  john ", null)).isEqualTo(result);
    }

    @Test
    void searchWithDepartmentFiltersByDepartment() {
        List<Employee> result = List.of(newEmployee());
        when(employeeRepository.searchInDepartment("", 1L)).thenReturn(result);

        assertThat(employeeService.search(null, 1L)).isEqualTo(result);
    }

    @Test
    void countActiveCountsOnlyActiveEmployees() {
        when(employeeRepository.countByStatus(EmployeeStatus.ACTIVE)).thenReturn(4L);

        assertThat(employeeService.countActive()).isEqualTo(4L);
    }
}

package com.example.employee.service;

import com.example.employee.entity.Department;
import com.example.employee.exception.DuplicateValueException;
import com.example.employee.exception.OperationNotAllowedException;
import com.example.employee.repository.DepartmentRepository;
import com.example.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private DepartmentService departmentService;

    @Test
    void deleteRemovesDepartmentWithoutEmployees() {
        Department department = new Department("Legal", "Legal team");
        department.setId(7L);
        when(departmentRepository.findById(7L)).thenReturn(Optional.of(department));
        when(employeeRepository.countByDepartmentId(7L)).thenReturn(0L);

        departmentService.delete(7L);

        verify(departmentRepository).delete(department);
    }

    @Test
    void deleteIsBlockedWhenDepartmentHasEmployees() {
        Department department = new Department("IT", "Information Technology");
        department.setId(1L);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(employeeRepository.countByDepartmentId(1L)).thenReturn(3L);

        assertThatThrownBy(() -> departmentService.delete(1L))
                .isInstanceOf(OperationNotAllowedException.class)
                .hasMessageContaining("3 employee(s)");
        verify(departmentRepository, never()).delete(any());
    }

    @Test
    void createRejectsDuplicateName() {
        when(departmentRepository.existsByNameIgnoreCase("IT")).thenReturn(true);

        assertThatThrownBy(() -> departmentService.create(new Department(" IT ", null)))
                .isInstanceOf(DuplicateValueException.class);
        verify(departmentRepository, never()).save(any());
    }
}

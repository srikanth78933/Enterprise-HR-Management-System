package com.example.employee.repository;

import com.example.employee.entity.Employee;
import com.example.employee.entity.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Matches employee ID, first name, last name or "first last"
    String KEYWORD_CONDITION = """
            (LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(CONCAT(e.firstName, ' ', e.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """;

    @Query("SELECT e FROM Employee e WHERE " + KEYWORD_CONDITION + " ORDER BY e.employeeCode")
    List<Employee> search(@Param("keyword") String keyword);

    @Query("SELECT e FROM Employee e WHERE e.department.id = :departmentId AND " + KEYWORD_CONDITION
            + " ORDER BY e.employeeCode")
    List<Employee> searchInDepartment(@Param("keyword") String keyword, @Param("departmentId") Long departmentId);

    List<Employee> findTop5ByOrderByIdDesc();

    long countByStatus(EmployeeStatus status);

    long countByDepartmentId(Long departmentId);

    @Query("SELECT e.department.id, COUNT(e) FROM Employee e GROUP BY e.department.id")
    List<Object[]> countEmployeesPerDepartment();

    boolean existsByEmployeeCodeIgnoreCase(String employeeCode);

    boolean existsByEmployeeCodeIgnoreCaseAndIdNot(String employeeCode, Long id);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}

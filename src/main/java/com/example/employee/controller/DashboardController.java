package com.example.employee.controller;

import com.example.employee.service.DepartmentService;
import com.example.employee.service.EmployeeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final EmployeeService employeeService;
    private final DepartmentService departmentService;

    public DashboardController(EmployeeService employeeService, DepartmentService departmentService) {
        this.employeeService = employeeService;
        this.departmentService = departmentService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        long totalEmployees = employeeService.count();
        long activeEmployees = employeeService.countActive();

        model.addAttribute("totalEmployees", totalEmployees);
        model.addAttribute("totalDepartments", departmentService.count());
        model.addAttribute("activeEmployees", activeEmployees);
        model.addAttribute("inactiveEmployees", totalEmployees - activeEmployees);
        model.addAttribute("recentEmployees", employeeService.findRecent());
        return "dashboard";
    }
}

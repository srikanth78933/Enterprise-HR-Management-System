package com.example.employee.controller;

import com.example.employee.entity.Employee;
import com.example.employee.entity.EmployeeStatus;
import com.example.employee.exception.DuplicateValueException;
import com.example.employee.service.DepartmentService;
import com.example.employee.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final DepartmentService departmentService;

    public EmployeeController(EmployeeService employeeService, DepartmentService departmentService) {
        this.employeeService = employeeService;
        this.departmentService = departmentService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Long departmentId,
                       Model model) {
        model.addAttribute("employees", employeeService.search(keyword, departmentId));
        model.addAttribute("departments", departmentService.findAll());
        model.addAttribute("keyword", keyword);
        model.addAttribute("departmentId", departmentId);
        return "employees";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("employee", employeeService.findById(id));
        return "employee-details";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        return showForm(new Employee(), model);
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("employee") Employee employee,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return showForm(employee, model);
        }
        try {
            Employee saved = employeeService.create(employee);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Employee " + saved.getFullName() + " was added successfully.");
            return "redirect:/employees/" + saved.getId();
        } catch (DuplicateValueException ex) {
            bindingResult.rejectValue(ex.getField(), "duplicate", ex.getMessage());
            return showForm(employee, model);
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        return showForm(employeeService.findById(id), model);
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("employee") Employee employee,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        employee.setId(id);
        if (bindingResult.hasErrors()) {
            return showForm(employee, model);
        }
        try {
            Employee saved = employeeService.update(id, employee);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Employee " + saved.getFullName() + " was updated successfully.");
            return "redirect:/employees/" + saved.getId();
        } catch (DuplicateValueException ex) {
            bindingResult.rejectValue(ex.getField(), "duplicate", ex.getMessage());
            return showForm(employee, model);
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Employee employee = employeeService.findById(id);
        employeeService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage",
                "Employee " + employee.getFullName() + " was deleted successfully.");
        return "redirect:/employees";
    }

    private String showForm(Employee employee, Model model) {
        model.addAttribute("employee", employee);
        model.addAttribute("departments", departmentService.findAll());
        model.addAttribute("statuses", EmployeeStatus.values());
        return "employee-form";
    }
}

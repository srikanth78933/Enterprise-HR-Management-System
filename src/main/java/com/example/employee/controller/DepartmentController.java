package com.example.employee.controller;

import com.example.employee.entity.Department;
import com.example.employee.exception.DuplicateValueException;
import com.example.employee.exception.OperationNotAllowedException;
import com.example.employee.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("departments", departmentService.findAll());
        model.addAttribute("employeeCounts", departmentService.getEmployeeCounts());
        return "departments";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("department", new Department());
        return "department-form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("department") Department department,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "department-form";
        }
        try {
            Department saved = departmentService.create(department);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Department " + saved.getName() + " was added successfully.");
            return "redirect:/departments";
        } catch (DuplicateValueException ex) {
            bindingResult.rejectValue(ex.getField(), "duplicate", ex.getMessage());
            return "department-form";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("department", departmentService.findById(id));
        return "department-form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("department") Department department,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        department.setId(id);
        if (bindingResult.hasErrors()) {
            return "department-form";
        }
        try {
            Department saved = departmentService.update(id, department);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Department " + saved.getName() + " was updated successfully.");
            return "redirect:/departments";
        } catch (DuplicateValueException ex) {
            bindingResult.rejectValue(ex.getField(), "duplicate", ex.getMessage());
            return "department-form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Department department = departmentService.findById(id);
            departmentService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Department " + department.getName() + " was deleted successfully.");
        } catch (OperationNotAllowedException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/departments";
    }
}

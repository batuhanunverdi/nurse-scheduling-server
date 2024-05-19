package com.example.nurseschedulingserver.service.interfaces;

import com.example.nurseschedulingserver.entity.department.Department;

import java.util.List;

public interface DepartmentService {

    Department getDepartmentByName(String name);

    List<Department> getAllDepartments();
}

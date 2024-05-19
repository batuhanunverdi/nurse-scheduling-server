package com.example.nurseschedulingserver.service.interfaces;

import com.example.nurseschedulingserver.entity.department.Department;

public interface DepartmentService {

    Department getDepartmentByName(String name);
}

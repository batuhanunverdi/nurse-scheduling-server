package com.example.nurseschedulingserver.service.implementations;

import com.example.nurseschedulingserver.entity.department.Department;
import com.example.nurseschedulingserver.repository.DepartmentRepository;
import com.example.nurseschedulingserver.service.interfaces.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;
    @Override
    public Department getDepartmentByName(String name) {
        return departmentRepository.findByName(name).orElse(null);
    }
}

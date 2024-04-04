package com.example.nurseschedulingserver.repository;

import com.example.nurseschedulingserver.entity.department.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department,String> {
    Optional<Department> findByName (String name);
}

package com.example.nurseschedulingserver.repository;

import com.example.nurseschedulingserver.entity.constraint.Constraint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConstraintRepository extends JpaRepository<Constraint, String> {
    Optional<Constraint> findByDepartmentId(String departmentId);
}

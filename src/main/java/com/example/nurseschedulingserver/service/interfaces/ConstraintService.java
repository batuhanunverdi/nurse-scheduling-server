package com.example.nurseschedulingserver.service.interfaces;

import com.example.nurseschedulingserver.entity.constraint.Constraint;

import java.util.List;

public interface ConstraintService {

    String createConstraint(String departmentId, List<Integer> minimumNursesForEachShift);

    String updateConstraintByDepartmentId(String departmentId, List<Integer> minimumNursesForEachShift);

    Constraint getConstraintByDepartmentId(String departmentId);

}

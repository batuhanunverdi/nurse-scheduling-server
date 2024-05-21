package com.example.nurseschedulingserver.service.interfaces;

import com.example.nurseschedulingserver.dto.constraint.ConstraintResponseDto;
import com.example.nurseschedulingserver.entity.constraint.Constraint;

import java.util.List;

public interface ConstraintService {

    ConstraintResponseDto createConstraint(String departmentId, List<Integer> minimumNursesForEachShift) throws Exception;

    ConstraintResponseDto updateConstraintByDepartmentId(String departmentId, List<Integer> minimumNursesForEachShift) throws Exception;

    Constraint getConstraintByDepartmentName(String departmentId);

}

package com.example.nurseschedulingserver.service.interfaces;

import com.example.nurseschedulingserver.entity.constraint.Constraint;
import com.example.nurseschedulingserver.entity.nurse.Nurse;

import java.util.List;

public interface CPService {
    void createShifts(List<Nurse> nurseList, Constraint constraint);
}

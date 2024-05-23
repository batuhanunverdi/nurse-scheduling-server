package com.example.nurseschedulingserver.service.interfaces;

import com.example.nurseschedulingserver.dto.workday.WorkDayRequestDto;
import com.example.nurseschedulingserver.dto.workday.WorkDayResponseDto;
import com.example.nurseschedulingserver.entity.workday.WorkDay;

import java.util.List;


public interface WorkDayService {
    WorkDayResponseDto saveWorkDays(WorkDayRequestDto workDays);


    WorkDayResponseDto getWorkDays(String month, String year);


    List<WorkDay> findWorkDayByMonthAndYear(int month, int year,String departmentId);
}

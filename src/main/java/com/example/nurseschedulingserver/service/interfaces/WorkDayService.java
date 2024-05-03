package com.example.nurseschedulingserver.service.interfaces;

import com.example.nurseschedulingserver.dto.workday.WorkDayRequestDto;
import com.example.nurseschedulingserver.dto.workday.WorkDayResponseDto;

import java.util.List;


public interface WorkDayService {
    WorkDayResponseDto saveWorkDays(WorkDayRequestDto workDays);


    List<WorkDayResponseDto> getWorkDays(int month, int year);
}

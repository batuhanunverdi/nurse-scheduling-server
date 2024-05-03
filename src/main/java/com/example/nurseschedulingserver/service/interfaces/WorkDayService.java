package com.example.nurseschedulingserver.service.interfaces;

import com.example.nurseschedulingserver.dto.workday.WorkDayRequestDto;
import com.example.nurseschedulingserver.dto.workday.WorkDayResponseDto;


public interface WorkDayService {
    WorkDayResponseDto saveWorkDays(WorkDayRequestDto workDays);


    WorkDayResponseDto getWorkDays(String month, String year);
}

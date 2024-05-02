package com.example.nurseschedulingserver.service.interfaces;

import com.example.nurseschedulingserver.dto.workday.WorkDayResponseDto;
import com.example.nurseschedulingserver.entity.workday.WorkDay;

import java.util.List;


public interface WorkDayService {
    List<WorkDayResponseDto> saveWorkDays(List<WorkDay> workDays);


}

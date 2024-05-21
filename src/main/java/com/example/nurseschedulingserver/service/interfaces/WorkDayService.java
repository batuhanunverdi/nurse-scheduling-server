package com.example.nurseschedulingserver.service.interfaces;

import com.example.nurseschedulingserver.dto.workday.WorkDayRequestDto;
import com.example.nurseschedulingserver.dto.workday.WorkDayResponseDto;
import com.example.nurseschedulingserver.entity.workday.WorkDay;

import java.util.Date;


public interface WorkDayService {
    WorkDayResponseDto saveWorkDays(WorkDayRequestDto workDays);


    WorkDayResponseDto getWorkDays(String month, String year);

    WorkDay findWorkDayByNurseId(String id, int month, int year);

    boolean checkWorkDayExistsByDate(Date date);
}

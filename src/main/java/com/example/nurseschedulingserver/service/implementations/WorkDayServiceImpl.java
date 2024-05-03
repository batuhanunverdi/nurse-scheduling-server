package com.example.nurseschedulingserver.service.implementations;

import com.example.nurseschedulingserver.dto.auth.AuthProjection;
import com.example.nurseschedulingserver.dto.workday.WorkDayRequestDto;
import com.example.nurseschedulingserver.dto.workday.WorkDayResponseDto;
import com.example.nurseschedulingserver.entity.workday.WorkDay;
import com.example.nurseschedulingserver.repository.WorkDayRepository;
import com.example.nurseschedulingserver.service.interfaces.NurseService;
import com.example.nurseschedulingserver.service.interfaces.WorkDayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;


@Service
@RequiredArgsConstructor
public class WorkDayServiceImpl implements WorkDayService {
    private final WorkDayRepository workDayRepository;
    private final NurseService nurseService;
    @Override
    public WorkDayResponseDto saveWorkDays(WorkDayRequestDto workDays) {
        AuthProjection user = nurseService.getLoggedInUser();
        Optional<WorkDay> workDayResponseDto = workDayRepository.findByNurseId(user.getId());
        WorkDay workDay;
        String message;
        if (workDayResponseDto.isPresent()) {
            workDay = workDayResponseDto.get();
            workDay.setWorkDate(workDays.getWorkDate());
            message = "Work day updated successfully";
        }else {
            workDay = new WorkDay();
            workDay.setNurseId(user.getId());
            workDay.setWorkDate(workDays.getWorkDate());
            message = "Work day created successfully";
        }

        workDay = workDayRepository.save(workDay);

        return new WorkDayResponseDto(workDay.getId(), workDay.getWorkDate(), workDay.getNurseId(),message);

    }


}

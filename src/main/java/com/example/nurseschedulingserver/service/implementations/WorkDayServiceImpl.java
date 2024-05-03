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

import java.util.ArrayList;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class WorkDayServiceImpl implements WorkDayService {
    private final WorkDayRepository workDayRepository;
    private final NurseService nurseService;
    @Override
    public WorkDayResponseDto saveWorkDays(WorkDayRequestDto workDays) {
        AuthProjection user = nurseService.getLoggedInUser();
        Optional<WorkDay> loggedInUserWorkDay = workDayRepository.findByNurseId(user.getId());
        WorkDay workDay = new WorkDay();
        String message;
        if (loggedInUserWorkDay.isPresent()) {
            workDay = loggedInUserWorkDay.get();
            workDay.setWorkDate(workDays.getWorkDate());
            message = "Work day updated successfully";
        }else {
            workDay.setNurseId(user.getId());
            workDay.setWorkDate(workDays.getWorkDate());
            message = "Work day created successfully";
        }

        workDay = workDayRepository.save(workDay);

        return new WorkDayResponseDto(workDay.getId(), workDay.getWorkDate(), workDay.getNurseId(),message);
    }

    @Override
    public WorkDayResponseDto getWorkDays(String month, String year) {
        AuthProjection user = nurseService.getLoggedInUser();
        int monthInt = Integer.parseInt(month);
        int yearInt = Integer.parseInt(year);
        WorkDay workDays =  workDayRepository.findAllByNurseIdAndDate(user.getId(),monthInt,yearInt);
        if(workDays == null){
            return new WorkDayResponseDto("",new ArrayList<>(),"","Work day not found");
        }
        return new WorkDayResponseDto(workDays.getId(),workDays.getWorkDate(),workDays.getNurseId(),"");
    }


}

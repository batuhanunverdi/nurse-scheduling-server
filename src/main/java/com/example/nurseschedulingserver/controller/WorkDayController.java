package com.example.nurseschedulingserver.controller;

import com.example.nurseschedulingserver.dto.workday.WorkDayRequestDto;
import com.example.nurseschedulingserver.dto.workday.WorkDayResponseDto;
import com.example.nurseschedulingserver.entity.workday.WorkDay;
import com.example.nurseschedulingserver.service.interfaces.WorkDayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/workdays")
@RequiredArgsConstructor
public class WorkDayController {
    private final WorkDayService workDayService;
    @PostMapping("/generate")
    public ResponseEntity<WorkDayResponseDto> postWorkDays(@RequestBody WorkDayRequestDto workDayList) {
        try{
            return new ResponseEntity<>(workDayService.saveWorkDays(workDayList), HttpStatus.OK);
        }
        catch (Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}

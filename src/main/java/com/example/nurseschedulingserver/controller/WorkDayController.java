package com.example.nurseschedulingserver.controller;

import com.example.nurseschedulingserver.dto.workday.WorkDayRequestDto;
import com.example.nurseschedulingserver.dto.workday.WorkDayResponseDto;
import com.example.nurseschedulingserver.service.interfaces.WorkDayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workdays")
@RequiredArgsConstructor
public class WorkDayController {
    private final WorkDayService workDayService;
    @PostMapping()
    public ResponseEntity<WorkDayResponseDto> postWorkDays(@RequestBody WorkDayRequestDto workDayList) {
        try{
            return new ResponseEntity<>(workDayService.saveWorkDays(workDayList), HttpStatus.OK);
        }
        catch (Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<List<WorkDayResponseDto>> getWorkDays(@RequestParam(name = "month") int month, @RequestParam(name = "year") int year) {
        try{
            return new ResponseEntity<>(workDayService.getWorkDays(month,year), HttpStatus.OK);
        }
        catch (Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}

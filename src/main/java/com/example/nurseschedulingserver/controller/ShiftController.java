package com.example.nurseschedulingserver.controller;

import com.example.nurseschedulingserver.dto.shift.ExchangeShiftDto;
import com.example.nurseschedulingserver.dto.shift.ShiftDto;
import com.example.nurseschedulingserver.service.interfaces.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shifts")
@RequiredArgsConstructor
public class ShiftController {
    private final ShiftService shiftService;

    @GetMapping("/{id}")
    public ResponseEntity<ShiftDto> getShiftById(@PathVariable(value = "id") String id) {
        try {
            return new ResponseEntity<>(shiftService.getShiftById(id), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("/exchange")
    public ResponseEntity<ExchangeShiftDto> getShiftById(@RequestBody ExchangeShiftDto exchangeShiftDto) {
        try {
            return new ResponseEntity<>(shiftService.exchangeShifts(exchangeShiftDto), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(ExchangeShiftDto.buildForError(e.getMessage()) ,HttpStatus.BAD_REQUEST);
        }
    }


}

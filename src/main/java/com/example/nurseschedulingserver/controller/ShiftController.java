package com.example.nurseschedulingserver.controller;

import com.example.nurseschedulingserver.dto.shift.ExchangeShiftDto;
import com.example.nurseschedulingserver.dto.shift.ShiftDto;
import com.example.nurseschedulingserver.service.interfaces.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @PreAuthorize("hasAuthority('CHARGE')")
    public ResponseEntity<ExchangeShiftDto> patchShiftById(@RequestBody ExchangeShiftDto exchangeShiftDto) {
        try {
            return new ResponseEntity<>(shiftService.exchangeShifts(exchangeShiftDto), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(ExchangeShiftDto.buildForError(e.getMessage()) ,HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CHARGE')")
    public ResponseEntity<List<ShiftDto>> getShifts(@RequestParam(value = "month", required = false) String month, @RequestParam(value = "year", required = false) String year) {
        try {
            return new ResponseEntity<>(shiftService.getShifts(month,year), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/my-shifts")
    public ResponseEntity<ShiftDto> getLoggedInUserShiftsByDate(@RequestParam(name = "date") String date) {
        try {
            return new ResponseEntity<>(shiftService.getLoggedInUserShiftsByDate(date), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping("/{id}/{month}/{year}")
    public ResponseEntity<List<ShiftDto>> getShiftsByMonthAndYear(@PathVariable(name = "id") String id ,@PathVariable(name = "month") String month, @PathVariable(name = "year") String year) {
        try {
            return new ResponseEntity<>(shiftService.getShiftsByMonthAndYear(id,month,year), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
   //refactor
    @GetMapping("/other-shifts")
    public ResponseEntity<List<ShiftDto>> getNotLoggedInUsersShiftsByDate(@RequestParam(value = "date") String date) {
        try {
            return new ResponseEntity<>(shiftService.getNotLoggedInUsersShiftsByDate(date), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


}

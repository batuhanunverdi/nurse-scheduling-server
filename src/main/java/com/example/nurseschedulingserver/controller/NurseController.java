package com.example.nurseschedulingserver.controller;

import com.example.nurseschedulingserver.dto.nurse.NurseDto;
import com.example.nurseschedulingserver.dto.shift.ShiftDto;
import com.example.nurseschedulingserver.service.interfaces.NurseService;
import com.example.nurseschedulingserver.service.interfaces.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/nurses")
@RequiredArgsConstructor
public class NurseController {
    private final NurseService nurseService;
    private final ShiftService shiftService;

    @PreAuthorize("hasAuthority('CHARGE')")
    @GetMapping
    public ResponseEntity<Page<NurseDto>> getNurses(@RequestParam(value = "department") String department, Pageable pageable) {
        try {
            return new ResponseEntity<>(nurseService.getNurses(department,pageable), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasAuthority('CHARGE')")
    @GetMapping("/listNurses")
    public ResponseEntity<List<NurseDto>> getNursesList(@RequestParam(value = "department") String department) {
        try {
            return new ResponseEntity<>(nurseService.getNursesList(department), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasAuthority('CHARGE')")
    @GetMapping("/{id}")
    public ResponseEntity<NurseDto> getNurseById(@PathVariable(value = "id") String id) {
        try {
            return new ResponseEntity<>(nurseService.getNurseById(id), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}/shifts")
    public ResponseEntity<List<ShiftDto>> getShiftsByNurseId(
            @PathVariable(value = "id") String id,
            @RequestParam(value = "month") String month,
            @RequestParam(value = "year") String year) {
        try {
            return new ResponseEntity<>(shiftService.getShiftsByNurseId(id, month, year), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}

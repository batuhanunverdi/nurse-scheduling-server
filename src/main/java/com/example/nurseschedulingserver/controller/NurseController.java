package com.example.nurseschedulingserver.controller;

import com.example.nurseschedulingserver.dto.nurse.NurseDto;
import com.example.nurseschedulingserver.service.interfaces.NurseService;
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

}

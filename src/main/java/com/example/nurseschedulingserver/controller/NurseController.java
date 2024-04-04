package com.example.nurseschedulingserver.controller;

import com.example.nurseschedulingserver.dto.nurse.NurseDto;
import com.example.nurseschedulingserver.service.interfaces.NurseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/nurses")
@RequiredArgsConstructor
public class NurseController {
    private final NurseService nurseService;

    @PreAuthorize("hasAuthority('CHARGE')")
    @GetMapping
    public ResponseEntity<Page<NurseDto>> getNurses(Pageable pageable) {
        try {
            return new ResponseEntity<>(nurseService.getNurses(pageable), HttpStatus.OK);
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

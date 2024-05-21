package com.example.nurseschedulingserver.controller;

import com.example.nurseschedulingserver.dto.constraint.ConstraintResponseDto;
import com.example.nurseschedulingserver.entity.constraint.Constraint;
import com.example.nurseschedulingserver.service.interfaces.ConstraintService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/constraint")
@RequiredArgsConstructor
public class ConstraintController {
    private final ConstraintService constraintService;

    @PreAuthorize("hasAuthority('CHARGE')")
    @GetMapping
    public ResponseEntity<Constraint> getConstraint(@RequestParam(value = "department") String department) {
        return new ResponseEntity<>(constraintService.getConstraintByDepartmentName(department), HttpStatus.OK);
    }
    @PostMapping
    @PreAuthorize("hasAuthority('CHARGE')")
    public ResponseEntity<ConstraintResponseDto> createConstraint(@RequestParam(value = "department") String department,@RequestBody List<Integer> minimumNurseList) throws Exception {
        try{
            return new ResponseEntity<>(constraintService.createConstraint(department, minimumNurseList), HttpStatus.OK);
        }
        catch (Exception e){
            return new ResponseEntity<>(new ConstraintResponseDto(e.getMessage()),HttpStatus.BAD_REQUEST);
        }
    }
}

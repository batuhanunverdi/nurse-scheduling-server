package com.example.nurseschedulingserver.controller;

import com.example.nurseschedulingserver.entity.constraint.Constraint;
import com.example.nurseschedulingserver.service.interfaces.ConstraintService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/constraint")
@RequiredArgsConstructor
public class ConstraintController {
    private final ConstraintService constraintService;

    @PreAuthorize("hasAuthority('CHARGE')")
    @GetMapping
    public ResponseEntity<Constraint> getConstraint(@RequestParam(value = "department") String department) {
        return new ResponseEntity<>(constraintService.getConstraintByDepartmentId(department), HttpStatus.OK);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('CHARGE')")
    public ResponseEntity<String> updateConstraint(@RequestParam(value = "department") String department, @RequestBody Constraint constraint) {
        return new ResponseEntity<>(constraintService.updateConstraintByDepartmentId(department, constraint.getMinimumNursesForEachShift()), HttpStatus.OK);
    }
    @PostMapping
    @PreAuthorize("hasAuthority('CHARGE')")
    public ResponseEntity<String> createConstraint(@RequestParam(value = "department") String department, @RequestBody Constraint constraint) {
        return new ResponseEntity<>(constraintService.createConstraint(department, constraint.getMinimumNursesForEachShift()), HttpStatus.OK);
    }
}

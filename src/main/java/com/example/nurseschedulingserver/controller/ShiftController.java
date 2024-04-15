package com.example.nurseschedulingserver.controller;


import com.example.nurseschedulingserver.dto.shift.ShiftDto;
import com.example.nurseschedulingserver.service.interfaces.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/nurses")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService shiftService;
    @GetMapping("/{id}/shifts")
    public ResponseEntity<Page<ShiftDto>> getShiftsByNurseId(@PathVariable(value = "id") String id,Pageable pageable) {
        try {
            return new ResponseEntity<>(shiftService.getShiftsByNurseId(id,pageable), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}

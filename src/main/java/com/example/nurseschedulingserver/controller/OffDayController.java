package com.example.nurseschedulingserver.controller;

import com.example.nurseschedulingserver.dto.offday.OffDayResponseDto;
import com.example.nurseschedulingserver.dto.offday.OffDayUpdateDto;
import com.example.nurseschedulingserver.service.interfaces.OffDayService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/off-days")
@RestController
@RequiredArgsConstructor
public class OffDayController {
    private final OffDayService offDayService;

    @GetMapping
    @PreAuthorize("hasAuthority('CHARGE')")
    public ResponseEntity<Page<OffDayResponseDto>> getOffDays(Pageable pageable, @RequestParam(value = "status") String status) {
        try{
            return new ResponseEntity<>(offDayService.getOffDaysAsPagination(pageable,status), HttpStatus.OK);
        }
        catch (Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('CHARGE')")
    public ResponseEntity<OffDayUpdateDto> updateStatus(@PathVariable String id, @RequestParam(value = "status") String status) {
        try{
            return new ResponseEntity<>(offDayService.updateStatus(id,status), HttpStatus.OK);
        }
        catch (Exception e){
            return new ResponseEntity<>(OffDayUpdateDto.buildForError(e.getMessage()),HttpStatus.BAD_REQUEST);
        }
    }
}

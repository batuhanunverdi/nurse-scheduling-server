package com.example.nurseschedulingserver.controller;

import com.example.nurseschedulingserver.dto.shift.ExchangeShiftRequestDto;
import com.example.nurseschedulingserver.service.interfaces.ExchangeShiftRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exchange-shift-requests")
public class ExchangeShiftRequestController {
    private final ExchangeShiftRequestService exchangeShiftRequestService;

    @GetMapping("/my-requests")
    public ResponseEntity<List<ExchangeShiftRequestDto>> getMyExchangeShiftRequests() {
        try {
            return new ResponseEntity<>(exchangeShiftRequestService.getAllLoggedInUserExchangeShiftRequests(), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("{id}/accept")
    public ResponseEntity<String> acceptExchangeShiftRequest(@PathVariable String id) {
        try {
            return new ResponseEntity<>(exchangeShiftRequestService.acceptExchangeShiftRequest(id), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PatchMapping("{id}/reject")
    public ResponseEntity<String> rejectExchangeShiftRequest(@PathVariable String id) {
        try {
            return new ResponseEntity<>(exchangeShiftRequestService.rejectExchangeShiftRequest(id), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

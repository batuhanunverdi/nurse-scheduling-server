package com.example.nurseschedulingserver.controller;

import com.example.nurseschedulingserver.dto.auth.AuthRequestDto;
import com.example.nurseschedulingserver.dto.auth.AuthResponseDto;
import com.example.nurseschedulingserver.service.interfaces.NurseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final NurseService nurseService;

    @PostMapping("/charge-nurse/login")
    public ResponseEntity<AuthResponseDto> chargeLogin(@RequestBody AuthRequestDto authRequestDto) {
        try{
            return new ResponseEntity<>(nurseService.getChargeNurse(authRequestDto), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(AuthResponseDto.buildForError("Bir Hata Oluştu."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @PostMapping("/nurse/login")
    public ResponseEntity<AuthResponseDto> nurseLogin(@RequestBody AuthRequestDto authRequestDto) {
        try{
            return new ResponseEntity<>(nurseService.getNurse(authRequestDto), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(AuthResponseDto.buildForError("Bir Hata Oluştu."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

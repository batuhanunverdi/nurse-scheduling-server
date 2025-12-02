package com.example.nurseschedulingserver.controller;

import com.example.nurseschedulingserver.dto.auth.AuthRequestDto;
import com.example.nurseschedulingserver.dto.auth.AuthResponseDto;
import com.example.nurseschedulingserver.security.JwtTokenProvider;
import com.example.nurseschedulingserver.service.interfaces.NurseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final NurseService nurseService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/charge-nurse/login")
    public ResponseEntity<AuthResponseDto> chargeLogin(@RequestBody AuthRequestDto authRequestDto) {
        try{
            AuthResponseDto response = nurseService.getChargeNurse(authRequestDto);
            if(response.getErrorMessage() != null){
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
            response.setToken(jwtTokenProvider.generateToken(authRequestDto.getTcKimlikNo(), response.getRole()));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(AuthResponseDto.buildForError("Bir Hata Olustu."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/nurse/login")
    public ResponseEntity<AuthResponseDto> nurseLogin(@RequestBody AuthRequestDto authRequestDto) {
        try{
            AuthResponseDto response = nurseService.getNurse(authRequestDto);
            if(response.getErrorMessage() != null){
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
            response.setToken(jwtTokenProvider.generateToken(authRequestDto.getTcKimlikNo(), response.getRole()));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(AuthResponseDto.buildForError("Bir Hata Olustu."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

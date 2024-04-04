package com.example.nurseschedulingserver.service.interfaces;

import com.example.nurseschedulingserver.dto.auth.AuthRequestDto;
import com.example.nurseschedulingserver.dto.auth.AuthResponseDto;
import com.example.nurseschedulingserver.dto.nurse.NurseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NurseService {
    AuthResponseDto getChargeNurse(AuthRequestDto payload);
    AuthResponseDto getNurse(AuthRequestDto authRequestDto);
    Page<NurseDto> getNurses(Pageable pageable);

    NurseDto getNurseById(String id);
}

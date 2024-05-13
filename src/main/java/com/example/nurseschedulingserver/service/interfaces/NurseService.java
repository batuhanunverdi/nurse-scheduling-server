package com.example.nurseschedulingserver.service.interfaces;

import com.example.nurseschedulingserver.dto.auth.AuthProjection;
import com.example.nurseschedulingserver.dto.auth.AuthRequestDto;
import com.example.nurseschedulingserver.dto.auth.AuthResponseDto;
import com.example.nurseschedulingserver.dto.nurse.NurseDto;
import com.example.nurseschedulingserver.entity.nurse.Nurse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NurseService {
    AuthResponseDto getChargeNurse(AuthRequestDto payload);
    AuthResponseDto getNurse(AuthRequestDto authRequestDto);
    Page<NurseDto> getNurses(String department,Pageable pageable);

    NurseDto getNurseById(String id);

    List<NurseDto> getNursesList(String department);

    AuthProjection getLoggedInUser();

    List<Nurse> getNursesByDepartment(String departmentId);
}

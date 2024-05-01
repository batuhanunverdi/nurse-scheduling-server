package com.example.nurseschedulingserver.service.implementations;

import com.example.nurseschedulingserver.dto.auth.AuthProjection;
import com.example.nurseschedulingserver.dto.auth.AuthRequestDto;
import com.example.nurseschedulingserver.dto.auth.AuthResponseDto;
import com.example.nurseschedulingserver.dto.nurse.NurseDto;
import com.example.nurseschedulingserver.enums.Role;
import com.example.nurseschedulingserver.repository.NurseRepository;
import com.example.nurseschedulingserver.service.interfaces.NurseService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NurseServiceImpl implements NurseService {
    private final NurseRepository nurseRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper = new ModelMapper();
    public AuthResponseDto authenticateNurse(AuthRequestDto payload, boolean checkRole) {
        Optional<AuthProjection> nurseOptional = nurseRepository.findNurseByTcKimlikNo(payload.getTcKimlikNo());

        if (nurseOptional.isEmpty()) {
            return AuthResponseDto.buildForError("Kullanıcı Bulunamadı.");
        }

        AuthProjection nurse = nurseOptional.get();

        if (!passwordEncoder.matches(payload.getPassword(), nurse.getPassword())) {
            return AuthResponseDto.buildForError("Şifre Hatalı.");
        }

        if (checkRole && !nurse.getRole().equals(Role.CHARGE.toString())) {
            return AuthResponseDto.buildForError("Erişim için yetkiniz bulunamamaktadır.");
        }

        return convertToDto(nurse);
    }

    @Override
    public AuthResponseDto getChargeNurse(AuthRequestDto payload) {
        return authenticateNurse(payload, true);
    }
    @Override
    public AuthResponseDto getNurse(AuthRequestDto authRequestDto){
        return authenticateNurse(authRequestDto, false);
    }


    @Override
    public Page<NurseDto> getNurses(String department,Pageable pageable) {
        return nurseRepository.findAllNursesByDepartment(department,pageable);
    }

    @Override
    public NurseDto getNurseById(String id) {
        return nurseRepository.findNurseById(id).orElseThrow(() -> new RuntimeException("Nurse not found"));
    }

    private AuthResponseDto convertToDto(AuthProjection authProjection) {
        return modelMapper.map(authProjection, AuthResponseDto.class);
    }

    public List<NurseDto> getNursesList(String department){
        return nurseRepository.findAllNursesByDepartmentList(department);
    }

    public AuthProjection getLoggedInUser() {
        return nurseRepository.findNurseByTcKimlikNo(SecurityContextHolder
                .getContext()
                        .getAuthentication()
                        .getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}

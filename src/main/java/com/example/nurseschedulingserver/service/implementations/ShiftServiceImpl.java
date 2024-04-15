package com.example.nurseschedulingserver.service.implementations;

import com.example.nurseschedulingserver.dto.shift.ShiftDto;
import com.example.nurseschedulingserver.repository.ShiftRepository;
import com.example.nurseschedulingserver.service.interfaces.ShiftService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShiftServiceImpl implements ShiftService {
    private final ShiftRepository shiftRepository;
    private final NurseServiceImpl nurseService;

    private final ModelMapper modelMapper = new ModelMapper();
    @Override
    public Page<ShiftDto> getShiftsByNurseId(String nurseId, Pageable pageable) {

        nurseService.getNurseById(nurseId);
        return shiftRepository.findShiftsByNurseId(nurseId, pageable);
    }
}

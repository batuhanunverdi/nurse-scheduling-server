package com.example.nurseschedulingserver.service.implementations;

import com.example.nurseschedulingserver.dto.shift.ShiftDto;
import com.example.nurseschedulingserver.repository.ShiftRepository;
import com.example.nurseschedulingserver.service.interfaces.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShiftServiceImpl implements ShiftService {
    private final ShiftRepository shiftRepository;
    private final NurseServiceImpl nurseService;

    @Override
    public List<ShiftDto> getShiftsByNurseId(String nurseId, String month, String year) {
        nurseService.getNurseById(nurseId);
        return shiftRepository.findShiftsByNurseId(nurseId, month, year);
    }
}

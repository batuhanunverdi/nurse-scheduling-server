package com.example.nurseschedulingserver.service.implementations;

import com.example.nurseschedulingserver.dto.auth.AuthProjection;
import com.example.nurseschedulingserver.dto.shift.ExchangeShiftDto;
import com.example.nurseschedulingserver.dto.shift.ShiftDto;
import com.example.nurseschedulingserver.entity.shift.Shift;
import com.example.nurseschedulingserver.repository.ShiftRepository;
import com.example.nurseschedulingserver.service.interfaces.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShiftServiceImpl implements ShiftService {
    private final ShiftRepository shiftRepository;
    private final NurseServiceImpl nurseService;

    @Override
    public List<ShiftDto> getShiftsByNurseId(String nurseId) {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        nurseService.getNurseById(nurseId);
        return shiftRepository.findShiftsByNurseId(nurseId, month, year);
    }

    public ShiftDto getShiftById(String id) {
        return shiftRepository.findShiftDtoById(id).orElseThrow(() -> new RuntimeException("Shift not found"));
    }

    @Override
    public ExchangeShiftDto exchangeShifts(ExchangeShiftDto exchangeShiftDto) {
        Shift shift1 = shiftRepository.findById(exchangeShiftDto.getFirstShiftId()).orElseThrow(() -> new RuntimeException("Shift not found"));
        Shift shift2 = shiftRepository.findById(exchangeShiftDto.getSecondShiftId()).orElseThrow(() -> new RuntimeException("Shift not found"));

        String tempNurseId = shift1.getNurseId();
        shift1.setNurseId(shift2.getNurseId());
        shift2.setNurseId(tempNurseId);

        shiftRepository.save(shift1);
        shiftRepository.save(shift2);

        return exchangeShiftDto;
    }

    @Override
    public List<ShiftDto> getShifts() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        AuthProjection user = nurseService.getLoggedInUser();
        return shiftRepository.findAllShiftsByMothAndYear(month,year,user.getDepartmentName());

    }

    @Override
    public ShiftDto getLoggedInUserShifts(String date) {
        AuthProjection user = nurseService.getLoggedInUser();
        return shiftRepository.findShiftsByNurseIdAndDate(user.getId(),date);
    }

    @Override
    public Shift getShiftEntityById(String id) {
        return shiftRepository.findById(id).orElseThrow(() -> new RuntimeException("Shift not found"));
    }

    @Override
    public Shift saveShift(Shift shift) {
        return shiftRepository.save(shift);
    }

    public List<ShiftDto> getNotLoggedInUsersShiftsByDate(String date) {
        AuthProjection user = nurseService.getLoggedInUser();
        return shiftRepository.findAllShiftsByDate(date, user.getId(),user.getDepartmentName());
    }


}

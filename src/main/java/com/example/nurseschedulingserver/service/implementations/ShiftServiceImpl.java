package com.example.nurseschedulingserver.service.implementations;

import com.example.nurseschedulingserver.dto.auth.AuthProjection;
import com.example.nurseschedulingserver.dto.nurse.NurseDto;
import com.example.nurseschedulingserver.dto.shift.ExchangeShiftDto;
import com.example.nurseschedulingserver.dto.shift.ShiftDto;
import com.example.nurseschedulingserver.entity.shift.Shift;
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
    public List<ShiftDto> getShiftsByNurseId(String nurseId,String month,String year) {
        int monthInt = Integer.parseInt(month)+1;
        int yearInt = Integer.parseInt(year);
        nurseService.getNurseById(nurseId);
        return shiftRepository.findShiftsByNurseId(nurseId, monthInt, yearInt);
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
    public List<ShiftDto> getShifts(String month, String year) {
        int monthInt = Integer.parseInt(month)+1;
        int yearInt = Integer.parseInt(year);
        AuthProjection user = nurseService.getLoggedInUser();
        return shiftRepository.findAllShiftsByMothAndYear(monthInt,yearInt,user.getDepartmentName());

    }

    @Override
    public ShiftDto getLoggedInUserShiftsByDate(String date) {
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

    @Override
    public List<ShiftDto> getShiftsByMonthAndYear(String id,String month, String year) {
        NurseDto nurse = nurseService.getNurseById(id);
        int monthInt = Integer.parseInt(month);
        int yearInt = Integer.parseInt(year);
        return shiftRepository.findShiftsByNurseIdAndMonthAndYearAndDepartmentName(nurse.getId(), monthInt,yearInt,nurse.getDepartmentName());
    }

    public List<ShiftDto> getNotLoggedInUsersShiftsByDate(String date) {
        AuthProjection user = nurseService.getLoggedInUser();
        return shiftRepository.findAllShiftsByDate(date, user.getId(),user.getDepartmentName());
    }


}

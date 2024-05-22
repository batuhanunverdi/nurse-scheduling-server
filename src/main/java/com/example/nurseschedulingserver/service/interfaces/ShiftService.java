package com.example.nurseschedulingserver.service.interfaces;

import com.example.nurseschedulingserver.dto.shift.ExchangeShiftDto;
import com.example.nurseschedulingserver.dto.shift.ShiftDto;
import com.example.nurseschedulingserver.entity.shift.Shift;


import java.util.List;

public interface ShiftService {
    ShiftDto getShiftById(String id);

    ExchangeShiftDto exchangeShifts(ExchangeShiftDto exchangeShiftDto);

    List<ShiftDto> getShifts(String month, String year);

    List<ShiftDto> getNotLoggedInUsersShiftsByDate(String date);

    ShiftDto getLoggedInUserShiftsByDate(String date);

    Shift getShiftEntityById(String id);

    Shift saveShift(Shift shift);

    List<ShiftDto> getShiftsByMonthAndYear(String id, String month, String year);

    List<Shift> saveAll(List<Shift> shifts);

    List<ShiftDto> getAvailableShiftsByShiftId(String shiftId,String nurseId, String month, String year);
    List<ShiftDto> getShiftsByDepartmentAndDate(String departmentName,int month,int year ,int days);
}

package com.example.nurseschedulingserver.service.interfaces;

import com.example.nurseschedulingserver.dto.shift.ExchangeShiftDto;
import com.example.nurseschedulingserver.dto.shift.ShiftDto;


import java.util.List;

public interface ShiftService {
    List<ShiftDto> getShiftsByNurseId(String nurseId, String month, String year);

    ShiftDto getShiftById(String id);

    ExchangeShiftDto exchangeShifts(ExchangeShiftDto exchangeShiftDto);
}

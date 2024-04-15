package com.example.nurseschedulingserver.service.interfaces;

import com.example.nurseschedulingserver.dto.shift.ShiftDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ShiftService {
    Page<ShiftDto> getShiftsByNurseId(String nurseId, Pageable pageable);

}

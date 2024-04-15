package com.example.nurseschedulingserver.repository;

import com.example.nurseschedulingserver.dto.shift.ShiftDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface ShiftRepository {

    Page<ShiftDto> findShiftsByNurseId(String nurseId, Pageable pageable);

}

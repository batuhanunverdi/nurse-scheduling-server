package com.example.nurseschedulingserver.service.interfaces;

import com.example.nurseschedulingserver.dto.offday.OffDayResponseDto;
import com.example.nurseschedulingserver.dto.offday.OffDayUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OffDayService {
    Page<OffDayResponseDto> getOffDaysAsPagination(Pageable pageable,String status);

    OffDayUpdateDto updateStatus(String id, String status) throws Exception;
}

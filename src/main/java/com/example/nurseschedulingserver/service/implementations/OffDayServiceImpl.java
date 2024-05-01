package com.example.nurseschedulingserver.service.implementations;

import com.example.nurseschedulingserver.dto.offday.OffDayResponseDto;
import com.example.nurseschedulingserver.dto.offday.OffDayUpdateDto;
import com.example.nurseschedulingserver.entity.offday.OffDay;
import com.example.nurseschedulingserver.enums.RequestStatus;
import com.example.nurseschedulingserver.repository.OffDayRepository;
import com.example.nurseschedulingserver.service.interfaces.OffDayService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OffDayServiceImpl implements OffDayService {
    private final OffDayRepository offDayRepository;
    @Override
    public Page<OffDayResponseDto> getOffDaysAsPagination(Pageable pageable,String status) {
        return offDayRepository.findAllWithinPageAndStatus(status,pageable);
    }

    @Override
    public OffDayUpdateDto updateStatus(String id, String status) throws Exception {
        Optional<OffDay> offDay = offDayRepository.findById(id);
        if(offDay.isEmpty()){
           throw new Exception("Off day not found");
        }
        offDay.get().setStatus(RequestStatus.valueOf(status));
        offDayRepository.save(offDay.get());
        return new OffDayUpdateDto(offDay.get().getId(),offDay.get().getStatus().name(),null);
    }
}

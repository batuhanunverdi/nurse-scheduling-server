package com.example.nurseschedulingserver.service.implementations;

import com.example.nurseschedulingserver.dto.auth.AuthProjection;
import com.example.nurseschedulingserver.dto.shift.ExchangeShiftRequestDto;
import com.example.nurseschedulingserver.entity.shift.ExchangeShiftRequest;
import com.example.nurseschedulingserver.entity.shift.Shift;
import com.example.nurseschedulingserver.enums.RequestStatus;
import com.example.nurseschedulingserver.repository.ExchangeShiftRequestRepository;
import com.example.nurseschedulingserver.service.interfaces.ExchangeShiftRequestService;
import com.example.nurseschedulingserver.service.interfaces.NurseService;
import com.example.nurseschedulingserver.service.interfaces.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExchangeShiftRequestServiceImpl implements ExchangeShiftRequestService {
    private final ExchangeShiftRequestRepository exchangeShiftRequestRepository;
    private final NurseService nurseService;
    private final ShiftService shiftService;
    @Override
    public List<ExchangeShiftRequestDto> getAllLoggedInUserExchangeShiftRequests() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        AuthProjection authProjection = nurseService.getLoggedInUser();
        return exchangeShiftRequestRepository.findAllByRequestedShiftId(authProjection.getId(), month, year);
    }

    @Override
    public String acceptExchangeShiftRequest(String id) {
        ExchangeShiftRequest exchangeShiftRequest = exchangeShiftRequestRepository.findById(id).orElseThrow(() -> new RuntimeException("Exchange shift request not found"));
        Shift requestedShift = shiftService.getShiftEntityById(exchangeShiftRequest.getRequestedShiftId());
        Shift requesterShift  = shiftService.getShiftEntityById(exchangeShiftRequest.getRequesterShiftId());
        exchangeShiftRequest.setStatus(RequestStatus.ACCEPTED);
        String requestedShiftNurseId = requestedShift.getNurseId();
        requestedShift.setNurseId(requesterShift.getNurseId());
        requesterShift.setNurseId(requestedShiftNurseId);
        shiftService.saveShift(requestedShift);
        shiftService.saveShift(requesterShift);
        exchangeShiftRequestRepository.save(exchangeShiftRequest);
        return exchangeShiftRequest.getStatus().name();
    }

    @Override
    public String rejectExchangeShiftRequest(String id) {
        ExchangeShiftRequest exchangeShiftRequest = exchangeShiftRequestRepository.findById(id).orElseThrow(() -> new RuntimeException("Exchange shift request not found"));
        exchangeShiftRequest.setStatus(RequestStatus.REJECTED);
        exchangeShiftRequestRepository.save(exchangeShiftRequest);
        return exchangeShiftRequest.getStatus().name();
    }
    
}

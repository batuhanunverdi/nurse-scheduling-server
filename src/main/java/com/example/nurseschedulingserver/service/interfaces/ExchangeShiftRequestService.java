package com.example.nurseschedulingserver.service.interfaces;

import com.example.nurseschedulingserver.dto.shift.ExchangeShiftRequestDto;

import java.util.List;

public interface ExchangeShiftRequestService {

    List<ExchangeShiftRequestDto> getAllLoggedInUserExchangeShiftRequests();

    String  acceptExchangeShiftRequest(String id);

    String rejectExchangeShiftRequest(String id);


}

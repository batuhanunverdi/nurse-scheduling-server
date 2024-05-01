package com.example.nurseschedulingserver.dto.shift;

public interface ExchangeShiftRequestDto {
    String getId();
    String getRequesterShiftId();
    String getRequesterFullName();
    String getRequesterShiftStartDate();
    String getRequesterShiftEndDate();
    String getRequestedShiftId();
    String getRequestedFullName();
    String getRequestedShiftStartDate();
    String getRequestedShiftEndDate();
    String getStatus();
}

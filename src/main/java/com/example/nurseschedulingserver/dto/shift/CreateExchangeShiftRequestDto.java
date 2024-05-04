package com.example.nurseschedulingserver.dto.shift;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateExchangeShiftRequestDto {
    private String requesterShiftId;
    private String requestedShiftId;
}

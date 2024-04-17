package com.example.nurseschedulingserver.dto.shift;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeShiftDto {
    private String firstNurseId;
    private String secondNurseId;
    private String firstShiftId;
    private String secondShiftId;
    private String errorMessage;

    public ExchangeShiftDto(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public static ExchangeShiftDto buildForError(String errorMessage) {
        return new ExchangeShiftDto(errorMessage);
    }
}

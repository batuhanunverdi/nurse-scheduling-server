package com.example.nurseschedulingserver.dto.offday;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OffDayUpdateDto {
    private String id;
    private String status;
    private String errorMessage;

    public OffDayUpdateDto(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public static OffDayUpdateDto buildForError(String errorMessage) {
        return new OffDayUpdateDto(errorMessage);
    }
}

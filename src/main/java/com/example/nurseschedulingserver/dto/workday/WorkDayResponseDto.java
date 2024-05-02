package com.example.nurseschedulingserver.dto.workday;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkDayResponseDto {

    private String Id;
    private String date;
    private String nurseId;
    private String message;

}

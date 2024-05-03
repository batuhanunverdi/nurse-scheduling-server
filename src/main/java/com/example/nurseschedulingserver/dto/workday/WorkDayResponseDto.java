package com.example.nurseschedulingserver.dto.workday;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkDayResponseDto {
    private String Id;
    private List<Date> workDate;
    private String nurseId;
    private String message;
}

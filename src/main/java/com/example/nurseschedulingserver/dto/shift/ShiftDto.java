package com.example.nurseschedulingserver.dto.shift;

import java.util.Date;

    public interface ShiftDto {
        String getId();
        Date getStartDate();
        Date getEndDate();
        String getNurseId();
        String getNurseFirstName();
        String getNurseLastName();

    }

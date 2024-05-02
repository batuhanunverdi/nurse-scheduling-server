package com.example.nurseschedulingserver.service.implementations;

import com.example.nurseschedulingserver.dto.workday.WorkDayResponseDto;
import com.example.nurseschedulingserver.entity.workday.WorkDay;
import com.example.nurseschedulingserver.repository.WorkDayRepository;
import com.example.nurseschedulingserver.service.interfaces.WorkDayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkDayServiceImpl implements WorkDayService {
    private final WorkDayRepository workDayRepository;
    @Override
    public List<WorkDayResponseDto> saveWorkDays(List<WorkDay> workDays) {
        List<WorkDay> savedWorkDays = workDayRepository.saveAll(workDays);
        String message = savedWorkDays.isEmpty() ? "Workday preference saved successfully." : "Failed to save workday preference.";

        return savedWorkDays.stream()
                .map(workDay -> convertToDto(workDay, message))
                .collect(Collectors.toList());
    }

    private WorkDayResponseDto convertToDto(WorkDay workDay, String message) {
        WorkDayResponseDto dto = new WorkDayResponseDto();
        dto.setId(workDay.getId());
        dto.setNurseId(workDay.getNurseId());
        dto.setDate(workDay.getWorkDate());
        dto.setMessage(message);
        return dto;
    }
}

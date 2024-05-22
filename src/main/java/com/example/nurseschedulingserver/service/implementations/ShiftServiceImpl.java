package com.example.nurseschedulingserver.service.implementations;

import com.example.nurseschedulingserver.dto.auth.AuthProjection;
import com.example.nurseschedulingserver.dto.nurse.NurseDto;
import com.example.nurseschedulingserver.dto.shift.ExchangeShiftDto;
import com.example.nurseschedulingserver.dto.shift.ShiftDto;
import com.example.nurseschedulingserver.entity.shift.Shift;
import com.example.nurseschedulingserver.repository.ShiftRepository;
import com.example.nurseschedulingserver.service.interfaces.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShiftServiceImpl implements ShiftService {
    private final ShiftRepository shiftRepository;
    private final NurseServiceImpl nurseService;

    @Override
    public ShiftDto getShiftById(String id) {
        return shiftRepository.findShiftDtoById(id).orElseThrow(() -> new RuntimeException("Shift not found"));
    }

    @Override
    public ExchangeShiftDto exchangeShifts(ExchangeShiftDto exchangeShiftDto) {
        Shift shift1 = shiftRepository.findById(exchangeShiftDto.getFirstShiftId()).orElseThrow(() -> new RuntimeException("Shift not found"));
        Shift shift2 = shiftRepository.findById(exchangeShiftDto.getSecondShiftId()).orElseThrow(() -> new RuntimeException("Shift not found"));

        String tempNurseId = shift1.getNurseId();
        shift1.setNurseId(shift2.getNurseId());
        shift2.setNurseId(tempNurseId);

        shiftRepository.save(shift1);
        shiftRepository.save(shift2);

        return exchangeShiftDto;
    }

    @Override
    public List<ShiftDto> getShifts(String month, String year) {
        int monthInt = Integer.parseInt(month) + 1;
        int yearInt = Integer.parseInt(year);
        AuthProjection user = nurseService.getLoggedInUser();
        return shiftRepository.findAllShiftsByMothAndYear(monthInt, yearInt, user.getDepartmentName());

    }

    @Override
    public ShiftDto getLoggedInUserShiftsByDate(String date) {
        AuthProjection user = nurseService.getLoggedInUser();
        return shiftRepository.findShiftsByNurseIdAndDate(user.getId(), date);
    }

    @Override
    public Shift getShiftEntityById(String id) {
        return shiftRepository.findById(id).orElseThrow(() -> new RuntimeException("Shift not found"));
    }

    @Override
    public Shift saveShift(Shift shift) {
        return shiftRepository.save(shift);
    }

    @Override
    public List<ShiftDto> getShiftsByMonthAndYear(String id, String month, String year) {
        NurseDto nurse = nurseService.getNurseById(id);
        int monthInt = Integer.parseInt(month);
        int yearInt = Integer.parseInt(year);
        return shiftRepository.findShiftsByNurseIdAndMonthAndYearAndDepartmentName(nurse.getId(), monthInt, yearInt, nurse.getDepartmentName());
    }

    @Override
    public List<Shift> saveAll(List<Shift> shifts) {
        return shiftRepository.saveAll(shifts);
    }

    public List<ShiftDto> getAvailableShiftsByShiftId(String shiftId, String selectedNurseId, String month, String year) {
        ShiftDto shift = getShiftById(shiftId);
        long duration = (shift.getEndDate().getTime() - shift.getStartDate().getTime()) / (60 * 60 * 1000);

        List<ShiftDto> nurseShifts = getShiftsByMonthAndYear(shift.getNurseId(), month, year);
        List<ShiftDto> selectedNurseShifts = getShiftsByMonthAndYear(selectedNurseId, month, year);
        List<ShiftDto> availableShifts = new ArrayList<>();

        for (ShiftDto selectedNurseShift : selectedNurseShifts) {
            if (isConflict(selectedNurseShift, shift, duration) ) {
                return new ArrayList<>();
            }
            if(!isAvailableForSwap(selectedNurseShift, shift, duration)){
                continue;
            }
            availableShifts.add(selectedNurseShift);
        }

        List<ShiftDto> finalAvailableShifts = new ArrayList<>(availableShifts);
        for (ShiftDto nurseShift : nurseShifts) {
            finalAvailableShifts.removeIf(availableShift -> isConflict(availableShift, nurseShift, duration));
        }

        return finalAvailableShifts;
    }

    private boolean isConflict(ShiftDto shift1, ShiftDto shift2, long duration) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        String shift1StartDate = sdf.format(shift1.getStartDate());
        String shift1EndDate = sdf.format(shift1.getEndDate());
        long shift1Duration = (shift1.getEndDate().getTime() - shift1.getStartDate().getTime()) / (60 * 60 * 1000);

        String shift2StartDate = sdf.format(shift2.getStartDate());
        String shift2EndDate = sdf.format(shift2.getEndDate());

        // Same day and same duration check
        if (shift1StartDate.equals(shift2StartDate) && shift1Duration == duration) {
            return true;
        }

        // Overlapping dates check
        if (shift1StartDate.equals(shift2StartDate) ||
                shift1StartDate.equals(shift2EndDate) ||
                shift1EndDate.equals(shift2EndDate) ||
                shift1EndDate.equals(shift2StartDate)) {
            return true;
        }

        // Constraint checks
        if ((duration == 16 || duration == 24) && getPreviousDay(shift1.getStartDate(), 1).equals(shift2.getStartDate())) {
            return true;
        }
        if (duration == 24 && getPreviousDay(shift1.getStartDate(), 2).equals(shift2.getStartDate())) {
            return true;
        }
        if (duration == 16 && getNextDay(shift1.getStartDate(), 1).equals(shift2.getStartDate())) {
            return true;
        }
        return duration == 24 && (getNextDay(shift1.getStartDate(), 1).equals(shift2.getStartDate()) || getNextDay(shift1.getStartDate(), 2).equals(shift2.getStartDate()));
    }

    private boolean isAvailableForSwap(ShiftDto selectedNurseShift, ShiftDto shift, long duration) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        String selectedNurseShiftStartDate = sdf.format(selectedNurseShift.getStartDate());
        String selectedNurseShiftEndDate = sdf.format(selectedNurseShift.getEndDate());
        String shiftStartDate = sdf.format(shift.getStartDate());
        String shiftEndDate = sdf.format(shift.getEndDate());
        long selectedNurseShiftDuration = (selectedNurseShift.getEndDate().getTime() - selectedNurseShift.getStartDate().getTime()) / (60 * 60 * 1000);

        if (selectedNurseShiftStartDate.equals(shiftStartDate) && duration == selectedNurseShiftDuration) {
            return false;
        }

        if (selectedNurseShiftStartDate.equals(shiftStartDate) ||
                selectedNurseShiftStartDate.equals(shiftEndDate) ||
                selectedNurseShiftEndDate.equals(shiftEndDate) ||
                selectedNurseShiftEndDate.equals(shiftStartDate)) {
            return false;
        }

        if (getPreviousDay(shift.getStartDate(), 1).equals(selectedNurseShift.getStartDate())) {
            if (selectedNurseShiftDuration == 16 || selectedNurseShiftDuration == 24) {
                return false;
            }
        }
        if (getPreviousDay(shift.getStartDate(), 2).equals(selectedNurseShift.getStartDate())) {
            if (selectedNurseShiftDuration == 24) {
                return false;
            }
        }
        if (duration == 16) {
            if (getNextDay(shift.getStartDate(), 1).equals(selectedNurseShift.getStartDate())) {
                return false;
            }
        }
        if (duration == 24) {
            return !getNextDay(shift.getStartDate(), 2).equals(selectedNurseShift.getStartDate()) && !getNextDay(shift.getStartDate(), 1).equals(selectedNurseShift.getStartDate());
        }

        return true;
    }

    public static Date getPreviousDay(Date date, int daysBefore) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -daysBefore);
        return calendar.getTime();
    }

    public static Date getNextDay(Date date, int daysAfter) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, daysAfter);
        return calendar.getTime();
    }

    @Override
    public List<ShiftDto> getNotLoggedInUsersShiftsByDate(String date) {
        AuthProjection user = nurseService.getLoggedInUser();
        return shiftRepository.findAllShiftsByDate(date, user.getId(),user.getDepartmentName());
    }


}

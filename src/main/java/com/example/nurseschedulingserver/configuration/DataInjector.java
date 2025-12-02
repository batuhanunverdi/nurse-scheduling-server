package com.example.nurseschedulingserver.configuration;

import com.example.nurseschedulingserver.entity.department.Department;
import com.example.nurseschedulingserver.entity.nurse.Nurse;
import com.example.nurseschedulingserver.entity.offday.OffDay;
import com.example.nurseschedulingserver.entity.shift.ExchangeShiftRequest;
import com.example.nurseschedulingserver.entity.shift.Shift;
import com.example.nurseschedulingserver.entity.workday.WorkDay;
import com.example.nurseschedulingserver.enums.RequestStatus;
import com.example.nurseschedulingserver.enums.Role;
import com.example.nurseschedulingserver.repository.*;
import com.example.nurseschedulingserver.service.interfaces.CPService;
import com.example.nurseschedulingserver.service.interfaces.ConstraintService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Configuration
@RequiredArgsConstructor
public class DataInjector implements CommandLineRunner {
    private static final String DEFAULT_DEPARTMENT_NAME = "Acil Servis";
    private static final String DEFAULT_PASSWORD = "Sanane5885";
    private static final String CHARGE_TC = "99999999990";
    private static final String NURSE_TC = "99999999991";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final DepartmentRepository departmentRepository;
    private final NurseRepository nurseRepository;
    private final OffDayRepository offDayRepository;
    private final PasswordEncoder passwordEncoder;
    private final ShiftRepository shiftRepository;
    private final ExchangeShiftRequestRepository exchangeShiftRequestRepository;
    private final WorkDayRepository workDayRepository;
    private final ConstraintService constraintService;
    private final CPService cpService;

    @Override
    public void run(String... args) throws Exception {
       injectDepartments();
       injectNurse();
       injectWorkDays();
       injectConstraints();
       cpService.executeConstraint();
    }
    public void injectConstraints() throws Exception {
        departmentRepository.findAll()
                .stream()
                .map(Department::getName)
                .distinct()
                .forEach(name -> {
                    try {
                        constraintService.createConstraint(name, new ArrayList<>(Arrays.asList(3, 2, 2)));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public void injectDepartments() {
        List<String> departments = List.of(
                "Yara Bakım Birimi",
                "Acil Servis",
                "Yoğun Bakım Ünitesi",
                "Palyatif Bakım Birimi",
                "Kadın Doğum Servisi",
                "Nöroloji Departmanı",
                "Onkoloji Bölümü",
                "Ortopedi Servisi",
                "Psikiyatri Departmanı",
                "Diyabet Bakım Birimi"
        );
        for (String s : departments) {
            if (departmentRepository.existsByName(s)) {
                continue;
            }
            Department department = new Department();
            department.setName(s);
            departmentRepository.save(department);
        }
    }

    public void injectNurse() {
        Department department = departmentRepository.findFirstByName(DEFAULT_DEPARTMENT_NAME)
                .orElseGet(() -> {
                    Department dept = new Department();
                    dept.setName(DEFAULT_DEPARTMENT_NAME);
                    return departmentRepository.save(dept);
                });

        upsertNurse(CHARGE_TC, "Charge", "Nurse", Role.CHARGE, department.getId());
        upsertNurse(NURSE_TC, "Test", "Nurse", Role.NURSE, department.getId());
    }

    public void injectOffDays() {
        nurseRepository.findByTcKimlikNo(NURSE_TC).ifPresent(nurse -> {
            offDayRepository.deleteAll();
            List<OffDay> offDays = new ArrayList<>();
            LocalDate today = LocalDate.now();
            for (int i = 1; i <= 5; i++) {
                OffDay offDay = new OffDay();
                offDay.setDate(today.plusDays(i).format(DATE_FORMATTER));
                offDay.setNurseId(nurse.getId());
                offDay.setStatus(RequestStatus.PENDING);
                offDays.add(offDay);
            }
            offDayRepository.saveAll(offDays);
        });
    }

    public void injectShifts() {
        Optional<Nurse> chargeNurse = nurseRepository.findByTcKimlikNo(CHARGE_TC);
        Optional<Nurse> nurse = nurseRepository.findByTcKimlikNo(NURSE_TC);
        if (chargeNurse.isEmpty() || nurse.isEmpty()) {
            return;
        }
        shiftRepository.deleteAll();
        LocalDate today = LocalDate.now();
        List<Shift> shifts = new ArrayList<>();
        shifts.add(buildShift(chargeNurse.get(), today, 8, 16));
        shifts.add(buildShift(nurse.get(), today, 16, 24));
        shifts.add(buildShift(nurse.get(), today.plusDays(1), 8, 16));
        shiftRepository.saveAll(shifts);
    }

    public void injectExchangeShiftRequests() {
        List<Shift> shifts = shiftRepository.findAll();
        if (shifts.size() < 2) {
            return;
        }
        ExchangeShiftRequest exchangeShiftRequest = new ExchangeShiftRequest();
        exchangeShiftRequest.setRequesterShiftId(shifts.get(0).getId());
        exchangeShiftRequest.setRequestedShiftId(shifts.get(1).getId());
        exchangeShiftRequest.setStatus(RequestStatus.PENDING);
        exchangeShiftRequestRepository.save(exchangeShiftRequest);


    }

    public void injectWorkDays() {
        List<WorkDay> workDays = new ArrayList<>();
        List<Nurse> nurses = nurseRepository.findAll();
        Random random = new Random();

        int[] months = {Calendar.JUNE, Calendar.JULY, Calendar.AUGUST};

        for (int month : months) {
            List<Date> allDaysInMonth = new ArrayList<>();
            Calendar calendar = Calendar.getInstance();
            calendar.set(2024, month, 1);
            int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

            for (int day = 1; day <= daysInMonth; day++) {
                calendar.set(Calendar.DAY_OF_MONTH, day);
                allDaysInMonth.add(calendar.getTime());
            }

            for (Nurse nurse : nurses) {
                if(nurse.getRole().equals(Role.NURSE)){
                    Set<Date> selectedDates = new HashSet<>();
                    while (selectedDates.size() < daysInMonth - 3) { // Selecting random days except for 3 days
                        int randomIndex = random.nextInt(allDaysInMonth.size());
                        selectedDates.add(allDaysInMonth.get(randomIndex));
                    }

                    List<Date> datesWithMidnight = new ArrayList<>();
                    for (Date date : selectedDates) {
                        calendar.setTime(date);
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                        datesWithMidnight.add(calendar.getTime());
                    }

                    WorkDay workDay = new WorkDay();
                    workDay.setWorkDate(datesWithMidnight);
                    workDay.setNurseId(nurse.getId());
                    workDays.add(workDay);
                }
            }
        }

        workDayRepository.saveAll(workDays);
    }

    private void upsertNurse(String tcKimlikNo, String firstName, String lastName, Role role, String departmentId) {
        Nurse nurse = nurseRepository.findByTcKimlikNo(tcKimlikNo).orElseGet(Nurse::new);
        nurse.setFirstName(firstName);
        nurse.setLastName(lastName);
        nurse.setTcKimlikNo(tcKimlikNo);
        nurse.setPhoneNumber("0532 123 45 67");
        nurse.setDepartmentId(departmentId);
        nurse.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        nurse.setRole(role);
        nurse.setGender("Kadın");
        nurse.setBirthDate("01.01.1990");
        nurseRepository.save(nurse);
    }

    private Shift buildShift(Nurse nurse, LocalDate day, int startHour, int endHour) {
        Shift shift = new Shift();
        shift.setNurseId(nurse.getId());
        shift.setStartDate(toDate(day, startHour));
        LocalDate endDay = endHour >= 24 ? day.plusDays(1) : day;
        int normalizedEnd = endHour >= 24 ? endHour - 24 : endHour;
        shift.setEndDate(toDate(endDay, normalizedEnd));
        return shift;
    }

    private Date toDate(LocalDate day, int hour) {
        return Date.from(day.atTime(hour, 0).atZone(ZoneId.systemDefault()).toInstant());
    }
}

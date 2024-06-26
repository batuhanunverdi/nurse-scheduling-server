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

import java.util.*;

@Configuration
@RequiredArgsConstructor
public class DataInjector implements CommandLineRunner {
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
//        injectDepartments();
//        injectNurse();
//        injectWorkDays();
//        injectConstraints();
//        cpService.executeConstraint();
    }
    public void injectConstraints() throws Exception {
        List<Department> departments = departmentRepository.findAll();
        for (Department department : departments) {
            constraintService.createConstraint(department.getName(), new ArrayList<>(Arrays.asList(3, 2, 2)));
        }
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
            Department department = new Department();
            department.setName(s);
            departmentRepository.save(department);
        }
    }

    public void injectNurse() {
        List<Department> departments = departmentRepository.findAll();
        List<Nurse> nurses = new ArrayList<>();

        for (Department department : departments) {
            String departmentId = department.getId();
            String defaultPassword = "Sanane5885";
            Random random = new Random();
            for (int i = 0; i < 12; i++) {
                long randomNumber = (long) (random.nextDouble() * 1_000_000_000_00L);
                String tcKimlikNo = String.valueOf(randomNumber);
                Nurse nurse2 = new Nurse();
                nurse2.setFirstName(department.getName() + (i));
                nurse2.setLastName("");
                nurse2.setTcKimlikNo(tcKimlikNo);
                nurse2.setPhoneNumber("0532 123 45 67");
                nurse2.setDepartmentId(departmentId);
                nurse2.setPassword(passwordEncoder.encode(defaultPassword));
                if(i==0){
                    nurse2.setRole(Role.CHARGE);
                }
                else {
                    nurse2.setRole(Role.NURSE);
                }
                nurse2.setGender(i % 2 == 0 ? "Erkek" : "Kadın");
                nurse2.setBirthDate("09.06.1998");
                nurses.add(nurse2);
            }
        }
        nurseRepository.saveAll(nurses);
    }

    public void injectOffDays() {
        List<Nurse> nurses = new ArrayList<>(nurseRepository.findAll().stream().filter(nurse -> nurse.getDepartmentId().equals("8cbb9908-5f44-4aa1-bd94-adf4cf6ce51d")).toList());
        nurses.removeIf(nurse -> nurse.getRole().equals(Role.CHARGE));
        List<String> dates = new ArrayList<>();
        dates.add("08.06.2024");
        dates.add("09.06.2024");
        dates.add("10.06.2024");
        dates.add("11.06.2024");
        dates.add("12.06.2024");
        dates.add("13.06.2024");
        dates.add("14.06.2024");
        dates.add("15.06.2024");
        dates.add("16.06.2024");
        dates.add("17.06.2024");
        dates.add("18.06.2024");
        dates.add("19.06.2024");
        dates.add("20.06.2024");
        dates.add("21.06.2024");
        dates.add("22.06.2024");
        List<OffDay> offDays = new ArrayList<>();

        for (int i = 0; i < 15; i++) {
            OffDay offDay = new OffDay();
            offDay.setDate(dates.get(i));
            offDay.setNurseId(nurses.get(new Random().nextInt(nurses.size())).getId());
            offDay.setStatus(RequestStatus.PENDING);
            offDays.add(offDay);
        }
        offDayRepository.saveAll(offDays);
    }

    public void injectShifts() {
        Calendar calendar = Calendar.getInstance();
        List<Shift> shifts = shiftRepository.findAll();
        Shift shift1 = new Shift();
        shift1.setNurseId(nurseRepository.findNurseByTcKimlikNo("37012561724").orElseThrow().getId());
        calendar.set(2024, Calendar.MAY, 8, 8, 0, 0);
        shift1.setStartDate(calendar.getTime());
        calendar.set(2024, Calendar.MAY, 8, 16, 0, 0);
        shift1.setEndDate(calendar.getTime());
        shifts.add(shift1);

        Shift shift2 = new Shift();
        shift2.setNurseId(nurseRepository.findNurseByTcKimlikNo("12345678901").orElseThrow().getId());
        calendar.set(2024, Calendar.MAY, 8, 16, 0, 0);
        shift2.setStartDate(calendar.getTime());
        calendar.set(2024, Calendar.MAY, 8, 24, 0, 0);
        shift2.setEndDate(calendar.getTime());

        shifts.add(shift2);

        Shift shift3 = new Shift();
        shift3.setNurseId(nurseRepository.findNurseByTcKimlikNo("12345678902").orElseThrow().getId());
        calendar.set(2024, Calendar.MAY, 9, 0, 0, 0);
        shift3.setStartDate(calendar.getTime());
        calendar.set(2024, Calendar.MAY, 9, 8, 0, 0);
        shift3.setEndDate(calendar.getTime());
        shifts.add(shift3);

        Shift shift4 = new Shift();
        shift4.setNurseId(nurseRepository.findNurseByTcKimlikNo("37012561724").orElseThrow().getId());
        calendar.set(2024, Calendar.MAY, 9, 8, 0, 0);
        shift4.setStartDate(calendar.getTime());
        calendar.set(2024, Calendar.MAY, 9, 16, 0, 0);
        shift4.setEndDate(calendar.getTime());
        shifts.add(shift4);

        Shift shift5 = new Shift();
        shift5.setNurseId(nurseRepository.findNurseByTcKimlikNo("12345678901").orElseThrow().getId());
        calendar.set(2024, Calendar.MAY, 9, 16, 0, 0);
        shift5.setStartDate(calendar.getTime());
        calendar.set(2024, Calendar.MAY, 9, 24, 0, 0);
        shift5.setEndDate(calendar.getTime());
        shifts.add(shift5);

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
}

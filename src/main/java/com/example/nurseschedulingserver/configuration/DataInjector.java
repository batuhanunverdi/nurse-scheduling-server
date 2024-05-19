package com.example.nurseschedulingserver.configuration;

import com.example.nurseschedulingserver.dto.auth.AuthProjection;
import com.example.nurseschedulingserver.entity.constraint.Constraint;
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
import com.example.nurseschedulingserver.service.interfaces.NurseService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.text.ParseException;
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
    private final CPService cpService;
    private final NurseService nurseService;
    @Override
    public void run(String... args) throws ParseException {
        injectDepartments();
        injectNurse();
        injectOffDays();
        injectWorkDays();
        injectCP();
        injectExchangeShiftRequests();
    }
    public void injectCP(){
        List<Integer> minimumNursesForEachShift = new ArrayList<>(Arrays.asList(3,2,2));
        Department department = departmentRepository.findByName("Acil Servis").orElseThrow();
        List<Nurse> nurses = nurseService.getNursesByDepartment(department.getId());
        Constraint constraint = new Constraint();
        constraint.setDepartmentId(department.getId());
        constraint.setMinimumNursesForEachShift(minimumNursesForEachShift);
        cpService.createShifts(nurses,constraint);
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
    public void injectNurse(){
        String departmentId = departmentRepository.findByName("Acil Servis").orElseThrow().getId();
        String departmentId2 = departmentRepository.findByName("Yoğun Bakım Ünitesi").orElseThrow().getId();
        List<Nurse> nurses = nurseRepository.findAll();
        Nurse nurse = new Nurse();
        nurse.setFirstName("Mert Batuhan");
        nurse.setLastName("Ünverdi");
        nurse.setTcKimlikNo("37012561724");
        nurse.setPhoneNumber("0532 123 45 67");
        nurse.setDepartmentId(departmentId);
        nurse.setPassword(passwordEncoder.encode("Sanane5885"));
        nurse.setRole(Role.CHARGE);
        nurse.setGender("Erkek");
        nurse.setBirthDate("09.06.1998");
        nurses.add(nurse);

        String defaultPassword = "Sanane5885";

        for (int i = 0; i <8; i++) {
            Nurse nurse2 = new Nurse();
            nurse2.setFirstName("Hemşire" + (i));
            nurse2.setLastName("Soyadı");
            nurse2.setTcKimlikNo("1234567890" + (i));
            nurse2.setPhoneNumber("0532 123 45 67");
            nurse2.setDepartmentId(departmentId);
            nurse2.setPassword(passwordEncoder.encode(defaultPassword));
            nurse2.setRole(Role.NURSE);
            nurse2.setGender(i % 2 == 0 ? "Erkek" : "Kadın");
            nurse2.setBirthDate("09.06.1998");
            nurses.add(nurse2);
        }
        nurseRepository.saveAll(nurses);

    }
    public void injectOffDays(){
        List<AuthProjection> authProjections = new ArrayList<>();
        AuthProjection nurse = nurseRepository.findNurseByTcKimlikNo("37012561724").orElseThrow();
        AuthProjection nurse2 = nurseRepository.findNurseByTcKimlikNo("12345678901").orElseThrow();
        AuthProjection nurse3 = nurseRepository.findNurseByTcKimlikNo("12345678902").orElseThrow();
        authProjections.add(nurse);
        authProjections.add(nurse2);
        authProjections.add(nurse3);
        List<String> dates = new ArrayList<>();
        dates.add("08.04.2024");
        dates.add("09.04.2024");
        dates.add("10.04.2024");
        dates.add("11.04.2024");
        dates.add("12.04.2024");
        dates.add("13.04.2024");
        dates.add("14.04.2024");
        dates.add("15.04.2024");
        dates.add("16.04.2024");
        dates.add("17.04.2024");
        dates.add("18.04.2024");
        dates.add("19.04.2024");
        dates.add("20.04.2024");
        dates.add("21.04.2024");
        dates.add("22.04.2024");
        List<OffDay> offDays = new ArrayList<>();

        List<RequestStatus> statuses = List.of(
                RequestStatus.ACCEPTED,
                RequestStatus.REJECTED,
                RequestStatus.PENDING
        );

        for (int i = 0; i <15 ; i++) {
            OffDay offDay = new OffDay();
            offDay.setDate(dates.get(i));
            offDay.setNurseId(authProjections.get(i%3).getId());
            offDay.setStatus(statuses.get(new Random().nextInt(1)));
            offDays.add(offDay);
        }
        offDayRepository.saveAll(offDays);
    }

    public void injectShifts(){
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
    public void injectExchangeShiftRequests(){
    List<Shift> shifts = shiftRepository.findAll();
    if(shifts.size() < 2){
        return;
    }
        ExchangeShiftRequest exchangeShiftRequest = new ExchangeShiftRequest();
        exchangeShiftRequest.setRequesterShiftId(shifts.get(0).getId());
        exchangeShiftRequest.setRequestedShiftId(shifts.get(1).getId());
        exchangeShiftRequest.setStatus(RequestStatus.PENDING);
        exchangeShiftRequestRepository.save(exchangeShiftRequest);


    }

    public void injectWorkDays() throws ParseException {
        List<WorkDay> workDays = new ArrayList<>();
        List<Nurse> nurses = nurseRepository.findAll();

        List<Date> allDaysInJune = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        calendar.set(2024, Calendar.JUNE, 1);
        int daysInJune = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int day = 1; day <= daysInJune; day++) {
            calendar.set(Calendar.DAY_OF_MONTH, day);
            allDaysInJune.add(calendar.getTime());
        }

        Random random = new Random();
        for (Nurse nurse : nurses) {
            Set<Date> selectedDates = new HashSet<>();
            while (selectedDates.size() < 26) {
                int randomIndex = random.nextInt(allDaysInJune.size());
                selectedDates.add(allDaysInJune.get(randomIndex));
            }
            WorkDay workDay = new WorkDay();
            List<Date> datesWithMidnight = new ArrayList<>();
            for (Date date : selectedDates) {
                calendar.setTime(date);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                datesWithMidnight.add(calendar.getTime());
            }
            workDay.setWorkDate(datesWithMidnight);
            workDay.setNurseId(nurse.getId());
            workDays.add(workDay);
        }

        workDayRepository.saveAll(workDays);
    }


}

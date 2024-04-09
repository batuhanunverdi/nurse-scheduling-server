package com.example.nurseschedulingserver.configuration;

import com.example.nurseschedulingserver.dto.auth.AuthProjection;
import com.example.nurseschedulingserver.entity.department.Department;
import com.example.nurseschedulingserver.entity.nurse.Nurse;
import com.example.nurseschedulingserver.entity.offday.OffDay;
import com.example.nurseschedulingserver.enums.OffDayRequestStatus;
import com.example.nurseschedulingserver.enums.Role;
import com.example.nurseschedulingserver.repository.DepartmentRepository;
import com.example.nurseschedulingserver.repository.NurseRepository;
import com.example.nurseschedulingserver.repository.OffDayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Configuration
@RequiredArgsConstructor
public class DataInjector implements CommandLineRunner {
    private final DepartmentRepository departmentRepository;
    private final NurseRepository nurseRepository;
    private final OffDayRepository offDayRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public void run(String... args) {
        injectDepartments();
        injectNurse();
        injectOffDays();
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
        List<Nurse> nurses = nurseRepository.findAll();
        Nurse nurse = new Nurse();
        nurse.setFirstName("Mert Batuhan");
        nurse.setLastName("Ünverdi");
        nurse.setTcKimlikNo("37012561724");
        nurse.setPhoneNumber("0532 123 45 67");
        nurse.setDepartmentId(departmentId);
        nurse.setPassword(passwordEncoder.encode("Sanane5885"));
        nurse.setRole(Role.CHARGE);
        nurse.setProfilePicture("https://cdn-icons-png.flaticon.com/512/8496/8496122.png");
        nurses.add(nurse);

        Nurse nurse2 = new Nurse();
        nurse2.setFirstName("Mehmet");
        nurse2.setLastName("Yılmaz");
        nurse2.setTcKimlikNo("12345678901");
        nurse2.setPhoneNumber("0532 123 45 67");
        nurse2.setDepartmentId(departmentId);
        nurse2.setPassword(passwordEncoder.encode("Sanane5885"));
        nurse2.setRole(Role.NURSE);
        nurse2.setProfilePicture("https://cdn-icons-png.flaticon.com/512/8496/8496122.png");
        nurses.add(nurse2);

        Nurse nurse3 = new Nurse();
        nurse3.setFirstName("Ayşe");
        nurse3.setLastName("Yılmaz");
        nurse3.setTcKimlikNo("12345678902");
        nurse3.setPhoneNumber("0532 123 45 67");
        nurse3.setDepartmentId(departmentId);
        nurse3.setPassword(passwordEncoder.encode("Sanane5885"));
        nurse3.setRole(Role.NURSE);
        nurse3.setProfilePicture("https://cdn-icons-png.flaticon.com/512/8496/8496122.png");
        nurses.add(nurse3);
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

        List<OffDayRequestStatus> statuses = List.of(
                OffDayRequestStatus.ACCEPTED,
                OffDayRequestStatus.REJECTED,
                OffDayRequestStatus.PENDING
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
}

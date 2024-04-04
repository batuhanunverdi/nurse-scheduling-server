package com.example.nurseschedulingserver.configuration;

import com.example.nurseschedulingserver.entity.department.Department;
import com.example.nurseschedulingserver.entity.nurse.Nurse;
import com.example.nurseschedulingserver.enums.Role;
import com.example.nurseschedulingserver.repository.DepartmentRepository;
import com.example.nurseschedulingserver.repository.NurseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInjector implements CommandLineRunner {
    private final DepartmentRepository departmentRepository;
    private final NurseRepository nurseRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public void run(String... args) {
        injectDepartments();
        injectNurse();
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
        nurse.setRole(Role.NURSE);
        nurse.setProfilePicture("https://cdn-icons-png.flaticon.com/512/8496/8496122.png");
        nurses.add(nurse);

        // Add more nurses here with different departments and TC Kimlik No
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

        // Add more nurses here with different departments and TC Kimlik No
        Nurse nurse3 = new Nurse();
        nurse3.setFirstName("Ayşe");
        nurse3.setLastName("Yılmaz");
        nurse3.setTcKimlikNo("12345678902");
        nurse3.setPhoneNumber("0532 123 45 67");
        nurse3.setDepartmentId(departmentId);
        nurse3.setPassword(passwordEncoder.encode("Sanane5885"));
        nurse3.setRole(Role.CHARGE);
        nurse3.setProfilePicture("https://cdn-icons-png.flaticon.com/512/8496/8496122.png");
        nurses.add(nurse3);

        nurseRepository.saveAll(nurses);

    }
}

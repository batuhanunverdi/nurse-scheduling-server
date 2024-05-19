package com.example.nurseschedulingserver.service.implementations;

import com.example.nurseschedulingserver.dto.constraint.ConstraintResponseDto;
import com.example.nurseschedulingserver.entity.constraint.Constraint;
import com.example.nurseschedulingserver.entity.department.Department;
import com.example.nurseschedulingserver.entity.nurse.Nurse;
import com.example.nurseschedulingserver.repository.ConstraintRepository;
import com.example.nurseschedulingserver.service.interfaces.ConstraintService;
import com.example.nurseschedulingserver.service.interfaces.DepartmentService;
import com.example.nurseschedulingserver.service.interfaces.NurseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConstraintServiceImpl implements ConstraintService {
    private final ConstraintRepository constraintRepository;
    private final DepartmentService departmentService;
    private final NurseService nurseService;

    @Override
    public ConstraintResponseDto createConstraint(String departmentName, List<Integer> minimumNursesForEachShift) throws Exception {
        try {
            Department department = validateDepartment(departmentName);
            validateNurseAvailability(department, minimumNursesForEachShift);

            Constraint existingConstraint = getConstraintByDepartmentId(departmentName);
            if (existingConstraint != null) {
                return updateConstraintByDepartmentId(departmentName, minimumNursesForEachShift);
            }

            Constraint constraint = new Constraint();
            constraint.setDepartmentId(department.getId());
            constraint.setMinimumNursesForEachShift(minimumNursesForEachShift);
            constraintRepository.save(constraint);

            return new ConstraintResponseDto("Kısıtlamalar Başarıyla Oluşturuldu");
        } catch (Exception e) {
            throw new Exception("Kısıtlamalar Oluşturulurken Hata Oluştu: " + e.getMessage());
        }
    }

    @Override
    public ConstraintResponseDto updateConstraintByDepartmentId(String departmentName, List<Integer> minimumNursesForEachShift) throws Exception {
        try {
            Department department = validateDepartment(departmentName);
            validateNurseAvailability(department, minimumNursesForEachShift);

            Constraint constraint = getConstraintByDepartmentId(departmentName);
            if (constraint == null) {
                throw new Exception("Bu Departmana ait Kısıtlamalar Bulunamadı");
            }

            constraint.setMinimumNursesForEachShift(minimumNursesForEachShift);
            constraint.setDepartmentId(department.getId());
            constraintRepository.save(constraint);

            return new ConstraintResponseDto("Kısıtlamalar Başarıyla Güncellendi");
        } catch (Exception e) {
            throw new Exception("Kısıtlamalar Güncellenirken Hata Oluştu: " + e.getMessage());
        }
    }

    @Override
    public Constraint getConstraintByDepartmentId(String departmentName) {
        Department department = departmentService.getDepartmentByName(departmentName);
        if (department == null) {
            return null;
        }
        Optional<Constraint> constraint = constraintRepository.findByDepartmentId(department.getId());
        return constraint.orElse(null);
    }

    private Department validateDepartment(String departmentName) throws Exception {
        Department department = departmentService.getDepartmentByName(departmentName);
        if (department == null) {
            throw new Exception("Bu Departman Bulunamadı");
        }
        return department;
    }

    private void validateNurseAvailability(Department department, List<Integer> minimumNursesForEachShift) throws Exception {
        List<Nurse> nurses = nurseService.getNursesByDepartment(department.getId());
        int totalCount = minimumNursesForEachShift.stream().mapToInt(Integer::intValue).sum();
        if (totalCount > nurses.size()) {
            throw new Exception("Bu Departmanda Yeterli Hemşire Yok. Bu Departmandaki Hemşire Sayısı :"+ nurses.size() + " Kısıtlamaların Toplamı : " + totalCount);
        }
    }
}

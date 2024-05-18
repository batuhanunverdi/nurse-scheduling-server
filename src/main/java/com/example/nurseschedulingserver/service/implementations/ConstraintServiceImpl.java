package com.example.nurseschedulingserver.service.implementations;

import com.example.nurseschedulingserver.entity.constraint.Constraint;
import com.example.nurseschedulingserver.repository.ConstraintRepository;
import com.example.nurseschedulingserver.service.interfaces.ConstraintService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConstraintServiceImpl implements ConstraintService {
    private final ConstraintRepository constraintRepository;
    @Override
    public String createConstraint(String departmentId, List<Integer> minimumNursesForEachShift) {
        try{
            Constraint checkConstraint = getConstraintByDepartmentId(departmentId);
            if(checkConstraint != null){
                return updateConstraintByDepartmentId(departmentId, minimumNursesForEachShift);
            }
            Constraint constraint = new Constraint();
            constraint.setDepartmentId(departmentId);
            constraint.setMinimumNursesForEachShift(minimumNursesForEachShift);
            constraintRepository.save(constraint);
            return "Kısıtlamalar Başarıyla Oluşturuldu";
        }catch (Exception e){
            return "Kısıtlamalar Oluşturulurken Hata Oluştu";
        }
    }

    @Override
    public String updateConstraintByDepartmentId(String departmentId, List<Integer> minimumNursesForEachShift) {
        try{
            Constraint constraint = getConstraintByDepartmentId(departmentId);
            if(constraint == null){
                return "Bu Departmana ait Kısıtlamalar Bulunamadı";
            }
            else{
                constraint.setMinimumNursesForEachShift(minimumNursesForEachShift);
                constraintRepository.save(constraint);
                return "Kısıtlamalar Başarıyla Güncellendi";
            }
        }catch (Exception e){
            return "Kısıtlamalar Güncellenirken Hata Oluştu";
        }
    }

    @Override
    public Constraint getConstraintByDepartmentId(String departmentId) {
        Optional<Constraint> constraint = constraintRepository.findByDepartmentId(departmentId);
        return constraint.orElse(null);
    }
}

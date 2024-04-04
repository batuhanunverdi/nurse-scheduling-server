package com.example.nurseschedulingserver.repository;

import com.example.nurseschedulingserver.dto.auth.AuthProjection;
import com.example.nurseschedulingserver.dto.nurse.NurseDto;
import com.example.nurseschedulingserver.entity.nurse.Nurse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NurseRepository extends JpaRepository<Nurse, String> {
    Optional<Nurse> findByTcKimlikNo(String tcKimlikNo);

    @Query(nativeQuery = true,
            value =
            "SELECT nurses.id as id, nurses.first_name as firstName, nurses.last_name as lastName, departments.name as departmentName, " +
            "nurses.phone_number as phoneNumber, nurses.tc_kimlik_no as tcKimlikNo, nurses.role as role ,nurses.password as password," +
            "nurses.profile_picture as profilePicture FROM nurses " +
            "INNER JOIN departments " +
            "ON nurses.department_id = departments.id " +
            "WHERE nurses.tc_kimlik_no = ?1")
    Optional<AuthProjection> findNurseByTcKimlikNo(String tcKimlikNo);

    @Query(nativeQuery = true,
            value =
            "SELECT nurses.id as id, nurses.first_name as firstName, nurses.last_name as lastName, departments.name as departmentName, " +
            "nurses.tc_kimlik_no as tcKimlikNo, " +
            "nurses.profile_picture as profilePicture FROM nurses " +
            "INNER JOIN departments " +
            "ON nurses.department_id = departments.id ")
    Page<NurseDto> findAllNurses(Pageable pageable);


    @Query(nativeQuery = true,
            value =
                    "SELECT nurses.id as id, nurses.first_name as firstName, nurses.last_name as lastName, departments.name as departmentName, " +
                            "nurses.tc_kimlik_no as tcKimlikNo, " +
                            "nurses.profile_picture as profilePicture FROM nurses " +
                            "INNER JOIN departments " +
                            "ON nurses.department_id = departments.id " +
                            "WHERE nurses.id = ?1")
    Optional<NurseDto> findNurseById(String id);
}

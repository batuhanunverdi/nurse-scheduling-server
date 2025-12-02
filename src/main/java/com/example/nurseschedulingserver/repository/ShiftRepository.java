package com.example.nurseschedulingserver.repository;

import com.example.nurseschedulingserver.dto.shift.ShiftDto;
import com.example.nurseschedulingserver.entity.shift.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, String> {
    @Query(nativeQuery = true ,
            value = "SELECT shifts.id as id, shifts.start_date as startDate, shifts.end_date as endDate, shifts.nurse_id as nurseId, " +
                    "nurses.first_name as nurseFirstName, nurses.last_name as nurseLastName " +
                    "FROM shifts " +
                    "INNER JOIN nurses " +
                    "ON shifts.nurse_id = nurses.id " +
                    "WHERE shifts.id = ?1 "
    )
    Optional<ShiftDto> findShiftDtoById(String id);


    @Query(nativeQuery = true ,
            value = "SELECT shifts.id as id, shifts.start_date as startDate, shifts.end_date as endDate, shifts.nurse_id as nurseId, " +
                    "nurses.first_name as nurseFirstName, nurses.last_name as nurseLastName " +
                    "FROM shifts " +
                    "INNER JOIN nurses " +
                    "ON shifts.nurse_id = nurses.id " +
                    "INNER JOIN departments d ON d.id = nurses.department_id " +
                    "WHERE EXTRACT(MONTH FROM shifts.start_date) = :month " +
                    "AND EXTRACT(YEAR FROM shifts.start_date) = :year AND d.name = :departmentName "
    )
    List<ShiftDto> findAllShiftsByMothAndYear(int month, int year, String departmentName);

    @Query(nativeQuery = true,
            value = "SELECT shifts.id as id, shifts.start_date as startDate, shifts.end_date as endDate, shifts.nurse_id as nurseId, " +
                    "nurses.first_name as nurseFirstName, nurses.last_name as nurseLastName " +
                    "FROM shifts " +
                    "INNER JOIN nurses " +
                    "ON shifts.nurse_id = nurses.id " +
                    "WHERE shifts.nurse_id = ?1 " +
                    "AND CAST(shifts.start_date AS DATE) = CAST(?2 AS DATE)")
    ShiftDto findShiftsByNurseIdAndDate(String nurseId, String date);

    @Query(nativeQuery = true ,
            value = "SELECT shifts.id as id, shifts.start_date as startDate, shifts.end_date as endDate, shifts.nurse_id as nurseId, " +
                    "nurses.first_name as nurseFirstName, nurses.last_name as nurseLastName " +
                    "FROM shifts " +
                    "INNER JOIN nurses " +
                    "ON shifts.nurse_id = nurses.id " +
                    "INNER JOIN departments d on nurses.department_id = d.id " +
                    "WHERE CAST(shifts.start_date AS DATE) = CAST(?1 AS DATE) AND shifts.nurse_id <> ?2 AND d.name = ?3"
    )
    List<ShiftDto> findAllShiftsByDate(String date, String nurseId,String departmentName);

    @Query(nativeQuery = true ,
            value = "SELECT shifts.id as id, shifts.start_date as startDate, shifts.end_date as endDate, shifts.nurse_id as nurseId, " +
                    "nurses.first_name as nurseFirstName, nurses.last_name as nurseLastName " +
                    "FROM shifts " +
                    "INNER JOIN nurses " +
                    "ON shifts.nurse_id = nurses.id " +
                    "INNER JOIN departments d on nurses.department_id = d.id " +
                    "WHERE EXTRACT(MONTH FROM shifts.start_date) = :month " +
                    "AND EXTRACT(YEAR FROM shifts.start_date) = :year AND d.name = :departmentName AND shifts.nurse_id = :nurseId"
    )
    List<ShiftDto> findShiftsByNurseIdAndMonthAndYearAndDepartmentName(String nurseId, int month, int year, String departmentName);

    @Query(nativeQuery = true,
            value = "SELECT shifts.id as id, shifts.start_date as startDate, shifts.end_date as endDate, shifts.nurse_id as nurseId, " +
                    "nurses.first_name as nurseFirstName, nurses.last_name as nurseLastName " +
                    "FROM shifts " +
                    "INNER JOIN nurses " +
                    "ON shifts.nurse_id = nurses.id " +
                    "INNER JOIN departments d on nurses.department_id = d.id " +
                    "WHERE EXTRACT(MONTH FROM shifts.start_date) = :month " +
                    "AND EXTRACT(YEAR FROM shifts.start_date) = :year " +
                    "AND d.id = :departmentId " +
                    "AND EXTRACT(DAY FROM shifts.start_date) = :day"
    )
    List<ShiftDto> findShiftsByDepartmentNameAndDate(String departmentId, int month,int year, int day);

    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = "DELETE FROM shifts USING nurses n WHERE shifts.nurse_id = n.id " +
                    "AND n.department_id = :departmentId " +
                    "AND EXTRACT(MONTH FROM shifts.start_date) = :month " +
                    "AND EXTRACT(YEAR FROM shifts.start_date) = :year")
    void deleteByDepartmentAndMonth(String departmentId, int month, int year);
}

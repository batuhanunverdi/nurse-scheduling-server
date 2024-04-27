package com.example.nurseschedulingserver.repository;

import com.example.nurseschedulingserver.dto.shift.ShiftDto;
import com.example.nurseschedulingserver.entity.shift.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
                    "WHERE shifts.nurse_id = ?1 " +
                    "AND EXTRACT(MONTH FROM shifts.start_date) = CAST(?2 AS INTEGER) " +
                    "AND EXTRACT(YEAR FROM shifts.start_date) = CAST(?3 AS INTEGER)"
    )
    List<ShiftDto> findShiftsByNurseId(String nurseId, int month, int year);
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
                    "WHERE EXTRACT(MONTH FROM shifts.start_date) = :month " +
                    "AND EXTRACT(YEAR FROM shifts.start_date) = :year"
    )
    List<ShiftDto> findAllShiftsByMothAndYear(int month, int year);
}

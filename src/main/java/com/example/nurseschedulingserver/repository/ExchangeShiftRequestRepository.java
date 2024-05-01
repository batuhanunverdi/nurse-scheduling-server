package com.example.nurseschedulingserver.repository;

import com.example.nurseschedulingserver.dto.shift.ExchangeShiftRequestDto;
import com.example.nurseschedulingserver.entity.shift.ExchangeShiftRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExchangeShiftRequestRepository extends JpaRepository<ExchangeShiftRequest, String> {
    @Query("SELECT e.id as id," +
            "s1.id as requesterShiftId," +
            "s2.id as requestedShiftId," +
            "CONCAT(n2.firstName,' ',n2.lastName) as requesterFullName," +
            "CONCAT(n1.firstName,' ',n1.lastName)  as requestedFullName," +
            "s1.startDate as requesterShiftStartDate," +
            "s1.endDate as requesterShiftEndDate," +
            "s2.startDate as requestedShiftStartDate," +
            "s2.endDate as requestedShiftEndDate," +
            "e.status as status " +
            "FROM ExchangeShiftRequest e " +
            "INNER JOIN Shift s1 " +
            "ON s1.id = e.requesterShiftId " +
            "INNER JOIN Shift s2 " +
            "ON s2.id = e.requestedShiftId " +
            "INNER JOIN Nurse n1 " +
            "ON n1.id = s1.nurseId " +
            "INNER JOIN Nurse n2 " +
            "ON n2.id = s2.nurseId " +
            "WHERE s1.nurseId= ?1 " +
            "AND e.status = 'PENDING' " +
            "AND MONTH(s1.startDate) = ?2 " +
            "AND YEAR(s1.startDate) = ?3 " +
            "AND MONTH(s2.startDate) = ?2 " +
            "AND YEAR(s2.startDate) = ?3")
    List<ExchangeShiftRequestDto> findAllByRequestedShiftId(String requestedShiftId,int month,int year);
}

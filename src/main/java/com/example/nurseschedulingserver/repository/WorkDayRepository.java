package com.example.nurseschedulingserver.repository;

import com.example.nurseschedulingserver.dto.workday.WorkDayResponseDto;
import com.example.nurseschedulingserver.entity.workday.WorkDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkDayRepository extends JpaRepository<WorkDay, String>{

    Optional<WorkDay> findByNurseId(String id);


}

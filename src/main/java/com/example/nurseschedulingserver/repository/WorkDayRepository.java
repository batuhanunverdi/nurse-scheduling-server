package com.example.nurseschedulingserver.repository;

import com.example.nurseschedulingserver.entity.workday.WorkDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkDayRepository extends JpaRepository<WorkDay, String>{

}

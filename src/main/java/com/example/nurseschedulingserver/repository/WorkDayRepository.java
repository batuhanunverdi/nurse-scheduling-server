package com.example.nurseschedulingserver.repository;

import com.example.nurseschedulingserver.entity.workday.WorkDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface WorkDayRepository extends JpaRepository<WorkDay, String>{

    Optional<WorkDay> findByNurseId(String id);

    @Query(
            value = "SELECT * FROM work_days INNER JOIN work_day_work_date wdwd on work_days.id = wdwd.work_day_id WHERE nurse_id = ?1 AND EXTRACT(MONTH FROM work_date) = ?2 AND EXTRACT(YEAR FROM work_date) = ?3 ",
            nativeQuery = true
    )
    WorkDay findAllByNurseIdAndDate(String id,int month,int year);


    boolean existsAllByWorkDateContaining(Date date);
}

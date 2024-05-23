package com.example.nurseschedulingserver.repository;

import com.example.nurseschedulingserver.entity.workday.WorkDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkDayRepository extends JpaRepository<WorkDay, String>{


    @Query(
            value = "SELECT * FROM work_days INNER JOIN work_day_work_date wdwd on work_days.id = wdwd.work_day_id WHERE nurse_id = ?1 AND EXTRACT(MONTH FROM work_date) = ?2 AND EXTRACT(YEAR FROM work_date) = ?3 ",
            nativeQuery = true
    )
    WorkDay findAllByNurseIdAndDate(String id,int month,int year);


    @Query(
            value = "SELECT work_days.* FROM work_days INNER JOIN work_day_work_date wdwd on work_days.id = wdwd.work_day_id INNER JOIN nurses on nurses.id=work_days.nurse_id WHERE nurses.department_id=:departmentId AND  EXTRACT(MONTH FROM work_date) = :month AND EXTRACT(YEAR FROM work_date) = :year",
            nativeQuery = true
    )
    List<WorkDay> findAllByMonthAndYearAndDepartmentId(int month, int year, String departmentId);



    @Query(
            value = "SELECT * FROM work_days INNER JOIN work_day_work_date wdwd on work_days.id = wdwd.work_day_id WHERE nurse_id = ?1 AND EXTRACT(MONTH FROM work_date) = ?2 AND EXTRACT(YEAR FROM work_date) = ?3 ",
            nativeQuery = true
    )
    Optional<WorkDay> findByNurseIdAndMonthAndYear(String id,int month,int year);
}

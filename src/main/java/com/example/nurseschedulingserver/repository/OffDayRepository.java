package com.example.nurseschedulingserver.repository;

import com.example.nurseschedulingserver.dto.offday.OffDayResponseDto;
import com.example.nurseschedulingserver.entity.offday.OffDay;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OffDayRepository extends JpaRepository<OffDay, String> {

    @Query(nativeQuery = true,
            value =
                    "SELECT off_days.id as id, off_days.date as date, nurses.first_name || ' ' || nurses.last_name as nurseName, " +
                            "off_days.nurse_id as nurseId, off_days.status as status " +
                            "FROM off_days " +
                            "INNER JOIN nurses " +
                            "ON off_days.nurse_id = nurses.id WHERE off_days.status = ?1 ORDER BY off_days.date DESC")
    Page<OffDayResponseDto> findAllWithinPageAndStatus(String status,Pageable pageable);
}

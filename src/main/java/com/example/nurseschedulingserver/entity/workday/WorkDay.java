package com.example.nurseschedulingserver.entity.workday;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.jpa.repository.Query;

import java.util.ArrayList;
import java.sql.Date;
import java.util.List;


@Getter
@Setter
@Table(name = "work_days")
@Entity
public class WorkDay {
    @Id
    @UuidGenerator
    private String id;
    private String nurseId;
    @ElementCollection(targetClass = Date.class, fetch = FetchType.EAGER)
    private List<Date> workDate = new ArrayList<Date>();

}

package com.example.nurseschedulingserver.entity.workday;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;


@Getter
@Setter
@Table(name = "work_days")
@Entity
public class WorkDay {
    @Id
    @UuidGenerator
    private String id;
    private String nurseId;
    private String workDate;

}

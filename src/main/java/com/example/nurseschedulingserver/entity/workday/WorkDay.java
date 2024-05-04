package com.example.nurseschedulingserver.entity.workday;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.Date;
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
    private List<Date> workDate = new ArrayList<>();
    @CreationTimestamp
    private Date createdAt;
    @UpdateTimestamp
    private Date updatedAt;

}

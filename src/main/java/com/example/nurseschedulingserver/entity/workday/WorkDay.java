package com.example.nurseschedulingserver.entity.workday;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

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
    @CreatedDate
    private Date createdAt;
    @LastModifiedDate
    private Date updatedAt;

}

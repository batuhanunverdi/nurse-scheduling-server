package com.example.nurseschedulingserver.entity.shift;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "shifts")
@Entity
public class Shift {

    @Id
    @UuidGenerator
    private String id;
    private Date startDate;
    private Date endDate;
    private String nurseId;
    @CreatedDate
    private Date createdAt;
    @LastModifiedDate
    private Date updatedAt;


}

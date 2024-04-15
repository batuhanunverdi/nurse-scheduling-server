package com.example.nurseschedulingserver.entity.shift;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

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
    private String startDate;
    private String endDate;
    private String nurseId;

}

package com.example.nurseschedulingserver.entity.constraint;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;

@Table(name = "constraints")
@Entity
@Getter
@Setter
public class Constraint {
    @UuidGenerator
    @Id
    private String id;
    @Column(unique = true)
    private String departmentId;
    @ElementCollection(targetClass = Integer.class, fetch = FetchType.EAGER)
    private List<Integer> minimumNursesForEachShift;

}

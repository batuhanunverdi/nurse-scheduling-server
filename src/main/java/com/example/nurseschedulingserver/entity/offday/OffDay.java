package com.example.nurseschedulingserver.entity.offday;

import com.example.nurseschedulingserver.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Getter
@Setter
@Table(name = "off_days")
@Entity
public class OffDay {
    @Id
    @UuidGenerator
    private String id;
    private String date;
    private String nurseId;
    @Enumerated(EnumType.STRING)
    private RequestStatus status;
    @CreationTimestamp
    private String createdAt;
    @UpdateTimestamp
    private String updatedAt;
}

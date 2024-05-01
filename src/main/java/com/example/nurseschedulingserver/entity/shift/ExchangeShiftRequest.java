package com.example.nurseschedulingserver.entity.shift;

import com.example.nurseschedulingserver.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Getter
@Setter
@Entity
@Table(name = "exchange_shift_request")
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeShiftRequest {
    @Id
    @UuidGenerator
    private String id;
    private String requesterShiftId;
    private String requestedShiftId;
    @Enumerated(EnumType.STRING)
    private RequestStatus status;
}

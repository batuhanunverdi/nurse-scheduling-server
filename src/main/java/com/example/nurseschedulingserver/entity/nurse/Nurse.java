package com.example.nurseschedulingserver.entity.nurse;

import com.example.nurseschedulingserver.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "nurses")
@Entity
public class Nurse {

    @Id
    @GeneratedValue(generator = "uuid")
    @UuidGenerator
    private String id;
    private String firstName;
    private String lastName;
    private String tcKimlikNo;
    private String phoneNumber;
    private String departmentId;
    private String password;
    private String profilePicture;
    @Enumerated(EnumType.STRING)
    private Role role;


}

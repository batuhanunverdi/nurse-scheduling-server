package com.example.nurseschedulingserver.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDto {
    private String id;
    private String firstName;
    private String lastName;
    private String departmentName;
    private String phoneNumber;
    private String tcKimlikNo;
    private String role;
    private String gender;
    private String birthdate;
    private String errorMessage;
    private String pictureUrl;

    public AuthResponseDto(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public static AuthResponseDto buildForError(String errorMessage) {
        return new AuthResponseDto(errorMessage);
    }

}

package com.example.nurseschedulingserver.dto.auth;


public interface AuthProjection {
    String getId();
    String getFirstName();
    String getLastName();
    String getDepartmentName();
    String getPhoneNumber();
    String getTcKimlikNo();
    String getRole();
    String getPassword();

    String getGender();
    String getBirthDate();

}

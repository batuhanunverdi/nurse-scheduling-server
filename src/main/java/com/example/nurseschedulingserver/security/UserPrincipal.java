package com.example.nurseschedulingserver.security;

import com.example.nurseschedulingserver.entity.nurse.Nurse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {
    private final Nurse nurse;

    public UserPrincipal(Nurse nurse) {
        this.nurse = nurse;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.<GrantedAuthority>singletonList(new SimpleGrantedAuthority(nurse.getRole().name()));
    }

    @Override
    public String getPassword() {
        return nurse.getPassword();
    }

    @Override
    public String getUsername() {
        return nurse.getTcKimlikNo();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

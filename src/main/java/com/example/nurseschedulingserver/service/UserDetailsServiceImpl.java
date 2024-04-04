package com.example.nurseschedulingserver.service;

import com.example.nurseschedulingserver.entity.nurse.Nurse;
import com.example.nurseschedulingserver.repository.NurseRepository;
import com.example.nurseschedulingserver.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final NurseRepository nurseRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Nurse nurse = nurseRepository.findByTcKimlikNo(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        return new UserPrincipal(nurse);
    }
}

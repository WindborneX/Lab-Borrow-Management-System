package com.lab.borrow.security;

import com.lab.borrow.entity.User;
import com.lab.borrow.entity.UserRole;
import com.lab.borrow.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String studentId) {
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));

        String role = user.getRole() == UserRole.ADMINISTRATOR
                ? "ADMINISTRATOR"
                : "STUDENT";

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getStudentId())
                .password(user.getPasswordHash() == null ? "" : user.getPasswordHash())
                .roles(role)
                .build();
    }
}

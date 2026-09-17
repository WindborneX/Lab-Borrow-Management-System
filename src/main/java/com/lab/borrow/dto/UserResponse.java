package com.lab.borrow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lab.borrow.entity.User;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String studentId,
        String username,
        int role,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getStudentId(),
                user.getUsername(),
                user.getRole().getCode(),
                user.getCreatedAt()
        );
    }
}

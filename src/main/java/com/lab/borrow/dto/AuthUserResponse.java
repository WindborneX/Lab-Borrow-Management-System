package com.lab.borrow.dto;

import com.lab.borrow.entity.User;

public record AuthUserResponse(
        Long id,
        String studentId,
        String username,
        int role
) {

    public static AuthUserResponse from(User user) {
        return new AuthUserResponse(
                user.getId(),
                user.getStudentId(),
                user.getUsername(),
                user.getRole().getCode()
        );
    }
}

package com.lab.borrow.dto;

public record UserCreateRequest(
        String studentId,
        String username,
        String password,
        Integer role
) {
}

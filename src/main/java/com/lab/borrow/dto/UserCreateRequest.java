package com.lab.borrow.dto;

public record UserCreateRequest(
        String studentId,
        String username,
        Integer role
) {
}

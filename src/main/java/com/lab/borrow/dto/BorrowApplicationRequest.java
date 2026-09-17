package com.lab.borrow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record BorrowApplicationRequest(
        Long equipmentId,
        Long userId,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime expectReturnTime
) {
}

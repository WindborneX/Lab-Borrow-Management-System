package com.lab.borrow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lab.borrow.entity.BorrowRecord;

import java.time.LocalDateTime;

public record BorrowRecordResponse(
        Long id,
        Long equipmentId,
        Long userId,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime borrowTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime expectReturnTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime actualReturnTime,
        int status
) {

    public static BorrowRecordResponse from(BorrowRecord record) {
        return new BorrowRecordResponse(
                record.getId(),
                record.getEquipmentId(),
                record.getUserId(),
                record.getBorrowTime(),
                record.getExpectReturnTime(),
                record.getActualReturnTime(),
                record.getStatus().getCode()
        );
    }
}

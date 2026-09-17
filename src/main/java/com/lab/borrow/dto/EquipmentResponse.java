package com.lab.borrow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lab.borrow.entity.Equipment;

import java.time.LocalDateTime;

public record EquipmentResponse(
        Long id,
        String name,
        String category,
        int status,
        String description,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt
) {

    public static EquipmentResponse from(Equipment equipment) {
        return new EquipmentResponse(
                equipment.getId(),
                equipment.getName(),
                equipment.getCategory(),
                equipment.getStatus().getCode(),
                equipment.getDescription(),
                equipment.getCreatedAt()
        );
    }
}

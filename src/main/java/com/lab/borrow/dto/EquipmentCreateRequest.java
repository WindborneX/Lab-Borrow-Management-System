package com.lab.borrow.dto;

public record EquipmentCreateRequest(
        String name,
        String category,
        String description
) {
}

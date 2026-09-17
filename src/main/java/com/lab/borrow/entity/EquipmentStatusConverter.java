package com.lab.borrow.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class EquipmentStatusConverter implements AttributeConverter<EquipmentStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(EquipmentStatus attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public EquipmentStatus convertToEntityAttribute(Integer dbData) {
        return EquipmentStatus.fromCode(dbData);
    }
}

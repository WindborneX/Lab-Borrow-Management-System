package com.lab.borrow.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class BorrowStatusConverter implements AttributeConverter<BorrowStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(BorrowStatus attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public BorrowStatus convertToEntityAttribute(Integer dbData) {
        return BorrowStatus.fromCode(dbData);
    }
}

package com.aromasenja.domain.user.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Shift kerja admin.
 * DB: varchar CHECK (work_shift IN ('pagi', 'siang', 'malam'))
 */
public enum ShiftAdmin {

    PAGI, SIANG, MALAM;

    public String toDbValue() {
        return name().toLowerCase();
    }

    public static ShiftAdmin fromDbValue(String value) {
        if (value == null) return null;
        return valueOf(value.toUpperCase());
    }

    @Converter(autoApply = true)
    public static class ShiftAdminConverter implements AttributeConverter<ShiftAdmin, String> {

        @Override
        public String convertToDatabaseColumn(ShiftAdmin shift) {
            return shift == null ? null : shift.toDbValue();
        }

        @Override
        public ShiftAdmin convertToEntityAttribute(String dbData) {
            return dbData == null ? null : ShiftAdmin.fromDbValue(dbData);
        }
    }
}

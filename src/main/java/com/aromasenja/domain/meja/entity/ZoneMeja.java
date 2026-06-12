package com.aromasenja.domain.meja.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Zona penempatan meja.
 * DB: varchar CHECK (zone IN ('Indoor', 'Outdoor'))
 * Perhatikan: DB menggunakan Title Case (bukan lowercase seperti enum lain).
 */
public enum ZoneMeja {

    INDOOR("Indoor"), OUTDOOR("Outdoor");

    private final String dbValue;

    ZoneMeja(String dbValue) {
        this.dbValue = dbValue;
    }

    public String toDbValue() {
        return dbValue;
    }

    public static ZoneMeja fromDbValue(String value) {
        if (value == null) return null;
        for (ZoneMeja z : values()) {
            if (z.dbValue.equalsIgnoreCase(value)) return z;
        }
        throw new IllegalArgumentException("Unknown ZoneMeja DB value: " + value);
    }

    @Converter(autoApply = true)
    public static class ZoneMejaConverter implements AttributeConverter<ZoneMeja, String> {

        @Override
        public String convertToDatabaseColumn(ZoneMeja zone) {
            return zone == null ? null : zone.toDbValue();
        }

        @Override
        public ZoneMeja convertToEntityAttribute(String dbData) {
            return dbData == null ? null : ZoneMeja.fromDbValue(dbData);
        }
    }
}

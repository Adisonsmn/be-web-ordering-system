package com.aromasenja.domain.pesanan.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Metode pembayaran pesanan.
 * DB: varchar CHECK (metode_pembayaran IN ('cash', 'qris'))
 */
public enum MetodePembayaran {

    CASH, QRIS;

    public String toDbValue() {
        return name().toLowerCase();
    }

    public static MetodePembayaran fromDbValue(String value) {
        if (value == null) return null;
        return valueOf(value.toUpperCase());
    }

    @Converter(autoApply = true)
    public static class MetodePembayaranConverter implements AttributeConverter<MetodePembayaran, String> {

        @Override
        public String convertToDatabaseColumn(MetodePembayaran metode) {
            return metode == null ? null : metode.toDbValue();
        }

        @Override
        public MetodePembayaran convertToEntityAttribute(String dbData) {
            return dbData == null ? null : MetodePembayaran.fromDbValue(dbData);
        }
    }
}

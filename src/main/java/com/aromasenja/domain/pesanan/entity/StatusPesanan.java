package com.aromasenja.domain.pesanan.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Status pesanan — mengikuti lifecycle pesanan dari NEW sampai CANCELLED.
 * DB: varchar CHECK (status IN ('new', 'preparing', 'ready', 'served', 'cancelled'))
 *
 * Flow normal: NEW → PREPARING → READY → SERVED
 * Flow cancel: NEW → CANCELLED (hanya sebelum PREPARING)
 */
public enum StatusPesanan {

    NEW, PREPARING, READY, SERVED, CANCELLED;

    public String toDbValue() {
        return name().toLowerCase();
    }

    public static StatusPesanan fromDbValue(String value) {
        if (value == null) return null;
        return valueOf(value.toUpperCase());
    }

    @Converter(autoApply = true)
    public static class StatusPesananConverter implements AttributeConverter<StatusPesanan, String> {

        @Override
        public String convertToDatabaseColumn(StatusPesanan status) {
            return status == null ? null : status.toDbValue();
        }

        @Override
        public StatusPesanan convertToEntityAttribute(String dbData) {
            return dbData == null ? null : StatusPesanan.fromDbValue(dbData);
        }
    }
}

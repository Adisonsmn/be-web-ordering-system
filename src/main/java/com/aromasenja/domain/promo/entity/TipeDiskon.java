package com.aromasenja.domain.promo.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Tipe diskon promo: nominal (Rp) atau persen (%).
 * DB: varchar CHECK (tipe_diskon IN ('nominal', 'persen'))
 */
public enum TipeDiskon {

    NOMINAL, PERSEN;

    public String toDbValue() {
        return name().toLowerCase();
    }

    public static TipeDiskon fromDbValue(String value) {
        if (value == null) return null;
        return valueOf(value.toUpperCase());
    }

    @Converter(autoApply = true)
    public static class TipeDiskonConverter implements AttributeConverter<TipeDiskon, String> {

        @Override
        public String convertToDatabaseColumn(TipeDiskon tipe) {
            return tipe == null ? null : tipe.toDbValue();
        }

        @Override
        public TipeDiskon convertToEntityAttribute(String dbData) {
            return dbData == null ? null : TipeDiskon.fromDbValue(dbData);
        }
    }
}

package com.aromasenja.domain.poin.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Tipe transaksi poin loyalitas: earn (mendapat poin) atau redeem (gunakan poin).
 * DB: varchar CHECK (tipe IN ('earn', 'redeem'))
 */
public enum TipePoin {

    EARN, REDEEM, REFUND;

    public String toDbValue() {
        return name().toLowerCase();
    }

    public static TipePoin fromDbValue(String value) {
        if (value == null) return null;
        return valueOf(value.toUpperCase());
    }

    @Converter(autoApply = true)
    public static class TipePoinConverter implements AttributeConverter<TipePoin, String> {

        @Override
        public String convertToDatabaseColumn(TipePoin tipe) {
            return tipe == null ? null : tipe.toDbValue();
        }

        @Override
        public TipePoin convertToEntityAttribute(String dbData) {
            return dbData == null ? null : TipePoin.fromDbValue(dbData);
        }
    }
}

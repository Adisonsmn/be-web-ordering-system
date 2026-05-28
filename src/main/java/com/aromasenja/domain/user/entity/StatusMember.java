package com.aromasenja.domain.user.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Status keanggotaan client.
 * DB: varchar CHECK (status_member IN ('guest', 'regular', 'premium'))
 *
 * Catatan: Dengan Pure JWT guest (Pilihan B), record client di DB
 * hanya berisi REGULAR dan PREMIUM. GUEST hanya ada sebagai fallback
 * jika suatu saat strategi guest diubah ke Pilihan A.
 */
public enum StatusMember {

    GUEST, REGULAR, PREMIUM;

    public String toDbValue() {
        return name().toLowerCase();
    }

    public static StatusMember fromDbValue(String value) {
        if (value == null) return null;
        return valueOf(value.toUpperCase());
    }

    @Converter(autoApply = true)
    public static class StatusMemberConverter implements AttributeConverter<StatusMember, String> {

        @Override
        public String convertToDatabaseColumn(StatusMember status) {
            return status == null ? null : status.toDbValue();
        }

        @Override
        public StatusMember convertToEntityAttribute(String dbData) {
            return dbData == null ? null : StatusMember.fromDbValue(dbData);
        }
    }
}

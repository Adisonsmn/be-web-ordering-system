package com.aromasenja.common;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Enum role pengguna — dipakai lintas domain.
 * DB menyimpan nilai lowercase ('admin', 'client') sesuai CHECK constraint tabel users.
 * Konversi otomatis melalui inner AttributeConverter dengan autoApply = true.
 */
public enum Role {

    ADMIN, CLIENT;

    /** Nilai yang disimpan di database (lowercase). */
    public String toDbValue() {
        return name().toLowerCase();
    }

    /** Parse dari nilai DB ke enum Java. */
    public static Role fromDbValue(String value) {
        if (value == null) return null;
        return valueOf(value.toUpperCase());
    }

    /**
     * JPA AttributeConverter — otomatis diterapkan ke semua field bertipe Role
     * tanpa perlu @Convert di tiap entity field.
     */
    @Converter(autoApply = true)
    public static class RoleAttributeConverter implements AttributeConverter<Role, String> {

        @Override
        public String convertToDatabaseColumn(Role role) {
            return role == null ? null : role.toDbValue();
        }

        @Override
        public Role convertToEntityAttribute(String dbData) {
            return dbData == null ? null : Role.fromDbValue(dbData);
        }
    }
}

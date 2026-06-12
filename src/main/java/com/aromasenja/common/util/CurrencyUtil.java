package com.aromasenja.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility class untuk formatting dan perhitungan mata uang Rupiah.
 * Tidak dapat di-instantiate — semua method bersifat static.
 */
public final class CurrencyUtil {

    private static final Locale LOCALE_ID = new Locale("id", "ID");

    private CurrencyUtil() {
        // Utility class — tidak boleh di-instantiate
    }

    /**
     * Format BigDecimal ke string Rupiah.
     * Contoh: 15000 → "Rp15.000"
     *
     * @param amount jumlah uang
     * @return string terformat dalam Rupiah
     */
    public static String formatRupiah(BigDecimal amount) {
        if (amount == null) return "Rp0";
        NumberFormat formatter = NumberFormat.getCurrencyInstance(LOCALE_ID);
        formatter.setMaximumFractionDigits(0);
        formatter.setMinimumFractionDigits(0);
        return formatter.format(amount);
    }

    /**
     * Pembulatan ke kelipatan unit tertentu menggunakan HALF_UP.
     * Contoh: roundToNearest(15340, 500) → 15.500
     *
     * @param amount jumlah asli
     * @param unit   kelipatan pembulatan (misal: 500, 1000)
     * @return jumlah yang sudah dibulatkan
     */
    public static BigDecimal roundToNearest(BigDecimal amount, BigDecimal unit) {
        if (amount == null || unit == null || unit.compareTo(BigDecimal.ZERO) == 0) {
            return amount;
        }
        return amount.divide(unit, 0, RoundingMode.HALF_UP).multiply(unit);
    }
}

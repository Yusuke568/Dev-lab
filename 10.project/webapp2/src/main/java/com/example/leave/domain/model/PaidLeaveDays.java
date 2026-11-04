package com.example.leave.domain.model;

import java.math.BigDecimal;

/**
 * 有給休暇の日数（0.5 日単位）の表示。
 */
public final class PaidLeaveDays {

    private PaidLeaveDays() {
    }

    /** 12.0 → "12"、11.5 → "11.5" */
    public static String format(double days) {
        return BigDecimal.valueOf(days).stripTrailingZeros().toPlainString();
    }
}

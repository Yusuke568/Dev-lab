package com.example.kintai.domain.port.out;

import java.time.LocalDate;
import java.util.Map;

/**
 * 会社休日（マスタ管理で登録した、国民の祝日以外の会社の休日）を読むポート。
 */
public interface CompanyHolidayPort {

    /** 開始日〜終了日（両端を含む）の会社休日（日付 → 名前）。 */
    Map<LocalDate, String> findCompanyHolidays(LocalDate from, LocalDate to);
}

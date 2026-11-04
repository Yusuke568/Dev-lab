-- 期間（開始日〜終了日、両端を含む）の会社休日
SELECT holiday_date, name
FROM company_holiday
WHERE holiday_date >= ? AND holiday_date <= ?
ORDER BY holiday_date;

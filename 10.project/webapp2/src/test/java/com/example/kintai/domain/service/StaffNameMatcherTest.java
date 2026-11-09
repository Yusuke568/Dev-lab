package com.example.kintai.domain.service;

import com.example.kintai.domain.model.attendance.Subordinate;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 作業報告書がどの社員のものかを決める */
class StaffNameMatcherTest {

    private final List<Subordinate> staff = List.of(
            new Subordinate("126", "中島 祐介", "第一開発課"),
            new Subordinate("147", "浦山　純平", "第一開発課"),
            new Subordinate("200", "中島 太郎", "営業部"));

    @Test
    void 名前欄の空白の違いは無視する() {
        assertEquals(Optional.of("126"), StaffNameMatcher.match("中島　祐介", "work.xlsm", staff));
        assertEquals(Optional.of("147"), StaffNameMatcher.match("浦山 純平", null, staff));
    }

    @Test
    void 名前欄で決まらなければファイル名から決める() {
        assertEquals(Optional.of("126"), StaffNameMatcher.match(null, "作業報告書2026（中島祐介）_41.xlsm", staff));
    }

    @Test
    void 一人に決まらなければ決めない() {
        assertEquals(Optional.empty(), StaffNameMatcher.match("山田 花子", "作業報告書2026.xlsm", staff));
        // 同じ名前の社員が2人いる
        List<Subordinate> twins = List.of(new Subordinate("1", "佐藤 一郎", null), new Subordinate("2", "佐藤　一郎", null));
        assertEquals(Optional.empty(), StaffNameMatcher.match("佐藤 一郎", "佐藤一郎.xlsm", twins));
    }
}

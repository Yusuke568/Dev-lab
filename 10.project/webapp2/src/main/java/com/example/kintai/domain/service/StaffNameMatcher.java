package com.example.kintai.domain.service;

import com.example.kintai.domain.model.attendance.Subordinate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 作業報告書がどの社員のものかを、シートの「名前：」欄とファイル名から決める。
 * 空白（半角・全角）は無視して比べる。1人に決まらなければ決めない（画面で選んでもらう）。
 */
public final class StaffNameMatcher {

    private StaffNameMatcher() {
    }

    /**
     * @param sheetName  作業報告書の「名前：」欄（なければ null）
     * @param fileName   アップロードしたファイル名（例：作業報告書2026（中島祐介）_41.xlsm）
     * @param candidates 取り込める社員
     * @return 1人に決まればその社員ID
     */
    public static Optional<String> match(String sheetName, String fileName, List<Subordinate> candidates) {
        String byName = normalize(sheetName);
        if (!byName.isEmpty()) {
            List<Subordinate> hits = candidates.stream()
                    .filter(c -> normalize(c.getName()).equals(byName))
                    .collect(Collectors.toList());
            if (hits.size() == 1) {
                return Optional.of(hits.get(0).getStaffId());
            }
        }
        String file = normalize(fileName);
        if (!file.isEmpty()) {
            List<Subordinate> hits = candidates.stream()
                    .filter(c -> !normalize(c.getName()).isEmpty() && file.contains(normalize(c.getName())))
                    .collect(Collectors.toList());
            if (hits.size() == 1) {
                return Optional.of(hits.get(0).getStaffId());
            }
        }
        return Optional.empty();
    }

    /** 空白（半角・全角・改行）を除く */
    static String normalize(String value) {
        return value == null ? "" : value.replaceAll("[\\s\\u3000]", "");
    }
}

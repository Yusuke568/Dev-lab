package com.example.kintai.domain.service;

import java.time.YearMonth;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 作業報告書のファイル名（拡張子 .xlsm を除く）を、マスタで決めた形から作る。
 *
 * 形に書ける差し込み：
 *   {年}     … 4けたの年（例：2026）
 *   {月}     … 2けたの月（例：11）
 *   {氏名}   … 社員の氏名（空白は詰める）
 *   {社員ID} … 社員ID
 * それ以外の文字はそのまま使う。例：「作業報告書{年}（{氏名}）_41」→「作業報告書2026（中島祐介）_41」
 */
public final class WorkReportFileName {

    /** マスタで決めていないときの形（今までのダウンロードの名前と同じ） */
    public static final String DEFAULT_PATTERN = "作業報告書{年}{月}";
    public static final int MAX_LENGTH = 100;

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{([^{}]*)\\}");
    /** ファイル名に使えない文字（Windows の決まり） */
    private static final Pattern FORBIDDEN = Pattern.compile("[\\\\/:*?\"<>|]");

    private WorkReportFileName() {
    }

    /**
     * ファイル名を作る。形が空なら既定の形を使う。
     * 差し込んだ値にファイル名に使えない文字があれば取り除く。
     */
    public static String render(String pattern, YearMonth yearMonth, String staffId, String staffName) {
        String p = pattern == null || pattern.isBlank() ? DEFAULT_PATTERN : pattern.trim();
        Matcher m = PLACEHOLDER.matcher(p);
        StringBuilder out = new StringBuilder();
        while (m.find()) {
            String value;
            switch (m.group(1)) {
                case "年": value = String.valueOf(yearMonth.getYear()); break;
                case "月": value = String.format("%02d", yearMonth.getMonthValue()); break;
                case "氏名": value = staffName == null ? "" : staffName.replaceAll("[\\s　]", ""); break;
                case "社員ID": value = staffId == null ? "" : staffId; break;
                default: value = m.group(0);
            }
            m.appendReplacement(out, Matcher.quoteReplacement(value));
        }
        m.appendTail(out);
        String name = FORBIDDEN.matcher(out.toString()).replaceAll("").trim();
        return name.isEmpty() ? render(DEFAULT_PATTERN, yearMonth, staffId, staffName) : name;
    }

    /**
     * マスタで保存する前に形を確かめる。問題があれば IllegalArgumentException（画面に出す文）。
     * @return 前後の空白を除いた形
     */
    public static String validate(String pattern) {
        String p = pattern == null ? "" : pattern.trim();
        if (p.isEmpty()) {
            throw new IllegalArgumentException("ファイル名の形を入れてください（例：作業報告書{年}（{氏名}）_41）。");
        }
        if (p.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("ファイル名の形は" + MAX_LENGTH + "文字以内で入れてください。");
        }
        if (FORBIDDEN.matcher(p).find()) {
            throw new IllegalArgumentException("ファイル名に使えない文字（\\ / : * ? \" < > |）は入れられません。");
        }
        Matcher m = PLACEHOLDER.matcher(p);
        while (m.find()) {
            String key = m.group(1);
            if (!key.equals("年") && !key.equals("月") && !key.equals("氏名") && !key.equals("社員ID")) {
                throw new IllegalArgumentException("「{" + key + "}」は使えません。差し込めるのは {年} {月} {氏名} {社員ID} です。");
            }
        }
        if (p.endsWith(".xlsm")) {
            throw new IllegalArgumentException("拡張子（.xlsm）は自動で付くので、入れないでください。");
        }
        return p;
    }
}

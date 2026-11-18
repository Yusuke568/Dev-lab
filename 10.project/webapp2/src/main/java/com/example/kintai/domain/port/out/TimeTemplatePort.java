package com.example.kintai.domain.port.out;

/**
 * 社員ごとの「自分の時刻テンプレート」（出勤・退勤の組を1つ）。時刻は "HH:mm"。
 */
public interface TimeTemplatePort {

    /** 登録済みのテンプレート（[出勤, 退勤]）。未登録なら null */
    String[] findTimeTemplate(String staffId);

    /** テンプレートを保存する。start・end のどちらかが null なら登録を消す */
    void saveTimeTemplate(String staffId, String start, String end);
}

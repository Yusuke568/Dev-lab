package com.example.application.port.in;

/**
 * 社員登録のためのデータを保持するコマンドオブジェクト。
 *
 * @param name      社員名
 * @param namekana  社員名（カナ）
 * @param gender    性別
 * @param entryyear 入社年
 * @param jobclass  役職
 */
public record InsertShainCommand(
        String name,
        String namekana,
        String gender,
        int entryyear,
        String jobclass
) {
}

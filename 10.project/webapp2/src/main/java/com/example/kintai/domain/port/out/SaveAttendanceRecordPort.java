package com.example.kintai.domain.port.out;

import com.example.kintai.domain.model.attendance.AttendanceRecord;

/**
 * 勤怠記録を保存/更新するためのポート（インターフェース）。
 *
 * このインターフェースの実装は、インフラストラクチャ層（アダプタ）が担当します。
 */
public interface SaveAttendanceRecordPort {

    /**
     * 勤怠記録を永続化します。
     * 新規作成または更新の両方を扱います。
     *
     * @param record 保存する勤怠記録オブジェクト
     */
    void save(AttendanceRecord record);
}

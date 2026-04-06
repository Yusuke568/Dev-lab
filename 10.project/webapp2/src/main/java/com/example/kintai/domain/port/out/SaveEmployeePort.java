package com.example.kintai.domain.port.out;

import com.example.kintai.domain.model.employee.Employee;

/**
 * 社員情報を保存/更新するためのポート（インターフェース）。
 *
 * このインターフェースの実装は、インフラストラクチャ層（アダプタ）が担当します。
 */
public interface SaveEmployeePort {

    /**
     * 社員情報を永続化します。
     * 新規作成または更新の両方を扱います。
     *
     * @param employee 保存する社員オブジェクト
     */
    void save(Employee employee);
}

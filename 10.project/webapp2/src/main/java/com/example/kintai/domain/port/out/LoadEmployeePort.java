package com.example.kintai.domain.port.out;

import com.example.kintai.domain.model.employee.Employee;
import com.example.kintai.domain.model.employee.EmployeeId;

import java.util.List;
import java.util.Optional;

/**
 * 社員情報を読み込むためのポート（インターフェース）。
 *
 * このインターフェースの実装は、インフラストラクチャ層（アダプタ）が担当します。
 */
public interface LoadEmployeePort {

    /**
     * 指定されたIDの社員を検索します。
     *
     * @param employeeId 検索する社員のID
     * @return 見つかった場合は社員のOptional、見つからない場合はOptional.empty()
     */
    Optional<Employee> load(EmployeeId employeeId);

    /**
     * すべての社員を取得します。
     *
     * @return 全社員のリスト
     */
    List<Employee> loadAll();
}

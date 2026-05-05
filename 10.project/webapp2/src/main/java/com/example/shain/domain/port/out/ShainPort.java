package com.example.shain.domain.port.out;

import com.example.shain.domain.model.Shain;
import com.example.shain.domain.model.ShainId;
import java.util.List;
import java.util.Optional;

/**
 * 社員データの永続化に関する操作を定義する出力ポート（リポジトリ）。
 */
public interface ShainPort {

    /**
     * すべての社員を検索します。
     */
    List<Shain> findAll();

    /**
     * IDによって単一の社員を検索します。
     */
    Optional<Shain> findById(ShainId id);

    /**
     * 社員情報を更新します。
     */
    void update(Shain shain);

    /**
     * 新しい社員を作成します。
     */
    void create(Shain shain);

    /**
     * IDを指定して社員を削除します。
     */
    void deleteById(ShainId id);

    /**
     * 次の利用可能な社員IDを取得します。
     */
    ShainId nextId();
}

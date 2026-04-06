package com.example.application.port.out;

import com.example.entity.Shain;
import java.util.List;
import java.util.Optional;

/**
 * 社員データの永続化に関する操作を定義する出力ポート。
 */
public interface ShainPort {

    /**
     * すべての社員エンティティを永続化層から検索します。
     * @return 見つかった社員エンティティのリスト
     */
    List<Shain> findAll();

    /**
     * IDによって単一の社員エンティティを永続化層から検索します。
     * @param id 検索する社員のID
     * @return 見つかった社員エンティティを含むOptional、見つからなければ空のOptional
     */
    Optional<Shain> findById(int id);

    /**
     * 社員エンティティの変更を永続化します。
     * @param shain 更新する社員エンティティ
     */
    void update(Shain shain);

    /**
     * 新しい社員エンティティを永続化します。
     * @param shain 作成する社員エンティティ
     */
    void create(Shain shain);

    /**
     * IDを指定して社員エンティティを削除します。
     * @param id 削除する社員のID
     */
    void deleteById(int id);

    /**
     * 全社員の数を取得します。
     * @return 社員の総数
     */
    long count();
}

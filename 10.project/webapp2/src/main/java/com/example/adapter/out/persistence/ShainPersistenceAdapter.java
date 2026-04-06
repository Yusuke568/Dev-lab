package com.example.adapter.out.persistence;

import com.example.application.port.out.ShainPort;
import com.example.dao.ShainDao; // 古いDAOを一時的に利用
import com.example.entity.Shain;
import com.example.infra.ConnectionBase;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * 社員データのための永続化アダプタ。
 * ShainPortを実装し、データベースとのやり取りを隠蔽します。
 */
public class ShainPersistenceAdapter implements ShainPort {

    private final ShainDao shainDao; // 移行期間中、古いDAOを利用

    public ShainPersistenceAdapter() {
        this.shainDao = new ShainDao();
    }

    @Override
    public List<Shain> findAll() {
        // ConnectionBaseからコネクションを取得し、古いDAOのメソッドを呼び出す
        try (Connection con = ConnectionBase.getConnection()) {
            return shainDao.findAll(con);
        } catch (Exception e) {
            // 本来はより具体的な例外処理とロギングを行う
            throw new RuntimeException("Failed to find all shain", e);
        }
    }

    @Override
    public Optional<Shain> findById(int id) {
        try (Connection con = ConnectionBase.getConnection()) {
            // 古いDAOを呼び出し、結果をOptionalでラップする
            return Optional.ofNullable(shainDao.findById(con, id));
        } catch (Exception e) {
            // 本来はより具体的な例外処理とロギングを行う
            throw new RuntimeException("Failed to find shain by id: " + id, e);
        }
    }

    @Override
    public void update(Shain shain) {
        try (Connection con = ConnectionBase.getConnection()) {
            shainDao.update(con, shain);
        } catch (Exception e) {
            // 本来はより具体的な例外処理とロギングを行う
            throw new RuntimeException("Failed to update shain: " + shain.getId(), e);
        }
    }

    @Override
    public void create(Shain shain) {
        try (Connection con = ConnectionBase.getConnection()) {
            shainDao.create(con, shain);
        } catch (Exception e) {
            // 本来はより具体的な例外処理とロギングを行う
            throw new RuntimeException("Failed to create shain", e);
        }
    }

    @Override
    public void deleteById(int id) {
        try (Connection con = ConnectionBase.getConnection()) {
            shainDao.deleteById(con, id);
        } catch (Exception e) {
            // 本来はより具体的な例外処理とロギングを行う
            throw new RuntimeException("Failed to delete shain: " + id, e);
        }
    }

    @Override
    public long count() {
        // 注: この実装は非効率です。将来的には "SELECT COUNT(*)" を実行する
        // 専用のDAOメソッドを呼び出すようにリファクタリングするべきです。
        // ここでは古いロジックを維持するため、findAll().size() を使います。
        return findAll().size();
    }
}

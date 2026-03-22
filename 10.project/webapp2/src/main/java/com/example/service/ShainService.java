package com.example.service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.naming.NamingException;

import com.example.entity.Shain;
import com.example.service.transaction.TransactionManager;
import com.example.infra.ConnectionBase;
import com.example.dao.ShainDao;

public class ShainService {

    private final ShainDao shainDao;
    private final TransactionManager transactionManager;

    public ShainService() {
        this(new ShainDao(), new TransactionManager());
    }

    // テスト用にコンストラクタを公開
    public ShainService(ShainDao shainDao, TransactionManager transactionManager) {
        this.shainDao = shainDao;
        this.transactionManager = transactionManager;
    }

    // 全社員を取得
    public List<Shain> getAllShain() throws SQLException, NamingException, IOException {
        try (Connection con = ConnectionBase.getConnection()) {
            return shainDao.findAll(con);
        }
    }

    // 社員を削除
    public void deleteShain(int id) throws SQLException, NamingException, IOException {
        transactionManager.execute(con -> {
            shainDao.deleteById(con, id);
            return null; // 戻り値なし
        });
    }

    // 社員情報を更新
    public void updateShain(Shain shain) throws SQLException, NamingException, IOException {
        transactionManager.execute(con -> {
            shainDao.update(con, shain);
            return null; // 戻り値なし
        });
    }

    // 社員IDで取得
    public Shain getShainById(int id) throws SQLException, NamingException, IOException {
        try (Connection con = ConnectionBase.getConnection()) {
            return shainDao.findById(con, id);
        }
    }

    // 社員を登録
    public void insertShain(Shain shain) throws SQLException, NamingException, IOException {
        transactionManager.execute(con -> {
            shainDao.create(con, shain);
            return null; // 戻り値なし
        });
    }

    /**
     * 次に採番される社員IDを取得します。
     * @return 次の社員ID
     */
    public int getNextShainId() throws SQLException, NamingException, IOException {
        try (Connection con = ConnectionBase.getConnection()) {
            // このロジックはパフォーマンス上問題があるため、本来は COUNT(*) や MAX(id) を使うべきです。
            return shainDao.findAll(con).size() + 100; // 仮の採番ロジック
        }
    }

    /**
     * 名前で社員を検索します。
     * @param name 検索キーワード
     * @return 検索結果の社員リスト
     */
    public List<Shain> searchByName(String name) throws SQLException, NamingException, IOException {
        try (Connection con = ConnectionBase.getConnection()) {
            return shainDao.searchByName(con, name);
        }
    }

    // 全社員の情報を取得（勤続年数も計算）
    public List<Shain> getAllShainWithLeaveInfo(Connection con) throws SQLException, IOException {
        List<Shain> shainList = shainDao.findAll(con);
        int currentYear = java.time.Year.now().getValue();
        for (Shain shain : shainList) {
            shain.setYearsOfService(currentYear - shain.getEntryyear());
        }
        return shainList;
    }

    // コネクションを内部で開く古いメソッドも後方互換性のために残す（あるいは削除を選択）
    public List<Shain> getAllShainWithLeaveInfo() throws SQLException, NamingException, IOException {
        try (Connection con = ConnectionBase.getConnection()) {
            return getAllShainWithLeaveInfo(con);
        }
    }
}

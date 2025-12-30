package dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.common.MyUtil;

import com.example.entity.Classmaster;

/**
 * 役職マスター(class_masterテーブル)へのアクセスを行うDAO。
 */
public class ClassmasterDao {

    /**
     * すべての役職情報を取得します。
     * @param con データベース接続
     * @return 全役職の情報が格納されたArrayList
     * @throws SQLException
     * @throws IOException
     */
    public List<Classmaster> findAll(Connection con) throws SQLException, IOException {

        List<Classmaster> classmasterList = new ArrayList<>();

        // 役職を取得するSQL
        String sql = MyUtil.loadSqlFromClasspath("sql/classmaster/get_all_class.sql");

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            // SQL文をコンソール表示
            System.out.println(pstmt.toString());
            // SQL実行
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Classmaster classmaster = new Classmaster();
                classmaster.setId(rs.getString("ID"));
                classmaster.setName(rs.getString("NAME"));
                classmasterList.add(classmaster);
            }
        }
        return classmasterList;
    }
}

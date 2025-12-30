package dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import com.example.common.MyUtil;

import com.example.entity.Shain;
import com.example.infra.ConnectionBase;

public class ShainDao {

    /**
     * 全社員の情報を取得します。
     * @param con データベース接続
     * @return 全社員の情報が格納されたArrayList
     * @throws SQLException
     * @throws IOException
     */
    public List<Shain> findAll(Connection con) throws SQLException, IOException {

        // ArrayListの初期化
        List<Shain> shainList = new ArrayList<>();

        // 社員を取得するSQL
        String sql = MyUtil.loadSqlFromClasspath("sql/shainlist/get_all_shain.sql");

        // SQL実行の準備
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            // SQL文をコンソール表示
            System.out.println(pstmt.toString());
            // SQL実行
            ResultSet rs = pstmt.executeQuery();
            // 取得した行数を繰り返す
            while (rs.next()) {
                Shain shain = new Shain();

                // 値を設定
                shain.setId(rs.getInt("id"));
                shain.setName(rs.getString("name"));
                shain.setNamekana(rs.getString("namekana"));
                shain.setEntryyear(rs.getInt("entry_year"));
                shain.setGender(rs.getString("gender"));
                shain.setJobclass(rs.getString("jobclass"));
                shain.setPaidLeaveDays(rs.getInt("paid_leave_days"));
                shainList.add(shain);
            }
        }
        return shainList;
    }

    /**
     * 社員情報を登録します。
     * @param con データベース接続
     * @param shain 登録する社員情報
     * @throws SQLException
     * @throws IOException
     */
    public void create(Connection con, Shain shain) throws SQLException, IOException {
        //社員を登録するSQL
        String sql = MyUtil.loadSqlFromClasspath("sql/shainlist/insert_shain.sql");

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            //パラメータをSQLにセット
            pstmt.setInt(1, shain.getId());
            pstmt.setString(2, shain.getName());
            pstmt.setString(3, shain.getNamekana());
            pstmt.setInt(4, shain.getEntryyear());
            pstmt.setString(5, shain.getGender());
            pstmt.setInt(6, shain.getPaidLeaveDays());
            pstmt.setString(7, shain.getJobclass());

            //SQL文を表示
            System.out.println(pstmt.toString());

            //SQL実行
            pstmt.executeUpdate();
        }
    }

    /**
     * IDをキーに社員情報を1件取得します。
     * @param con データベース接続
     * @param id 取得する社員のID
     * @return 取得した社員の情報。見つからない場合はnull。
     * @throws SQLException
     * @throws IOException
     */
    public Shain findById(Connection con, int id) throws SQLException, IOException {
        Shain shain = null;

        //社員を取得するSQL
        String sql = MyUtil.loadSqlFromClasspath("sql/shainlist/select_shain.sql");

        //SQL実行の準備
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            //パラメータをSQLにセット
            pstmt.setInt(1, id);
            //SQL文を表示
            System.out.println(pstmt.toString());
            //SQL実行
            ResultSet rs = pstmt.executeQuery();
            // 取得した行があれば
            if (rs.next()) {
                shain = new Shain();
                //値を取得
                shain.setId(id);
                shain.setName(rs.getString("name"));
                shain.setNamekana(rs.getString("namekana"));
                shain.setGender(rs.getString("gender"));
                shain.setEntryyear(rs.getInt("entry_year"));
                shain.setJobclass(rs.getString("jobclass"));
                shain.setPaidLeaveDays(rs.getInt("paid_leave_days"));
            }
        }
        return shain;
    }

    /**
     * 社員情報を更新します。
     * @param con データベース接続
     * @param shain 更新する社員情報
     * @throws SQLException
     * @throws IOException
     */
    public void update(Connection con, Shain shain) throws SQLException, IOException {
        //社員を更新するSQL
        String sql = MyUtil.loadSqlFromClasspath("sql/shainlist/update_shain.sql");
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            //パラメータをSQLにセット
            pstmt.setString(1, shain.getName());
            pstmt.setString(2, shain.getNamekana());
            pstmt.setInt(3, shain.getEntryyear());
            pstmt.setString(4, shain.getGender());
            pstmt.setInt(5, shain.getPaidLeaveDays());
            pstmt.setString(6, shain.getJobclass());
            pstmt.setInt(7, shain.getId());
            //SQL文を表示
            System.out.println(pstmt.toString());
            //SQL実行
            pstmt.executeUpdate();
        }
    }

    /**
     * IDをキーに社員情報を削除します。
     * @param con データベース接続
     * @param id 削除する社員のID
     * @throws SQLException
     * @throws IOException
     */
    public void deleteById(Connection con, int id) throws SQLException, IOException {
        //社員を削除するSQL
        String sql = MyUtil.loadSqlFromClasspath("sql/shainlist/delete_shain.sql");
        //SQL実行の準備
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            //パラメータをSQLにセット
            pstmt.setInt(1, id);
            //SQL文を表示
            System.out.println(pstmt.toString());
            //SQL実行
            pstmt.executeUpdate();
        }
    }

    /**
     * 名前をキーに社員情報を検索します（部分一致）。
     * @param con データベース接続
     * @param name 検索する名前
     * @return 取得した社員の情報リスト
     * @throws SQLException
     * @throws IOException
     */
    public List<Shain> searchByName(Connection con, String name) throws SQLException, IOException {
        List<Shain> shainList = new ArrayList<>();
        // get_all_shain.sqlを流用し、WHERE句を追加する
        String sql = MyUtil.loadSqlFromClasspath("sql/shainlist/get_all_shain.sql") + " WHERE name LIKE ?";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, "%" + name + "%");
            System.out.println(pstmt.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Shain shain = new Shain();
                shain.setId(rs.getInt("id"));
                shain.setName(rs.getString("name"));
                shain.setNamekana(rs.getString("namekana"));
                shain.setEntryyear(rs.getInt("entry_year"));
                shain.setGender(rs.getString("gender"));
                shain.setJobclass(rs.getString("jobclass"));
                shain.setPaidLeaveDays(rs.getInt("paid_leave_days"));
                shainList.add(shain);
            }
        }
        return shainList;
    }
}

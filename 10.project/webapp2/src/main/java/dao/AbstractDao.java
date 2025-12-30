package dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.common.MyUtil;
import com.example.entity.WorkType;

public class AbstractDao {

    /**
     * 適用マスタを全件取得します。
     * @param con データベース接続
     * @return 適用マスタ(WorkType)のリスト
     * @throws SQLException
     * @throws IOException
     */
    public List<WorkType> findAll(Connection con) throws SQLException, IOException {
        List<WorkType> workTypeList = new ArrayList<>();
        String sql = MyUtil.loadSqlFromClasspath("sql/kintai/get_abstract.sql");

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                WorkType workType = new WorkType();
                workType.setId(rs.getInt("ID"));
                workType.setName(rs.getString("NAME"));
                workType.setWork(rs.getBoolean("IS_WORK"));
                workType.setPaid(rs.getBoolean("IS_PAID"));
                workType.setLegalHoliday(rs.getBoolean("IS_LEGAL_HOLIDAY"));
                workType.setPrescribedHoliday(rs.getBoolean("IS_PRESCRIBED_HOLIDAY"));
                workTypeList.add(workType);
            }
        }
        return workTypeList;
    }
}

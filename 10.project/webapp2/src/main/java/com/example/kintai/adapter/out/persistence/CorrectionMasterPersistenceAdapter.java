package com.example.kintai.adapter.out.persistence;

import com.example.kintai.domain.model.attendance.CorrectionMaster;
import com.example.kintai.domain.port.out.CorrectionMasterPort;
import com.example.shared.persistence.ConnectionBase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 補正マスタの永続化アダプタ。
 */
public class CorrectionMasterPersistenceAdapter implements CorrectionMasterPort {

    private static final String SQL_BASE_PATH = "/sql/kintai/";

    @Override
    public List<CorrectionMaster> findAll() {
        String sql = readSqlFile("get_all_correction_master.sql");
        List<CorrectionMaster> list = new ArrayList<>();

        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new CorrectionMaster(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("correction_us_time"),
                        rs.getInt("correction_mid_time")
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load correction master.", e);
        }

        return list;
    }

    @Override
    public void create(String name, int correctionUsTime, int correctionMidTime) {
        String sql = readSqlFile("insert_correction_master.sql");

        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, correctionUsTime);
            ps.setInt(3, correctionMidTime);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create correction master.", e);
        }
    }

    private String readSqlFile(String fileName) {
        try (InputStream is = getClass().getResourceAsStream(SQL_BASE_PATH + fileName)) {
            if (is == null) {
                throw new IOException("SQL file not found: " + fileName);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read SQL file: " + fileName, e);
        }
    }
}

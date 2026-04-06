package com.example.kintai.adapter.out.persistence;

import com.example.kintai.domain.model.employee.Employee;
import com.example.kintai.domain.model.employee.EmployeeId;
import com.example.kintai.domain.port.out.LoadEmployeePort;
import com.example.kintai.domain.port.out.SaveEmployeePort;
import com.example.infra.ConnectionBase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 社員情報の永続化を担当するアダプタ。
 * JDBCを使用してデータベースと直接通信します。
 */
public class EmployeePersistenceAdapter implements LoadEmployeePort, SaveEmployeePort {

    private static final String SQL_BASE_PATH = "/sql/shainlist/";

    @Override
    public Optional<Employee> load(EmployeeId employeeId) {
        String sql = readSqlFile("select_shain.sql");
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, Integer.parseInt(employeeId.getValue()));
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapToEmployee(rs));
                }
            }
        } catch (Exception e) {
            // 実際にはより詳細な例外処理/ロギングを行う
            throw new RuntimeException("Failed to load employee.", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Employee> loadAll() {
        String sql = readSqlFile("get_all_shain.sql");
        List<Employee> employees = new ArrayList<>();
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                employees.add(mapToEmployee(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load all employees.", e);
        }
        return employees;
    }

    @Override
    public void save(Employee employee) {
        // 既存か新規かでINSERT/UPDATEを分岐
        boolean exists = load(employee.getId()).isPresent();
        
        String sqlFileName = exists ? "update_shain.sql" : "insert_shain.sql";
        String sql = readSqlFile(sqlFileName);

        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (exists) {
                // UPDATE
                ps.setString(1, employee.getName());
                ps.setInt(2, Integer.parseInt(employee.getId().getValue()));
            } else {
                // INSERT
                ps.setInt(1, Integer.parseInt(employee.getId().getValue()));
                ps.setString(2, employee.getName());
                // 他の必須カラムがあれば設定
            }
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save employee.", e);
        }
    }

    /**
     * ResultSetの現在行からEmployeeオブジェクトにマッピングします。
     */
    private Employee mapToEmployee(ResultSet rs) throws SQLException {
        String id = String.valueOf(rs.getInt("id"));
        String name = rs.getString("name");
        // entryyearなど、他のプロパティも必要に応じてマッピング
        return new Employee(new EmployeeId(id), name);
    }
    
    /**
     * クラスパスからSQLファイルを読み込みます。
     */
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

package model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import beans.LeaveGrantRuleBean;
import beans.ShainBean;
import dao.LeaveDao;

class LeaveServiceTest {

    private LeaveService leaveService;
    private LeaveDaoMock leaveDaoMock;
    private ShainServiceMock shainServiceMock;

    private static final int TEST_EMPLOYEE_ID = 1;
    private static final String[] TEST_EMPLOYEE_IDS = {"1", "2", "3"};

    // --- Mock Classes ---

    class LeaveDaoMock extends LeaveDao {
        // メソッド呼び出しの引数を記録するフィールド
        private List<ShainBean> updatedEmployees = null;
        private String[] addDaysToSelected_ids_arg;
        private int addDaysToSelected_days_arg;
        private int updateDays_id_arg;
        private int updateDays_days_arg;
        private int incrementDays_id_arg;
        private int decrementDays_id_arg;
        
        // メソッド呼び出し回数を記録するカウンター
        public int addDaysToSelectedCallCount = 0;
        public int updateDaysCallCount = 0;
        public int incrementDaysCallCount = 0;
        public int decrementDaysCallCount = 0;

        @Override
        public List<LeaveGrantRuleBean> findAllRules() {
            List<LeaveGrantRuleBean> rules = new ArrayList<>();
            rules.add(new LeaveGrantRuleBean(1, 0, 10)); // 0年で10日
            rules.add(new LeaveGrantRuleBean(2, 1, 11)); // 1年で11日
            rules.add(new LeaveGrantRuleBean(3, 2, 12)); // 2年で12日
            return rules;
        }

        @Override
        public void batchUpdateDays(List<ShainBean> employees) {
            this.updatedEmployees = employees; // 更新されたリストを保存
        }
        
        @Override
        public void addDaysToSelected(String[] ids, int days) {
            this.addDaysToSelectedCallCount++;
            this.addDaysToSelected_ids_arg = ids;
            this.addDaysToSelected_days_arg = days;
        }

        @Override
        public void updateDays(int id, int days) {
            this.updateDaysCallCount++;
            this.updateDays_id_arg = id;
            this.updateDays_days_arg = days;
        }

        @Override
        public void incrementDays(int staffId) {
            this.incrementDaysCallCount++;
            this.incrementDays_id_arg = staffId;
        }

        @Override
        public void decrementDays(int staffId) {
            this.decrementDaysCallCount++;
            this.decrementDays_id_arg = staffId;
        }
        
        // --- Getter for assertion ---
        public List<ShainBean> getUpdatedEmployees() { return updatedEmployees; }
        public String[] getAddDaysToSelected_ids_arg() { return addDaysToSelected_ids_arg; }
        public int getAddDaysToSelected_days_arg() { return addDaysToSelected_days_arg; }
        public int getUpdateDays_id_arg() { return updateDays_id_arg; }
        public int getUpdateDays_days_arg() { return updateDays_days_arg; }
        public int getIncrementDays_id_arg() { return incrementDays_id_arg; }
        public int getDecrementDays_id_arg() { return decrementDays_id_arg; }
    }

    class ShainServiceMock extends ShainService {
         // ShainDaoのモックを渡す必要があるのでコンストラクタを定義
        public ShainServiceMock() throws NamingException, SQLException, IOException {
            super(null); // 親のコンストラクタにnullを渡す
        }

        @Override
        public ArrayList<ShainBean> getAllShainWithLeaveInfo() {
            ArrayList<ShainBean> employees = new ArrayList<>();
            
            ShainBean employee1 = new ShainBean();
            employee1.setId(1);
            employee1.setName("勤続1年");
            employee1.setYearsOfService(1);
            employees.add(employee1);

            ShainBean employee2 = new ShainBean();
            employee2.setId(2);
            employee2.setName("勤続10年");
            employee2.setYearsOfService(10);
            employees.add(employee2);
            
            return employees;
        }
    }
    
    // --- Test Setup ---

    @BeforeEach
    void setUp() throws NamingException, SQLException, IOException {
        leaveDaoMock = new LeaveDaoMock();
        shainServiceMock = new ShainServiceMock();
        // DIを使ってモックを注入
        leaveService = new LeaveService(leaveDaoMock, shainServiceMock);
    }

    // --- Tests ---

    @Test
    @DisplayName("勤続年数に応じた有給付与が正しく計算され、更新処理が呼ばれること")
    void testGrantLeaveByYearsOfService() throws Exception {
        // --- 実行 ---
        leaveService.grantLeaveByYearsOfService();

        // --- 検証 ---
        List<ShainBean> updated = leaveDaoMock.getUpdatedEmployees();
        assertNotNull(updated, "更新処理が呼ばれていること");
        assertEquals(2, updated.size(), "対象の社員数が正しいこと");

        // 各社員の付与日数が正しいかチェック
        ShainBean employee1 = updated.stream().filter(e -> e.getId() == 1).findFirst().orElse(null);
        assertNotNull(employee1);
        assertEquals(11, employee1.getPaidLeaveDays(), "勤続1年の社員には11日付与されること");
        
        ShainBean employee2 = updated.stream().filter(e -> e.getId() == 2).findFirst().orElse(null);
        assertNotNull(employee2);
        assertEquals(12, employee2.getPaidLeaveDays(), "勤続10年の社員には最大の12日付与されること");
    }

    @Test
    @DisplayName("選択した社員に有給を一括加算するDAOメソッドが正しい引数で呼ばれること")
    void testAddDaysToSelected() {
        int daysToAdd = 5;
        try {
            // --- 実行 ---
            leaveService.addDaysToSelected(TEST_EMPLOYEE_IDS, daysToAdd);
        } catch (Exception e) {
            fail("Exception should not be thrown", e);
        }

        // --- 検証 ---
        assertEquals(1, leaveDaoMock.addDaysToSelectedCallCount, "DAOのaddDaysToSelectedメソッドが1回呼ばれること");
        assertArrayEquals(TEST_EMPLOYEE_IDS, leaveDaoMock.getAddDaysToSelected_ids_arg(), "引数の社員ID配列が正しいこと");
        assertEquals(daysToAdd, leaveDaoMock.getAddDaysToSelected_days_arg(), "引数の日数が正しいこと");
    }

    @Test
    @DisplayName("指定した社員の有給を更新するDAOメソッドが正しい引数で呼ばれること")
    void testUpdateDays() {
        int daysToUpdate = 20;
        try {
            // --- 実行 ---
            leaveService.updateDays(TEST_EMPLOYEE_ID, daysToUpdate);
        } catch (Exception e) {
            fail("Exception should not be thrown", e);
        }

        // --- 検証 ---
        assertEquals(1, leaveDaoMock.updateDaysCallCount, "DAOのupdateDaysメソッドが1回呼ばれること");
        assertEquals(TEST_EMPLOYEE_ID, leaveDaoMock.getUpdateDays_id_arg(), "引数の社員IDが正しいこと");
        assertEquals(daysToUpdate, leaveDaoMock.getUpdateDays_days_arg(), "引数の日数が正しいこと");
    }

    @Test
    @DisplayName("有給を1日加算するDAOメソッドが正しい引数で呼ばれること")
    void testIncrementDays() {
        try {
            // --- 実行 ---
            leaveService.incrementDays(TEST_EMPLOYEE_ID);
        } catch (Exception e) {
            fail("Exception should not be thrown", e);
        }
        
        // --- 検証 ---
        assertEquals(1, leaveDaoMock.incrementDaysCallCount, "DAOのincrementDaysメソッドが1回呼ばれること");
        assertEquals(TEST_EMPLOYEE_ID, leaveDaoMock.getIncrementDays_id_arg(), "引数の社員IDが正しいこと");
    }

    @Test
    @DisplayName("有給を1日減算するDAOメソッドが正しい引数で呼ばれること")
    void testDecrementDays() {
        try {
            // --- 実行 ---
            leaveService.decrementDays(TEST_EMPLOYEE_ID);
        } catch (Exception e) {
            fail("Exception should not be thrown", e);
        }

        // --- 検証 ---
        assertEquals(1, leaveDaoMock.decrementDaysCallCount, "DAOのdecrementDaysメソッドが1回呼ばれること");
        assertEquals(TEST_EMPLOYEE_ID, leaveDaoMock.getDecrementDays_id_arg(), "引数の社員IDが正しいこと");
    }
}

package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import beans.ShainBean;
import dao.ShainDao;

class ShainServiceTest {

    private ShainService shainService;
    private ShainDaoMock shainDaoMock;

    private static final int TEST_EMPLOYEE_ID = 1;
    private static final int CURRENT_YEAR = Year.now().getValue();

    /**
     * ShainDaoのモック（偽物）クラス。
     * テスト用に、DBに接続せずにダミーのデータを返す。
     */
    class ShainDaoMock extends ShainDao {
        private final Map<Integer, ShainBean> db = new HashMap<>();
        
        // メソッド呼び出しの引数と回数を記録するフィールド
        public int deleteByIdCallCount = 0;
        public int deleteById_id_arg;
        public int updateCallCount = 0;
        public ShainBean update_bean_arg;
        public int createCallCount = 0;
        public ShainBean create_bean_arg;

        public ShainDaoMock() {
            // テスト用の初期データ
            ShainBean shain1 = new ShainBean();
            shain1.setId(TEST_EMPLOYEE_ID);
            shain1.setName("テスト太郎");
            shain1.setEntryyear(CURRENT_YEAR - 10); // 勤続10年
            db.put(shain1.getId(), shain1);

            ShainBean shain2 = new ShainBean();
            shain2.setId(2);
            shain2.setName("テスト花子");
            shain2.setEntryyear(CURRENT_YEAR); // 勤続0年
            db.put(shain2.getId(), shain2);
        }

        @Override
        public ArrayList<ShainBean> findAll() {
            return new ArrayList<>(db.values());
        }

        @Override
        public void deleteById(int id) {
            this.deleteByIdCallCount++;
            this.deleteById_id_arg = id;
            db.remove(id);
        }

        @Override
        public void update(ShainBean shainbean) {
            this.updateCallCount++;
            this.update_bean_arg = shainbean;
            db.put(shainbean.getId(), shainbean);
        }

        @Override
        public ShainBean findById(int id) {
            return db.get(id);
        }

        @Override
        public void create(ShainBean shainBean) {
            this.createCallCount++;
            this.create_bean_arg = shainBean;
            // IDは自動採番を模倣
            int newId = db.keySet().stream().max(Integer::compare).orElse(0) + 1;
            shainBean.setId(newId);
            db.put(newId, shainBean);
        }
    }

    @BeforeEach
    void setUp() throws NamingException, SQLException, IOException {
        // テストの前に、モックのDAOを準備
        shainDaoMock = new ShainDaoMock();
        // コンストラクタインジェクションを使い、モックのDAOを注入してServiceをインスタンス化
        shainService = new ShainService(shainDaoMock);
    }

    @Test
    @DisplayName("勤続年数が正しく計算されること")
    void testGetAllShainWithLeaveInfo() throws Exception {
        // --- 実行 ---
        ArrayList<ShainBean> result = shainService.getAllShainWithLeaveInfo();

        // --- 検証 ---
        assertEquals(2, result.size());
        
        ShainBean shain1 = result.stream().filter(s -> s.getId() == TEST_EMPLOYEE_ID).findFirst().get();
        assertEquals(10, shain1.getYearsOfService(), "勤続10年の社員の勤続年数が正しく計算されていること");
        
        ShainBean shain2 = result.stream().filter(s -> s.getId() == 2).findFirst().get();
        assertEquals(0, shain2.getYearsOfService(), "勤続0年の社員の勤続年数が正しく計算されていること");
    }

    @Test
    @DisplayName("全社員情報を取得するDAOメソッドが呼ばれること")
    void testGetAllShain() throws Exception {
        // --- 実行 ---
        ArrayList<ShainBean> result = shainService.getAllShain();
        // --- 検証 ---
        assertEquals(2, result.size(), "DAOから返されたリストがそのまま返されること");
    }
    
    @Test
    @DisplayName("社員を削除するDAOメソッドが正しい引数で呼ばれること")
    void testDeleteShain() {
        try {
            // --- 実行 ---
            shainService.deleteShain(TEST_EMPLOYEE_ID);
        } catch (Exception e) {
            fail("Exception should not be thrown", e);
        }
        // --- 検証 ---
        assertEquals(1, shainDaoMock.deleteByIdCallCount, "DAOのdeleteByIdメソッドが1回呼ばれること");
        assertEquals(TEST_EMPLOYEE_ID, shainDaoMock.deleteById_id_arg, "引数の社員IDが正しいこと");
    }
    
    @Test
    @DisplayName("社員を更新するDAOメソッドが正しい引数で呼ばれること")
    void testUpdateShain() {
        ShainBean updatedBean = new ShainBean();
        updatedBean.setId(TEST_EMPLOYEE_ID);
        updatedBean.setName("更新テスト");
        try {
            // --- 実行 ---
            shainService.updateShain(updatedBean);
        } catch (Exception e) {
            fail("Exception should not be thrown", e);
        }
        // --- 検証 ---
        assertEquals(1, shainDaoMock.updateCallCount, "DAOのupdateメソッドが1回呼ばれること");
        assertEquals(updatedBean, shainDaoMock.update_bean_arg, "引数のShainBeanが正しいこと");
    }
    
    @Test
    @DisplayName("特定の社員を取得するDAOメソッドが呼ばれ、正しいBeanが返されること")
    void testGetShainBean() throws Exception {
        // --- 実行 ---
        ShainBean result = shainService.getShainBean(TEST_EMPLOYEE_ID);
        // --- 検証 ---
        assertNotNull(result);
        assertEquals(TEST_EMPLOYEE_ID, result.getId(), "指定したIDの社員情報が返されること");
        assertEquals("テスト太郎", result.getName());
    }
    
    @Test
    @DisplayName("社員を登録するDAOメソッドが正しい引数で呼ばれること")
    void testInsertShain() {
        ShainBean newBean = new ShainBean();
        newBean.setName("新人テスト");
        try {
            // --- 実行 ---
            shainService.insertShain(newBean);
        } catch (Exception e) {
            fail("Exception should not be thrown", e);
        }
        // --- 検証 ---
        assertEquals(1, shainDaoMock.createCallCount, "DAOのcreateメソッドが1回呼ばれること");
        assertEquals(newBean, shainDaoMock.create_bean_arg, "引数のShainBeanが正しいこと");
    }
}

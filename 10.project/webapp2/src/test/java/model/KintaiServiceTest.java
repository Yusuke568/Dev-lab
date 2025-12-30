package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import beans.CalendarBean;
import dao.KintaiDao;

class KintaiServiceTest {

    private KintaiService kintaiService;
    private KintaiDaoMock kintaiDaoMock;
    private LeaveServiceMock leaveServiceMock;
    private static final int TEST_EMPLOYEE_ID = 100;

    /**
     * KintaiServiceのテスト用モッククラス。
     * 外部APIへのアクセスを遮断し、固定の祝日データを返す。
     */
    class KintaiServiceForTest extends KintaiService {
        private Map<String, String> holidays;

        public KintaiServiceForTest(KintaiDao kintaiDao, LeaveService leaveService, Map<String, String> holidays) {
            super(kintaiDao, leaveService);
            this.holidays = holidays;
        }

        @Override
        public Map<String, String> fetchHolidaysFromApi(int year) {
            // APIの代わりに固定の祝日マップを返す
            return this.holidays;
        }
    }

    /**
     * KintaiDaoの振る舞いを模倣するモッククラス。
     */
    class KintaiDaoMock extends KintaiDao {
        // メソッド呼び出しを記録するフラグや、ダミーデータを格納するフィールド
        private final Map<LocalDate, CalendarBean> db = new HashMap<>();
        public int updateCallCount = 0;
        public int batchCreateCallCount = 0;

        @Override
        public List<CalendarBean> findByMonth(int id, int year, int month) {
            List<CalendarBean> result = new ArrayList<>();
            db.values().stream()
                .filter(b -> b.getId() == id && b.getKintaidate().getYear() == year && b.getKintaidate().getMonthValue() == month)
                .forEach(result::add);
            return result;
        }

        @Override
        public CalendarBean findByDate(int staffId, LocalDate date) {
            return db.values().stream()
                .filter(b -> b.getId() == staffId && b.getKintaidate().equals(date))
                .findFirst().orElse(null);
        }

        @Override
        public void update(CalendarBean bean) {
            updateCallCount++;
            // 実際のDB更新の代わりにMapを更新
            db.put(bean.getKintaidate(), bean);
        }
        
        @Override
        public void batchCreate(List<CalendarBean> beans) {
            batchCreateCallCount++;
            beans.forEach(bean -> db.put(bean.getKintaidate(), bean));
        }
        
        @Override
        public boolean exists(int id) {
            return db.values().stream().anyMatch(b -> b.getId() == id);
        }

        // テスト用のヘルパーメソッド
        public void clear() {
            db.clear();
            updateCallCount = 0;
            batchCreateCallCount = 0;
        }
        public void addBean(CalendarBean bean) {
            db.put(bean.getKintaidate(), bean);
        }
    }

    /**
     * LeaveServiceの振る舞いを模倣するモッククラス。
     */
    class LeaveServiceMock extends LeaveService {
        public int incrementCallCount = 0;
        public int decrementCallCount = 0;
        
        public LeaveServiceMock() {
            // 親クラスのDAOにアクセスしないようにnullを渡す
            super(null);
        }

        @Override
        public void incrementDays(int employeeId) {
            incrementCallCount++;
        }

        @Override
        public void decrementDays(int employeeId) {
            decrementCallCount++;
        }
        
        public void clear() {
            incrementCallCount = 0;
            decrementCallCount = 0;
        }
    }


    @BeforeEach
    void setUp() {
        kintaiDaoMock = new KintaiDaoMock();
        leaveServiceMock = new LeaveServiceMock();
        
        // テスト用の固定祝日データ
        Map<String, String> dummyHolidays = new HashMap<>();
        dummyHolidays.put("2024/01/01", "元日");
        dummyHolidays.put("2024/05/05", "こどもの日");

        // APIアクセスとDAOアクセスをモック化したServiceを使用
        kintaiService = new KintaiServiceForTest(kintaiDaoMock, leaveServiceMock, dummyHolidays);
    }

    @Test
    @DisplayName("カレンダーが正しく生成されること（日数と祝日）")
    void testGenerateCalendar() {
        int year = 2024;
        int month = 5;

        // --- 実行 ---
        List<CalendarBean> calendar = kintaiService.generateCalendar(TEST_EMPLOYEE_ID, year, month);

        // --- 検証 ---
        assertEquals(31, calendar.size(), "2024年5月の日数が正しいこと");

        // 祝日のチェック
        CalendarBean holidayBean = calendar.stream().filter(b -> b.getKintaidate().getDayOfMonth() == 5).findFirst().get();
        assertTrue(holidayBean.isIsholiday(), "5月5日が祝日として設定されていること");
        
        // 祝日でない日のチェック
        CalendarBean nonHolidayBean = calendar.stream().filter(b -> b.getKintaidate().getDayOfMonth() == 6).findFirst().get();
        assertFalse(nonHolidayBean.isIsholiday(), "5月6日が祝日でないこと");
        
        // 曜日のチェック（例：2024/5/1は水曜日）
        CalendarBean firstDay = calendar.get(0);
        assertEquals(DayOfWeek.WEDNESDAY, firstDay.getWeek(), "初日の曜日が正しいこと");
    }
    
    @Test
    @DisplayName("カレンダーが正しく生成されること（うるう年）")
    void testGenerateCalendar_LeapYear() {
        int year = 2024; // うるう年
        int month = 2;

        // --- 実行 ---
        List<CalendarBean> calendar = kintaiService.generateCalendar(TEST_EMPLOYEE_ID, year, month);

        // --- 検証 ---
        assertEquals(29, calendar.size(), "うるう年（2024年）の2月の日数が正しいこと");
        assertEquals(29, calendar.get(calendar.size() - 1).getKintaidate().getDayOfMonth(), "最終日が29日であること");
    }

    @Test
    @DisplayName("月次勤怠データを正しく取得できること")
    void testGetmonthKintai() throws SQLException, NamingException, IOException {
        // --- 準備 ---
        LocalDate date = LocalDate.of(2024, 5, 1);
        CalendarBean bean = new CalendarBean();
        bean.setId(TEST_EMPLOYEE_ID);
        bean.setKintaidate(date);
        kintaiDaoMock.addBean(bean);
        
        // --- 実行 ---
        List<CalendarBean> result = kintaiService.getmonthKintai(TEST_EMPLOYEE_ID, 2024, 5);
        
        // --- 検証 ---
        assertEquals(1, result.size());
        assertEquals(TEST_EMPLOYEE_ID, result.get(0).getId());
        assertEquals(date, result.get(0).getKintaidate());
    }

    @Test
    @DisplayName("勤怠リストの一括更新（有給への変更）")
    void testUpdateKintaiList_toPaidLeave() throws Exception {
        // --- 準備 ---
        // DBの既存データ（出勤）
        LocalDate date = LocalDate.of(2024, 5, 10);
        CalendarBean oldBean = new CalendarBean();
        oldBean.setId(TEST_EMPLOYEE_ID);
        oldBean.setKintaidate(date);
        oldBean.setTekiyoukbn("出勤");
        kintaiDaoMock.addBean(oldBean);

        // 更新データ（有給）
        List<CalendarBean> newList = new ArrayList<>();
        CalendarBean newBean = new CalendarBean();
        newBean.setId(TEST_EMPLOYEE_ID);
        newBean.setKintaidate(date);
        newBean.setTekiyoukbn("有給");
        newList.add(newBean);
        
        // --- 実行 ---
        kintaiService.updateKintaiList(newList);

        // --- 検証 ---
        assertEquals(1, kintaiDaoMock.updateCallCount, "KintaiDao.updateが1回呼び出されること");
        assertEquals(0, leaveServiceMock.incrementCallCount, "有給日数の加算は行われないこと");
        assertEquals(1, leaveServiceMock.decrementCallCount, "有給日数が1日減算されること");
    }

    @Test
    @DisplayName("勤怠リストの一括更新（有給からの変更）")
    void testUpdateKintaiList_fromPaidLeave() throws Exception {
        // --- 準備 ---
        // DBの既存データ（有給）
        LocalDate date = LocalDate.of(2024, 5, 11);
        CalendarBean oldBean = new CalendarBean();
        oldBean.setId(TEST_EMPLOYEE_ID);
        oldBean.setKintaidate(date);
        oldBean.setTekiyoukbn("有給");
        kintaiDaoMock.addBean(oldBean);
        
        // 更新データ（出勤）
        List<CalendarBean> newList = new ArrayList<>();
        CalendarBean newBean = new CalendarBean();
        newBean.setId(TEST_EMPLOYEE_ID);
        newBean.setKintaidate(date);
        newBean.setTekiyoukbn("出勤");
        newList.add(newBean);
        
        // --- 実行 ---
        kintaiService.updateKintaiList(newList);

        // --- 検証 ---
        assertEquals(1, kintaiDaoMock.updateCallCount, "KintaiDao.updateが1回呼び出されること");
        assertEquals(1, leaveServiceMock.incrementCallCount, "有給日数が1日加算されること");
        assertEquals(0, leaveServiceMock.decrementCallCount, "有給日数の減算は行われないこと");
    }
    
    @Test
    @DisplayName("月初の勤怠情報を正しく初期化（一括登録）できること")
    void testInitializeMonthlyKintai() {
        // --- 実行 ---
        try {
            kintaiService.initializeMonthlyKintai(TEST_EMPLOYEE_ID, 2024, 5);
        } catch (Exception e) {
            fail("Exception should not be thrown", e);
        }
        
        // --- 検証 ---
        assertEquals(1, kintaiDaoMock.batchCreateCallCount, "KintaiDao.batchCreateが1回呼び出されること");
        // 5月は31日なので31件登録されているはず
        assertEquals(31, kintaiDaoMock.findByMonth(TEST_EMPLOYEE_ID, 2024, 5).size(), "生成された日数分のデータが登録されていること");
    }
}

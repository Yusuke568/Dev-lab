package com.example.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import com.example.entity.CalendarDay;
import com.example.service.transaction.TransactionManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import dao.KintaiDao;
import com.example.infra.ConnectionBase;

public class KintaiService {

    private final KintaiDao kintaiDao;
    private final LeaveService leaveService;
    private final TransactionManager transactionManager;

    public KintaiService() {
        this(new KintaiDao(), new LeaveService(), new TransactionManager());
    }
    
    public KintaiService(KintaiDao kintaiDao, LeaveService leaveService, TransactionManager transactionManager) {
        this.kintaiDao = kintaiDao;
        this.leaveService = leaveService;
        this.transactionManager = transactionManager;
    }

    /**
     * 勤怠リストを一括で更新します。有給の増減も考慮します。
     */
    public void updateKintaiList(List<CalendarDay> newKintaiList) throws SQLException, NamingException, IOException {
        transactionManager.execute(con -> {
            for (CalendarDay newBean : newKintaiList) {
                // 古い状態を取得するために、同じトランザクション内で読み取り
                CalendarDay oldBean = kintaiDao.findByDate(con, newBean.getId(), newBean.getKintaidate());

                String oldStatus = (oldBean != null && oldBean.getTekiyoukbn() != null) ? oldBean.getTekiyoukbn() : "";
                String newStatus = (newBean.getTekiyoukbn() != null) ? newBean.getTekiyoukbn() : "";

                // Case 1: Status changed TO "Paid Leave"
                if (!oldStatus.equals("有給") && newStatus.equals("有給")) {
                    leaveService.decrementDays(con, newBean.getId());
                }
                // Case 2: Status changed FROM "Paid Leave"
                else if (oldStatus.equals("有給") && !newStatus.equals("有給")) {
                    leaveService.incrementDays(con, newBean.getId());
                }
                kintaiDao.update(con, newBean);
            }
            return null; // 戻り値なし
        });
    }

    /**
     * APIより年の祝日リストを取得します。
     */
    public Map<String, String> fetchHolidaysFromApi(int year) {
        Map<String, String> holidays = new HashMap<>();
        String apiUrl = "https://holidays-jp.github.io/api/v1/date.json";
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, String> allHolidays = mapper.readValue(response.body(), new TypeReference<>() {});
                for (Map.Entry<String, String> entry : allHolidays.entrySet()) {
                    if (entry.getKey().startsWith(String.valueOf(year))) {
                        holidays.put(entry.getKey().replace("-", "/"), entry.getValue());
                    }
                }
            } else {
                System.err.println("祝日APIの取得に失敗しました。ステータス: " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return holidays;
    }

    /**
     * 指定された年月のカレンダーを生成します。祝日情報も付与します。
     */
    public List<CalendarDay> generateCalendar(int id, int year, int month) {
        LocalDate date = LocalDate.of(year, month, 1);
        Map<String, String> holidays = fetchHolidaysFromApi(year);
        List<CalendarDay> calendarList = new ArrayList<>();
        while (date.getMonthValue() == month) {
            CalendarDay calendarDay = new CalendarDay();
            calendarDay.setId(id);
            calendarDay.setKintaidate(date);
            calendarDay.setWeek(date.getDayOfWeek());
            String formattedDate = String.format("%d/%02d/%02d", date.getYear(), date.getMonthValue(), date.getDayOfMonth());
            calendarDay.setIsholiday(holidays.containsKey(formattedDate));
            calendarList.add(calendarDay);
            date = date.plusDays(1);
        }
        return calendarList;
    }
    
    /**
     * 指定した社員の月初の勤怠情報を初期化（カレンダー生成とDBへの一括登録）します。
     */
    public void initializeMonthlyKintai(int id, int year, int month) throws SQLException, NamingException, IOException {
        List<CalendarDay> calendarList = generateCalendar(id, year, month);
        transactionManager.execute(con -> {
            kintaiDao.batchCreate(con, calendarList);
            return null;
        });
    }
    
    // --- DAOへの委譲メソッド (読み取り系) ---

    public boolean isStaffidwork(int id) throws SQLException, NamingException, IOException {
        try (Connection con = ConnectionBase.getConnection()) {
            return kintaiDao.exists(con, id);
        }
    }

    public List<CalendarDay> getmonthKintai(int id, int year, int month)
            throws SQLException, NamingException, IOException {
        try (Connection con = ConnectionBase.getConnection()) {
            return kintaiDao.findByMonth(con, id, year, month);
        }
    }
    
    public CalendarDay getKintaiDay(int staffId, LocalDate date) throws SQLException, NamingException, IOException {
        try (Connection con = ConnectionBase.getConnection()) {
            return kintaiDao.findByDate(con, staffId, date);
        }
    }
}

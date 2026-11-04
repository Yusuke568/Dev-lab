package com.example.notice.application.service;

import com.example.notice.domain.model.Notice;
import com.example.notice.domain.model.NoticeDepartment;
import com.example.notice.domain.port.out.NoticePort;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NoticeServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 5);

    /** 上長 300 は 開発部(1) とその下の 開発1課(2) の承認者。営業部(3) は配下ではない */
    private final List<Object[]> inserted = new ArrayList<>();
    private final List<int[]> deleted = new ArrayList<>();
    private LocalDate askedToday;

    private final NoticePort port = new NoticePort() {
        @Override
        public List<Notice> findVisibleFor(int staffId, LocalDate today) {
            askedToday = today;
            return List.of();
        }

        @Override
        public List<Notice> findByAuthor(int authorStaffId) {
            return List.of();
        }

        @Override
        public List<NoticeDepartment> findManagedDepartments(int approverStaffId) {
            return approverStaffId == 300
                    ? List.of(new NoticeDepartment(1, "開発部"), new NoticeDepartment(2, "開発1課"))
                    : List.of();
        }

        @Override
        public void insert(int authorStaffId, int departmentId, String body, LocalDate expiresOn) {
            inserted.add(new Object[] {authorStaffId, departmentId, body, expiresOn});
        }

        @Override
        public boolean delete(int noticeId, int authorStaffId) {
            deleted.add(new int[] {noticeId, authorStaffId});
            return noticeId == 10 && authorStaffId == 300;
        }
    };

    private final NoticeService service = new NoticeService(port,
            Clock.fixed(Instant.parse("2026-11-05T01:00:00Z"), ZoneId.of("Asia/Tokyo")));

    @Test
    void 配下の部署にお知らせを出せる_本文の前後の空白は除く() {
        service.post("300", 2, "  今月の作業報告書は\n11/30までに提出してください。 ", LocalDate.of(2026, 11, 30));

        assertEquals(1, inserted.size());
        assertEquals(300, inserted.get(0)[0]);
        assertEquals(2, inserted.get(0)[1]);
        assertEquals("今月の作業報告書は\n11/30までに提出してください。", inserted.get(0)[2]);
    }

    @Test
    void 配下でない部署には出せない() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.post("300", 3, "お知らせ", null));
        assertTrue(e.getMessage().contains("この部署にはお知らせを出せません"));

        assertThrows(IllegalArgumentException.class, () -> service.post("126", 1, "お知らせ", null),
                "承認者になっていない人は出せない");
        assertTrue(inserted.isEmpty());
    }

    @Test
    void 本文が空や長すぎるときは出せない() {
        assertThrows(IllegalArgumentException.class, () -> service.post("300", 1, "   ", null));
        assertThrows(IllegalArgumentException.class, () -> service.post("300", 1, "あ".repeat(501), null));
        service.post("300", 1, "あ".repeat(500), null);
        assertEquals(1, inserted.size());
    }

    @Test
    void 掲載期限は今日以降() {
        assertThrows(IllegalArgumentException.class, () -> service.post("300", 1, "お知らせ", TODAY.minusDays(1)));
        service.post("300", 1, "お知らせ", TODAY);
        assertEquals(1, inserted.size());
    }

    @Test
    void 自分が出したお知らせだけ取り下げられる() {
        service.delete("300", 10);
        assertThrows(IllegalArgumentException.class, () -> service.delete("126", 10));
        assertEquals(2, deleted.size());
    }

    @Test
    void 表示するお知らせは今日の日付で探す() {
        service.getNoticesFor("126");
        assertEquals(TODAY, askedToday);
    }
}

package com.example.approval.domain.service;

import com.example.master.domain.model.Department;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApprovalRouteBuilderTest {

    private final ApprovalRouteBuilder builder = new ApprovalRouteBuilder();

    /** 本部(承認者100) → 開発部(承認者200) → 開発1課(承認者300) */
    private Map<Integer, Department> org() {
        Map<Integer, Department> map = new HashMap<>();
        map.put(1, new Department(1, "本部", null, 100, 0));
        map.put(2, new Department(2, "開発部", 1, 200, 0));
        map.put(3, new Department(3, "開発1課", 2, 300, 0));
        return map;
    }

    @Test
    void 所属部署から上位部署へ段数分たどる() {
        assertEquals(List.of(300, 200), builder.build(1, 3, org(), 2));
        assertEquals(List.of(300, 200, 100), builder.build(1, 3, org(), 3));
    }

    @Test
    void 段数が組織の深さより多くても最上位で止まる() {
        assertEquals(List.of(300, 200, 100), builder.build(1, 3, org(), 10));
    }

    @Test
    void 承認者が未設定の部署は飛ばす() {
        Map<Integer, Department> map = org();
        map.put(2, new Department(2, "開発部", 1, null, 0));
        assertEquals(List.of(300, 100), builder.build(1, 3, map, 2));
    }

    @Test
    void 申請者本人が承認者の部署は飛ばす() {
        // 課長(300)本人の申請は、部長(200)→本部長(100)へ
        assertEquals(List.of(200, 100), builder.build(300, 3, org(), 2));
    }

    @Test
    void 同じ人が複数部署の承認者なら1回だけ並べる() {
        Map<Integer, Department> map = org();
        map.put(2, new Department(2, "開発部", 1, 300, 0));
        assertEquals(List.of(300, 100), builder.build(1, 3, map, 2));
    }

    @Test
    void 部署が輪になっていても止まる() {
        Map<Integer, Department> map = new HashMap<>();
        map.put(1, new Department(1, "A", 2, 100, 0));
        map.put(2, new Department(2, "B", 1, 200, 0));
        assertEquals(List.of(100, 200), builder.build(1, 1, map, 5));
    }

    @Test
    void 所属部署が無いと申請できない() {
        assertThrows(IllegalStateException.class, () -> builder.build(1, null, org(), 2));
    }

    @Test
    void 最上位の承認者本人の申請は空のルート_自動承認() {
        // 本部長(100)本人の申請：本部より上がないので、ほかに承認者がいない
        assertEquals(List.of(), builder.build(100, 1, org(), 2));
        // 課長(300)が課も部も本部も兼ねている場合も同じ
        Map<Integer, Department> map = new HashMap<>();
        map.put(1, new Department(1, "本部", null, 300, 0));
        map.put(3, new Department(3, "開発1課", 1, 300, 0));
        assertEquals(List.of(), builder.build(300, 3, map, 2));
    }

    @Test
    void 上位に別の承認者がいれば自動承認にはしない() {
        // 部長(200)本人の申請は本部長(100)へ
        assertEquals(List.of(100), builder.build(200, 2, org(), 2));
    }

    @Test
    void 承認者が一人も見つからないと申請できない() {
        Map<Integer, Department> map = new HashMap<>();
        map.put(1, new Department(1, "本部", null, null, 0));
        assertThrows(IllegalStateException.class, () -> builder.build(1, 1, map, 2));
    }
}

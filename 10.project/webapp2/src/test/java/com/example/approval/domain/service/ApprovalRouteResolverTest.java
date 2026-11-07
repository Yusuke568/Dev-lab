package com.example.approval.domain.service;

import com.example.approval.domain.model.ApprovalRoute;
import com.example.approval.domain.model.OrganizationSnapshot;
import com.example.approval.domain.model.RequestType;
import com.example.approval.domain.model.ResolvedRoute;
import com.example.approval.domain.model.RouteAssignment;
import com.example.master.domain.model.Department;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApprovalRouteResolverTest {

    private final ApprovalRouteResolver resolver = new ApprovalRouteResolver();

    /** 本部(承認者100) → 開発部(承認者200) → 開発1課(承認者300)。営業部(承認者400) は本部の下 */
    private final Map<Integer, Department> departments = new HashMap<>(Map.of(
            1, new Department(1, "本部", null, 100, 0),
            2, new Department(2, "開発部", 1, 200, 0),
            3, new Department(3, "開発1課", 2, 300, 0),
            4, new Department(4, "営業部", 1, 400, 0)));
    private final Map<Integer, List<Integer>> subs = new HashMap<>();
    private final List<ApprovalRoute> routes = new ArrayList<>();

    private OrganizationSnapshot org(int maxLevels) {
        return new OrganizationSnapshot(departments, subs, routes, maxLevels);
    }

    private static ApprovalRoute route(int id, String name, RequestType type, List<List<Integer>> levels, RouteAssignment... to) {
        return new ApprovalRoute(id, name, type, null, levels, List.of(to));
    }

    private static RouteAssignment staff(int id) {
        return new RouteAssignment(RouteAssignment.STAFF, id, null);
    }

    private static RouteAssignment dept(int id) {
        return new RouteAssignment(RouteAssignment.DEPARTMENT, id, null);
    }

    @Test
    void 割り当てがなければ組織図で決まり_副承認者も同じ段の候補になる() {
        subs.put(3, List.of(310, 320));
        ResolvedRoute r = resolver.resolve(1, 3, RequestType.OVERTIME, org(2));

        assertEquals(List.of(List.of(300, 310, 320), List.of(200)), r.getLevels());
        assertEquals("組織図", r.getSource());
    }

    @Test
    void 主承認者本人の申請は同じ部署の副承認者に回さず上位部署へ() {
        subs.put(3, List.of(310));
        // 課長(300)本人 → 副課長(310)ではなく部長(200)→本部長(100)
        assertEquals(List.of(List.of(200), List.of(100)), resolver.resolve(300, 3, RequestType.OVERTIME, org(2)).getLevels());
        // 副課長(310)本人 → 課長(300)→部長(200)
        assertEquals(List.of(List.of(300), List.of(200)), resolver.resolve(310, 3, RequestType.OVERTIME, org(2)).getLevels());
    }

    @Test
    void 社員への割り当てが部署への割り当てより優先される() {
        routes.add(route(1, "開発部ルート", null, List.of(List.of(700)), dept(2)));
        routes.add(route(2, "個人ルート", null, List.of(List.of(800), List.of(900)), staff(1)));

        ResolvedRoute r = resolver.resolve(1, 3, RequestType.OVERTIME, org(2));

        assertEquals(List.of(List.of(800), List.of(900)), r.getLevels());
        assertTrue(r.getSource().contains("個人ルート"));
    }

    @Test
    void 部署の割り当ては下の部署にも効く() {
        routes.add(route(1, "開発部ルート", null, List.of(List.of(700, 701)), dept(2)));

        ResolvedRoute r = resolver.resolve(1, 3, RequestType.LEAVE, org(2));

        assertEquals(List.of(List.of(700, 701)), r.getLevels());
        assertTrue(r.getSource().contains("開発部"));
    }

    @Test
    void 申請種別を指定したルートが種別なしのルートより優先され_合わない種別のルートは使わない() {
        routes.add(route(1, "共通", null, List.of(List.of(700)), staff(1)));
        routes.add(route(2, "休暇用", RequestType.LEAVE, List.of(List.of(800)), staff(1)));

        assertEquals(List.of(List.of(800)), resolver.resolve(1, 3, RequestType.LEAVE, org(2)).getLevels());
        assertEquals(List.of(List.of(700)), resolver.resolve(1, 3, RequestType.OVERTIME, org(2)).getLevels());
    }

    @Test
    void 兼任部署を選ぶとその部署で決まる() {
        ResolvedRoute r = resolver.resolve(1, 4, RequestType.OVERTIME, org(2));
        assertEquals(List.of(List.of(400), List.of(100)), r.getLevels());
    }

    @Test
    void 本人と前の段に出た人を除き_空の段は飛ばす() {
        // 課長(300)本人の申請。ルートの1段目は本人と部長(200)、2段目は部長だけ → 2段目は空になり飛ばす
        routes.add(route(1, "課長用", null, List.of(List.of(300, 200), List.of(200), List.of(100)), staff(300)));

        ResolvedRoute r = resolver.resolve(300, 3, RequestType.OVERTIME, org(2));

        assertEquals(List.of(List.of(200), List.of(100)), r.getLevels());
    }

    @Test
    void ルートの段の先頭の人本人の申請は同じ段のほかの人に回さず次の段へ() {
        // 開発部の標準ルート：1段目 課長(300)・副課長(310)、2段目 部長(200)
        routes.add(route(1, "開発部 標準", null, List.of(List.of(300, 310), List.of(200)), dept(2)));

        // 課長本人 → 副課長ではなく部長
        assertEquals(List.of(List.of(200)), resolver.resolve(300, 3, RequestType.OVERTIME, org(2)).getLevels());
        // 副課長本人 → 課長 → 部長
        assertEquals(List.of(List.of(300), List.of(200)), resolver.resolve(310, 3, RequestType.OVERTIME, org(2)).getLevels());
        // 一般社員 → 課長か副課長 → 部長
        assertEquals(List.of(List.of(300, 310), List.of(200)), resolver.resolve(1, 3, RequestType.OVERTIME, org(2)).getLevels());
    }

    @Test
    void ルートの承認者が本人だけなら自動承認() {
        routes.add(route(1, "社長", null, List.of(List.of(100)), staff(100)));

        ResolvedRoute r = resolver.resolve(100, 1, RequestType.OVERTIME, org(2));

        assertTrue(r.isAutoApproved());
        assertTrue(r.getLevels().isEmpty());
    }

    @Test
    void 承認者が誰もいないルートは申請できない() {
        departments.put(5, new Department(5, "新設部", null, null, 0));
        assertThrows(IllegalStateException.class, () -> resolver.resolve(1, 5, RequestType.OVERTIME, org(2)));
    }

    @Test
    void 部署がなくても社員にルートがあれば申請できる() {
        routes.add(route(1, "個人ルート", null, List.of(List.of(800)), staff(9)));
        assertEquals(List.of(List.of(800)), resolver.resolve(9, null, RequestType.OVERTIME, org(2)).getLevels());
        assertThrows(IllegalStateException.class, () -> resolver.resolve(10, null, RequestType.OVERTIME, org(2)));
    }

    @Test
    void 組織図は承認ルート設定の段数まで() {
        assertEquals(List.of(List.of(300)), resolver.resolve(1, 3, RequestType.OVERTIME, org(1)).getLevels());
        assertEquals(3, resolver.resolve(1, 3, RequestType.OVERTIME, org(5)).getLevels().size());
    }
}

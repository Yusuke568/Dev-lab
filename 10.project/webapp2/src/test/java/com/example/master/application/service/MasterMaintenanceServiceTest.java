package com.example.master.application.service;

import com.example.master.domain.model.Department;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MasterMaintenanceServiceTest {

    @Test
    void 部署を親の直後に子が来る順に並べて深さを付ける() {
        List<Department> ordered = MasterMaintenanceService.orderAsTree(List.of(
                new Department(3, "開発1課", 2, null, 0),
                new Department(1, "本部", null, null, 0),
                new Department(4, "営業部", 1, null, 1),
                new Department(2, "開発部", 1, null, 0)));

        assertEquals(List.of("本部", "開発部", "開発1課", "営業部"),
                ordered.stream().map(Department::getName).collect(Collectors.toList()));
        assertEquals(List.of(0, 1, 2, 1),
                ordered.stream().map(Department::getDepth).collect(Collectors.toList()));
    }

    @Test
    void 輪になった部署も一覧から消えない() {
        List<Department> ordered = MasterMaintenanceService.orderAsTree(List.of(
                new Department(1, "A", 2, null, 0),
                new Department(2, "B", 1, null, 0)));

        assertEquals(2, ordered.size());
    }
}

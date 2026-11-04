package com.example.permission;

import com.example.master.application.service.MasterMaintenanceService;
import com.example.master.domain.model.PermissionGroup;
import com.example.master.domain.port.out.MasterPort;
import com.example.permission.application.service.GetUserFeaturesService;
import com.example.permission.domain.model.Feature;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** マスタごとの権限 */
class MasterPermissionTest {

    private static final Set<Feature> ALL_MASTERS = EnumSet.of(Feature.MASTER_PERMISSION_GROUP, Feature.MASTER_JOB_CLASS,
            Feature.MASTER_DEPARTMENT, Feature.MASTER_APPROVAL_ROUTE, Feature.MASTER_ABSTRACT,
            Feature.MASTER_LEAVE_RULE, Feature.MASTER_CORRECTION, Feature.MASTER_COMPANY_HOLIDAY);

    @Test
    void マスタ管理はマスタごとの機能に分かれる() {
        assertEquals(ALL_MASTERS, Feature.masterFeatures());
        assertEquals("マスタ管理（組織図）", Feature.MASTER_DEPARTMENT.getLabel());
        assertEquals("組織図", Feature.MASTER_DEPARTMENT.getShortLabel());
        assertEquals("承認", Feature.APPROVAL.getLabel());
    }

    @Test
    void 権限グループに付けたマスタだけを使える() {
        GetUserFeaturesService service = new GetUserFeaturesService(staffId ->
                Optional.of(Set.of("ATTENDANCE", "MASTER_DEPARTMENT", "MASTER_CORRECTION")));

        assertEquals(EnumSet.of(Feature.ATTENDANCE, Feature.MASTER_DEPARTMENT, Feature.MASTER_CORRECTION),
                service.getFeatures("126"));
    }

    @Test
    void 以前のマスタ管理はすべてのマスタとして扱う() {
        GetUserFeaturesService service = new GetUserFeaturesService(staffId ->
                Optional.of(Set.of("ATTENDANCE", "MASTER_ADMIN")));

        Set<Feature> features = service.getFeatures("126");
        assertTrue(features.containsAll(ALL_MASTERS));
        assertTrue(features.contains(Feature.ATTENDANCE));
    }

    @Test
    void 以前のマスタ管理が残るグループは画面ですべてのマスタにチェックが付く() {
        PermissionGroup legacy = new PermissionGroup(3, "管理者", Set.of("APPROVAL", "MASTER_ADMIN"));

        Map<String, Boolean> map = legacy.getFeatureMap();
        for (Feature f : ALL_MASTERS) {
            assertEquals(Boolean.TRUE, map.get(f.name()), f.name());
        }
        assertEquals(Boolean.TRUE, map.get("APPROVAL"));
    }

    // ---- 権限グループを設定できるグループが無くならないようにする ----

    /** findPermissionGroups / savePermissionGroup / replaceGroupFeatures だけを持つ MasterPort */
    private static MasterPort masterPort(Map<Integer, PermissionGroup> groups, Map<Integer, Set<String>> saved) {
        return (MasterPort) Proxy.newProxyInstance(MasterPort.class.getClassLoader(), new Class<?>[] {MasterPort.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "findPermissionGroups": return new ArrayList<>(groups.values());
                        case "savePermissionGroup": return args[0] == null ? 99 : args[0];
                        case "replaceGroupFeatures":
                            @SuppressWarnings("unchecked") Set<String> codes = (Set<String>) args[1];
                            saved.put((Integer) args[0], codes);
                            return null;
                        default: throw new UnsupportedOperationException(method.getName());
                    }
                });
    }

    @Test
    void 権限グループを設定できる最後のグループからは外せない() {
        Map<Integer, PermissionGroup> groups = new HashMap<>();
        groups.put(1, new PermissionGroup(1, "一般", Set.of("ATTENDANCE")));
        groups.put(3, new PermissionGroup(3, "管理者", Set.of("MASTER_PERMISSION_GROUP", "MASTER_DEPARTMENT")));
        Map<Integer, Set<String>> saved = new HashMap<>();
        MasterMaintenanceService service = new MasterMaintenanceService(masterPort(groups, saved));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.savePermissionGroup(3, "管理者", new LinkedHashSet<>(List.of("MASTER_DEPARTMENT"))));

        assertTrue(e.getMessage().contains("マスタ管理（権限グループ）"));
        assertTrue(saved.isEmpty(), "保存していない");
    }

    @Test
    void ほかのグループが権限グループを設定できれば外せる() {
        Map<Integer, PermissionGroup> groups = new HashMap<>();
        groups.put(3, new PermissionGroup(3, "管理者", Set.of("MASTER_PERMISSION_GROUP")));
        groups.put(4, new PermissionGroup(4, "システム管理", Set.of("MASTER_PERMISSION_GROUP")));
        Map<Integer, Set<String>> saved = new HashMap<>();
        MasterMaintenanceService service = new MasterMaintenanceService(masterPort(groups, saved));

        service.savePermissionGroup(3, "管理者", new LinkedHashSet<>(List.of("MASTER_DEPARTMENT", "MASTER_CORRECTION")));

        assertEquals(Set.of("MASTER_DEPARTMENT", "MASTER_CORRECTION"), saved.get(3));
    }
}

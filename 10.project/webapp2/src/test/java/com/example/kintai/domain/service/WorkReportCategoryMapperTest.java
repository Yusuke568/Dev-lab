package com.example.kintai.domain.service;

import com.example.entity.WorkType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorkReportCategoryMapperTest {

    /** 作業報告書の様式（AX14:AX22）の選択肢そのまま */
    private static final List<String> OPTIONS = List.of("出勤", "休出", "有休", "代休", "半休", "特休", "振出", "振休", "そ他");

    /** サンプルデータの適用マスタ */
    private static final List<WorkType> WORK_TYPES = List.of(
            workType(1, "出勤", false), workType(10, "休日", false), workType(2, "休出", false),
            workType(3, "有給", true), workType(4, "代休", false), workType(5, "半休", false),
            workType(6, "特休", false), workType(7, "振出", false), workType(8, "振休", false),
            workType(9, "その他", false));

    private static WorkType workType(int id, String name, boolean paid) {
        WorkType wt = new WorkType();
        wt.setId(id);
        wt.setName(name);
        wt.setPaid(paid);
        return wt;
    }

    private static Optional<String> category(String name) {
        return WORK_TYPES.stream().filter(wt -> wt.getName().equals(name)).findFirst()
                .flatMap(wt -> WorkReportCategoryMapper.toCategory(wt, OPTIONS));
    }

    @Test
    void 様式にある名前はそのまま摘要に書く() {
        for (String name : List.of("出勤", "休出", "代休", "半休", "特休", "振出", "振休")) {
            assertEquals(Optional.of(name), category(name), name);
        }
    }

    @Test
    void 表記の違う区分は様式の選択肢に読み替える() {
        assertEquals(Optional.of("有休"), category("有給"));
        assertEquals(Optional.of("そ他"), category("その他"));
    }

    @Test
    void 休日と未設定は空欄にする() {
        assertEquals(Optional.empty(), category("休日"));
        assertEquals(Optional.empty(), WorkReportCategoryMapper.toCategory(null, OPTIONS));
    }

    @Test
    void 様式の選択肢にない区分も名前をそのまま印字する() {
        assertEquals(Optional.of("欠勤"), WorkReportCategoryMapper.toCategory(workType(12, "欠勤", false), OPTIONS));
    }

    @Test
    void 名前が違っても有給扱いの区分は有休にする() {
        assertEquals(Optional.of("有休"), WorkReportCategoryMapper.toCategory(workType(11, "年次有給休暇", true), OPTIONS));
    }

    @Test
    void 取込では摘要の値を勤務区分に戻す() {
        assertEquals(4, WorkReportCategoryMapper.toWorkTypeId("代休", WORK_TYPES));
        assertEquals(3, WorkReportCategoryMapper.toWorkTypeId("有休", WORK_TYPES));
        assertEquals(9, WorkReportCategoryMapper.toWorkTypeId("そ他", WORK_TYPES));
        assertEquals(2, WorkReportCategoryMapper.toWorkTypeId(" 休出 ", WORK_TYPES));
    }

    @Test
    void 取込で空欄や知らない値は出勤にする() {
        assertEquals(1, WorkReportCategoryMapper.toWorkTypeId("", WORK_TYPES));
        assertEquals(1, WorkReportCategoryMapper.toWorkTypeId(null, WORK_TYPES));
        assertEquals(1, WorkReportCategoryMapper.toWorkTypeId("不明", WORK_TYPES));
    }

    @Test
    void 出力して取り込むと元の勤務区分に戻る() {
        for (WorkType wt : WORK_TYPES) {
            Optional<String> exported = WorkReportCategoryMapper.toCategory(wt, OPTIONS);
            if (exported.isPresent()) {
                assertEquals(wt.getId(), WorkReportCategoryMapper.toWorkTypeId(exported.get(), WORK_TYPES), wt.getName());
            }
        }
    }
}

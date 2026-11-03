package com.example.kintai.domain.service;

import com.example.entity.WorkType;

import java.util.List;
import java.util.Optional;

/**
 * 勤務区分（適用マスタ）と、作業報告書Excelの「摘要」欄（AN列）の値を対応づける。
 *
 * - 勤務区分が未設定の日と「休日」の日は空欄にする
 * - それ以外は印字する。様式のプルダウン（AX14:AX22）と表記が違うものは選択肢に読み替え
 *   （有給 → 有休、その他 → 様式の「そ他」）、選択肢にないものは勤務区分名をそのまま書く
 */
public final class WorkReportCategoryMapper {

    /** 様式の「有給休暇」の表記 */
    static final String CATEGORY_PAID_LEAVE = "有休";
    private static final String WORK_TYPE_PAID_LEAVE = "有給";
    private static final String WORK_TYPE_OTHER = "その他";
    private static final String WORK_TYPE_DEFAULT = "出勤";
    private static final String WORK_TYPE_HOLIDAY = "休日";

    private WorkReportCategoryMapper() {
    }

    /**
     * 勤務区分を、摘要欄に書く値にする。未設定（null）と休日は空。
     *
     * @param options 様式のプルダウンの選択肢
     */
    public static Optional<String> toCategory(WorkType workType, List<String> options) {
        if (workType == null || workType.getName() == null || workType.getName().isBlank() || isHoliday(workType)) {
            return Optional.empty();
        }
        List<String> choices = options == null ? List.of() : options;
        String name = workType.getName().trim();
        if (choices.contains(name)) {
            return Optional.of(name);
        }
        if ((workType.isPaid() || WORK_TYPE_PAID_LEAVE.equals(name)) && choices.contains(CATEGORY_PAID_LEAVE)) {
            return Optional.of(CATEGORY_PAID_LEAVE);
        }
        if (WORK_TYPE_OTHER.equals(name)) {
            // 様式の選択肢は「そ他」と書かれているため、「他」で終わる選択肢を「その他」とみなす
            Optional<String> other = choices.stream().filter(o -> o.endsWith("他")).findFirst();
            if (other.isPresent()) {
                return other;
            }
        }
        // 様式の選択肢にない勤務区分も、名前をそのまま印字する
        return Optional.of(name);
    }

    /** 「休日」（法定休日で労働しない区分）か。摘要欄には印字しない。 */
    private static boolean isHoliday(WorkType workType) {
        return WORK_TYPE_HOLIDAY.equals(workType.getName().trim()) || (workType.isLegalHoliday() && !workType.isWork());
    }

    /**
     * 摘要欄の値から勤務区分IDを決める（取込用）。空欄や読み替えられない値は「出勤」とする
     * （時刻のない空欄の日を未設定にするかどうかは呼び出し側で決める）。
     */
    public static Integer toWorkTypeId(String category, List<WorkType> workTypes) {
        String value = category == null ? "" : category.trim();
        if (!value.isEmpty()) {
            for (WorkType wt : workTypes) {
                if (value.equals(wt.getName())) {
                    return wt.getId();
                }
            }
            if (CATEGORY_PAID_LEAVE.equals(value)) {
                Integer paid = findId(workTypes, wt -> wt.isPaid() || WORK_TYPE_PAID_LEAVE.equals(wt.getName()));
                if (paid != null) {
                    return paid;
                }
            }
            if (value.endsWith("他")) {
                Integer other = findId(workTypes, wt -> WORK_TYPE_OTHER.equals(wt.getName()));
                if (other != null) {
                    return other;
                }
            }
        }
        return findId(workTypes, wt -> WORK_TYPE_DEFAULT.equals(wt.getName()));
    }

    private static Integer findId(List<WorkType> workTypes, java.util.function.Predicate<WorkType> condition) {
        return workTypes.stream().filter(condition).map(WorkType::getId).findFirst().orElse(null);
    }
}

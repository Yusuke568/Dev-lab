package com.example.approval.domain.model;

import java.util.List;

/**
 * 社員が申請したらどの承認ルートになるか（承認設定画面の一覧用）。
 *
 * @param levels       段ごとの承認者（自動承認・エラーのときは空）
 * @param source       決まり方（例: 「組織図」「承認ルート『派遣先A』（社員に割り当て）」）
 * @param autoApproved 上位の承認者がいないため自動承認になる
 * @param error        ルートが決まらない理由（決まれば null）
 */
public record RoutePreview(List<List<Integer>> levels, String source, boolean autoApproved, String error) {
}

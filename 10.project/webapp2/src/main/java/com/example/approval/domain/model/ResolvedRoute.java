package com.example.approval.domain.model;

import java.util.Collections;
import java.util.List;

/**
 * 決まった承認ルート。
 *
 * levels は段ごとの承認者の候補（申請者本人は除いてある）。
 * autoApproved が true のときは levels が空で、申請者が最上位の承認者のため自動承認にする。
 * source はどこから決まったかの説明（画面の確認用。例: 「承認ルート『開発部ルート』（部署 開発部 に割り当て）」）。
 */
public class ResolvedRoute {

    private final List<List<Integer>> levels;
    private final boolean autoApproved;
    private final String source;

    public ResolvedRoute(List<List<Integer>> levels, boolean autoApproved, String source) {
        this.levels = levels;
        this.autoApproved = autoApproved;
        this.source = source;
    }

    public List<List<Integer>> getLevels() { return Collections.unmodifiableList(levels); }
    public boolean isAutoApproved() { return autoApproved; }
    public String getSource() { return source; }
}

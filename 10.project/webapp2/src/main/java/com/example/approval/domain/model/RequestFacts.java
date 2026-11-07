package com.example.approval.domain.model;

/**
 * 承認ルートの「条件つきで足す段」を決めるための、申請の中身。
 * 条件を見ないとき（マスタ画面のふだんのルート表示、休暇申請など）は null を渡す。
 *
 * @param monthlyOvertimeMinutes 当月の時間外（実績＋今回の申請の予定）の分数
 * @param night                  申請の予定が深夜（22:00〜翌5:00）にかかるか
 */
public record RequestFacts(int monthlyOvertimeMinutes, boolean night) {
}

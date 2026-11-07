<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<title>勤怠管理システム</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
<style>
/* Additional styles for kintai page */
.kintai-header {
	display: flex;
	justify-content: space-between;
	align-items: flex-end;
	flex-wrap: wrap;
	margin-bottom: 1.5rem;
}
.stat-card {
	background: var(--card-background);
	padding: 1rem 1.5rem;
	border-radius: var(--border-radius);
	box-shadow: var(--card-shadow);
	text-align: center;
}
.stat-card .label {
	font-size: 0.9rem;
	font-weight: 700;
	color: #6c757d;
}
.stat-card .value {
	font-size: 1.8rem;
	font-weight: 800;
	color: var(--primary-color);
}
.actions-card {
	display: flex;
	flex-wrap: wrap;
	justify-content: space-between;
	align-items: center;
	padding: 1.5rem;
	gap: 1rem;
}
.table .form-input {
	padding: 6px 8px; /* Smaller padding for inputs inside table */
	font-size: 0.9rem;
}

.status-area {
    display: flex;
    flex-direction: column;
    gap: 8px;
    margin-bottom: 1rem;
}

.status-banner {
    width: 100%;
    box-sizing: border-box; /* 余白を幅に含め、スマホで画面からはみ出さないようにする */
    padding: 12px 20px;
    border-radius: 6px;
    font-size: 1.05rem;
    font-weight: 700;
    display: flex;
    align-items: center;
    gap: 10px;
}

.status-banner .icon {
    font-size: 1.3rem;
}

.status-banner .msg {
    flex: 1;
}

.status-info {
    background-color: #e7f3ff;
    color: #0b63c5;
}

.status-warning {
    background-color: #fff4e5;
    color: #c56a00;
}

.status-danger {
    background-color: #ffe5e5;
    color: #c50000;
}

.law-check-card {
    background: #ffffff;
    padding: 1rem 1.5rem;
    border-radius: 8px;
    box-shadow: var(--card-shadow);
    margin-bottom: 1.5rem;
}

.law-check-card h2 {
    font-size: 1.2rem;
    font-weight: 800;
    margin-bottom: 0.8rem;
}

.law-check-table {
    width: 100%;
    border-collapse: collapse;
    font-size: 0.9rem;
}

.law-check-table th {
    width: 30%;
    text-align: left;
    padding: 6px;
    background: #f5f5f5;
    border-bottom: 1px solid #ddd;
}

.law-check-table td {
    padding: 6px;
    border-bottom: 1px solid #ddd;
}

.law-check-notes {
    margin-top: 1rem;
    font-size: 0.8rem;
    color: #555;
    line-height: 1.4;
}

.info-grid {
    display: grid;
    grid-template-columns: 1fr 1fr 1fr;
    gap: 20px;
    margin-bottom: 1.5rem;
}

.law-check-card,
.worktype-card {
    background: #ffffff;
    padding: 1rem 1.5rem;
    border-radius: 8px;
    box-shadow: var(--card-shadow);
}

.status-card {
    background: #ffffff;
    padding: 1rem 1.5rem;
    border-radius: 8px;
    box-shadow: var(--card-shadow);
    max-height: 250px;
    overflow-y: auto;
    position: sticky;
    top: 20px;
}

.status-card h2 {
    font-size: 1.2rem;
    font-weight: 800;
    margin-bottom: 0.8rem;
}

.status-item {
    padding: 8px 10px;
    border-radius: 4px;
    font-size: 0.85rem;
    font-weight: 700;
    margin-bottom: 6px;
}

.status-success {
    background: #e8f8e8;
    color: #1a7f1a;
}

.info-section {
    background: #f0f7ff;
    padding: 20px;
    border-radius: 10px;
    margin-bottom: 20px;
    border: 1px solid #d0e4ff;
    display: none;
}

.info-toggle {
    font-size: 1.1rem;
    font-weight: 800;
    cursor: pointer;
    margin-bottom: 8px;
    color: #0b63c5;
    user-select: none;
}

.info-toggle:hover {
    opacity: 0.7;
}

.status-wide {
    grid-column: 1 / 4;
}

.header-actions {
    display: flex;
    gap: 1rem;
    align-items: center;
    flex-wrap: wrap;
}

/* --- 有給残日数 --- */
.stat-card .unit {
    font-size: 1rem;
    margin-left: 2px;
}
.paid-leave-note {
    font-size: 0.8rem;
    color: #c05621;
    font-weight: 700;
}
.paid-leave-note:empty {
    display: none;
}
#paid-leave-card.short .value,
#paid-leave-card.short .paid-leave-note {
    color: #c50000;
}

/* --- 勤務入力の注意事項 --- */
.rules-card {
    grid-column: 1 / -1;
    background: #ffffff;
    padding: 1rem 1.5rem;
    border-radius: 8px;
    box-shadow: var(--card-shadow);
}
.rules-card h2 {
    font-size: 1.2rem;
    font-weight: 800;
    margin-bottom: 0.6rem;
}
.rules-list {
    margin: 0;
    padding-left: 0;
    list-style: none;
    font-size: 0.9rem;
    line-height: 1.7;
}
.rules-important {
    color: #c50000;
    font-weight: 700;
}

/* --- 勤務区分一覧（参照用なので小さく） --- */
.worktype-card h2 {
    font-size: 1rem;
    margin-bottom: 0.5rem;
}
.worktype-list {
    display: flex;
    flex-wrap: wrap;
    gap: 4px 6px;
    margin: 0;
    padding: 0;
    list-style: none;
    font-size: 0.75rem;
}
.worktype-list li {
    padding: 2px 8px;
    border: 1px solid #e2e8f0;
    border-radius: 999px;
    background: #f7fafc;
    white-space: nowrap;
}
.worktype-list .worktype-id {
    color: #718096;
    margin-right: 4px;
}

/* --- 上長からのお知らせ（ステータス通知とは別の枠） --- */
.notice-card {
    background: #ffffff;
    border: 2px solid #2b6cb0;
    border-radius: 10px;
    padding: 12px 16px;
    margin-bottom: 16px;
    max-height: 320px; /* 件数が多いときは枠の中でスクロール */
    overflow-y: auto;
}
.notice-card h2 {
    font-size: 1.1rem;
    font-weight: 800;
    color: #2b6cb0;
    margin: 0 0 8px;
}
.notice-card .notice-count {
    display: inline-block;
    font-size: 0.8rem;
    color: #ffffff;
    background: #2b6cb0;
    border-radius: 999px;
    padding: 1px 8px;
    vertical-align: middle;
}
.notice-item {
    padding: 8px 10px;
    border-left: 4px solid #2b6cb0;
    background: #ebf4ff;
    border-radius: 4px;
    margin-bottom: 6px;
    font-size: 0.9rem;
}
.notice-item .notice-meta {
    font-size: 0.75rem;
    color: #4a5568;
    margin-bottom: 2px;
}
.notice-item .notice-body {
    white-space: pre-wrap;
    word-break: break-word;
}

/* --- 祝日 --- */
.holiday-name {
    display: block;
    font-size: 0.75rem;
    font-weight: 700;
    color: #c53030;
    white-space: nowrap;
}

/* --- 日をまたいだ退勤の印 --- */
.next-day-badge {
    display: inline-block;
    margin-left: 4px;
    padding: 1px 6px;
    border-radius: 10px;
    background: #ebf4ff;
    color: #1a3d6d;
    font-size: 0.75rem;
    font-weight: 700;
    vertical-align: middle;
}
.next-day-badge[hidden] {
    display: none;
}

/* --- 今日の打刻 --- */
.today-panel {
    flex: 1 1 260px;
}
.today-panel .today-label {
    font-weight: 800;
    color: #1a3d6d;
}
.today-panel .today-times {
    font-size: 1.1rem;
    margin: 4px 0;
}
.today-panel .today-times strong {
    font-size: 1.4rem;
    font-variant-numeric: tabular-nums;
}
.today-message {
    font-size: 0.9rem;
    color: #1a7f1a;
    min-height: 1.4em;
}
.today-message.error {
    color: #c50000;
    font-weight: 700;
}
.clock-buttons {
    display: flex;
    gap: 0.5rem;
    flex-wrap: wrap;
}
.clock-buttons .btn {
    min-width: 7em;
    padding: 12px 20px;
    font-size: 1.05rem;
}

/* --- 画面下に固定する保存バー --- */
.save-bar {
    position: sticky;
    bottom: 0;
    z-index: 20;
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 0.5rem;
    margin-top: 1rem;
    padding: 10px 12px;
    background: #ffffff;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.08);
}
.save-bar .dirty-indicator {
    font-size: 0.9rem;
    color: #718096;
}
.save-bar.dirty {
    border-color: #f6ad55;
    background: #fffaf0;
}
.save-bar.dirty .dirty-indicator {
    color: #c05621;
    font-weight: 700;
}
.save-bar .save-buttons {
    display: flex;
    gap: 0.5rem;
}
.save-bar .save-buttons .btn {
    margin: 0;
}
.table-kintai input[type="time"].form-input {
    min-width: 7em;
}

/* --- 週次作業報告（作業報告書の週ブロックと同じ並び） --- */
.weekly-report-block {
    background: #f8fafc;
    margin-bottom: 1rem;
}
.wwr-title {
    margin-top: 0;
}
.wwr-grid {
    display: grid;
    grid-template-columns: minmax(0, 2fr) minmax(0, 1fr);
    gap: 1rem 1.5rem;
}
/* 作業件名／作業進捗：作業報告書と同じく、左右の同じ行を並べる */
.wwr-sheet-head,
.wwr-row {
    display: grid;
    grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
    gap: 0 1rem;
}
.wwr-row {
    margin-bottom: 4px;
}
.wwr-row .form-input {
    padding: 6px 8px;
}
.wwr-cell-label {
    display: none; /* PC では上の列見出しで分かるので、各欄の見出しは出さない */
}
.wwr-cell-label-always {
    display: block;
    font-size: 0.85rem;
    font-weight: 600;
    color: #4a5568;
}
.wwr-sheet-note {
    margin-top: 0.5rem;
}
.wwr-heading {
    margin: 0 0 0.5rem;
    padding-bottom: 4px;
    border-bottom: 2px solid #e2e8f0;
    font-size: 0.95rem;
    color: #1a3d6d;
}
.wwr-limit {
    font-size: 0.8rem;
    font-weight: 400;
    color: #718096;
}
.weekly-report-block textarea.wwr-text {
    width: 100%;
    max-width: calc(20em + 30px); /* 1行20文字が収まる幅 */
    resize: vertical;
    line-height: 1.5;
}
.wwr-counter {
    font-size: 0.8rem;
    color: #718096;
    margin-top: 2px;
}
.wwr-counter.over {
    color: #c50000;
    font-weight: 700;
}
.weekly-report-block textarea.over {
    border-color: #e53e3e;
}
.wwr-prefixed {
    display: flex;
    align-items: center;
    gap: 4px;
}
.wwr-prefix {
    white-space: nowrap;
    font-size: 0.9rem;
}
.wwr-summary {
    background: #ffffff;
    border: 1px dashed #cbd5e0;
    border-radius: 4px;
    padding: 8px 10px;
    font-size: 0.9rem;
    font-variant-numeric: tabular-nums;
}
.wwr-summary-indent {
    padding-left: 1em;
}

/* --- 上部の「その他の操作」 --- */
.more-actions > summary {
    list-style: none;
}
.more-actions > summary::-webkit-details-marker {
    display: none;
}
.more-actions > summary::after {
    content: " ▾";
}
.more-actions[open] > summary::after {
    content: " ▴";
}
.more-actions.always-open > summary {
    display: none;
}
.more-actions-menu {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
    align-items: center;
}

/* --- 一括入力の選択 --- */
.bulk-select {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 0.5rem 1rem;
    margin-bottom: 0.75rem;
}
.bulk-select .radio-line {
    margin-bottom: 0;
}
.bulk-count {
    font-size: 0.9rem;
    color: #4a5568;
}

/* --- 保存結果などのメッセージ（alert の代わり） --- */
.inline-message {
    font-size: 0.9rem;
}
.inline-message:empty {
    display: none;
}
.inline-message.success {
    color: #1a7f1a;
    font-weight: 700;
}
.inline-message.error {
    color: #c50000;
    font-weight: 700;
}
.save-status {
    flex: 1 1 200px;
}
.table-kintai tr.row-error {
    box-shadow: inset 0 0 0 3px #e53e3e;
}

/* --- スマートフォン対応 --- */
@media (max-width: 768px) {
    .more-actions {
        flex: 1 1 100%;
    }
    .more-actions > summary {
        display: block;
        margin: 2px;
    }
    .more-actions-menu {
        flex-direction: column;
        align-items: stretch;
        margin-top: 0.5rem;
    }
    .more-actions-menu .btn,
    .more-actions-menu form,
    .more-actions-menu form .btn {
        display: block;
        width: 100%;
        box-sizing: border-box;
        margin: 0;
    }
    .wwr-grid {
        grid-template-columns: 1fr;
    }
    /* スマホでは左右を縦に並べ、各欄に見出しを出す */
    .wwr-sheet-head {
        display: none;
    }
    .wwr-row {
        grid-template-columns: 1fr;
        margin-bottom: 10px;
        padding-bottom: 6px;
        border-bottom: 1px dashed #e2e8f0;
    }
    .wwr-cell {
        margin-bottom: 4px;
    }
    .wwr-cell-label {
        display: block;
        font-size: 0.8rem;
        font-weight: 600;
        color: #4a5568;
    }
    /* 週次作業報告：PC 向けの固定幅を、スマホでは画面幅に合わせる */
    .weekly-report-block .form-input {
        width: 100% !important;
        max-width: 100%;
        box-sizing: border-box;
    }
    .weekly-report-block textarea.form-input {
        resize: vertical !important;
    }
    .weekly-report-block .form-group {
        flex: 1 1 100%;
    }
    /* iPhone は 16px 未満の入力欄をタップすると画面を拡大するので、16px にそろえる */
    .form-input,
    .table .form-input {
        font-size: 16px;
    }
    .save-bar .save-buttons {
        flex: 1 1 100%;
    }
    .save-bar .save-buttons .btn {
        flex: 1 1 0;
        padding: 12px 8px;
    }
    .clock-buttons .btn {
        flex: 1 1 0;
    }
    body {
        padding: 12px;
    }
    .card {
        padding: 15px;
    }
    .kintai-header {
        align-items: stretch;
    }
    .header-actions {
        justify-content: flex-start;
    }
    .header-actions .btn {
        flex: 1 1 auto;
        margin: 2px;
    }
    .actions-card {
        flex-wrap: wrap;
    }
    .info-grid {
        grid-template-columns: 1fr;
    }
    .status-card {
        position: static;
        max-height: none;
    }
    .month-nav .btn {
        padding: 8px 14px;
    }

    /* 勤務情報テーブルをカード形式に変換 */
    .table-responsive {
        overflow-x: visible;
        border: none;
    }
    .table-kintai thead {
        display: none;
    }
    .table-kintai, .table-kintai tbody, .table-kintai tr, .table-kintai td {
        display: block;
        width: 100%;
        box-sizing: border-box; /* 余白と枠線を幅に含め、カードからはみ出さないようにする */
    }
    .table-kintai tr {
        margin-bottom: 12px;
        border: 1px solid #e2e8f0;
        border-radius: 8px;
        padding: 6px 0;
        background: #ffffff;
    }
    .table-kintai td {
        border: none;
        border-bottom: 1px solid #f0f2f5;
        text-align: right;
        padding: 8px 12px 8px 42%;
        position: relative;
        min-height: 1.4em;
    }
    .table-kintai td:last-child {
        border-bottom: none;
    }
    .table-kintai td[data-label]::before {
        content: attr(data-label);
        position: absolute;
        left: 12px;
        width: 38%;
        text-align: left;
        font-weight: 700;
        color: #4a5568;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }
    .table-kintai td.detail-col {
        display: none;
    }
    .table-kintai tr.expanded td.detail-col {
        display: block;
    }
    .table-kintai td.toggle-col {
        text-align: center;
        padding: 10px 12px;
    }
    .mobile-only {
        display: block;
    }
    .table-kintai .form-input {
        width: 60%;
        display: inline-block;
    }

    /* 一括入力：出勤・退勤を横並び、勤務区分・補正CDとボタンは1行ずつ */
    .bulk-input > div {
        flex: 1 1 calc(50% - 0.5rem);
        min-width: 0;
    }
    .bulk-input > div:nth-child(n+3),
    .bulk-input > .btn {
        flex-basis: 100%;
    }
    /* iPhone の時刻入力が空のときに潰れたり重なったりしないようにする */
    input[type="time"].form-input {
        -webkit-appearance: none;
        appearance: none;
        min-height: 2.75em;
        min-width: 0;
    }
}

/* ============================================================
   カレンダー表示（resources/js/kintai/month-calendar.js）
   body.kintai-calendar-view のときは表の代わりにカレンダーを出す
   ============================================================ */
.view-switch {
    display: inline-flex;
    margin: 0 0 0.75rem;
    border: 1px solid #cbd5e0;
    border-radius: 8px;
    overflow: hidden;
}
.view-switch button {
    border: 0;
    background: #ffffff;
    color: #1a3d6d;
    padding: 8px 16px;
    font-size: 0.9rem;
    font-weight: 700;
    cursor: pointer;
}
.view-switch button + button {
    border-left: 1px solid #cbd5e0;
}
.view-switch button[aria-pressed="true"] {
    background: #1a3d6d;
    color: #ffffff;
}
.mc-toolbar {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    margin-bottom: 0.75rem;
}
.mc-toolbar .view-switch {
    margin: 0;
}
.mc-bulk-btn {
    display: none;
}
body.kintai-calendar-view .mc-bulk-btn {
    display: inline-flex;
}
/* カレンダー表示では、一括入力はカレンダーの「一括入力」ボタンから開く画面で行う */
body.kintai-calendar-view .bulk-card {
    display: none;
}
.month-calendar {
    display: none;
    margin-bottom: 0.5rem;
}
#mc-message:empty {
    display: none;
}

/* 一括入力：なぞって日付を選ぶカレンダー */
.bk-hint {
    font-size: 0.78rem;
    color: #4a5568;
    margin-top: 2px;
}
.bk-select-bar {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 6px;
    margin-bottom: 8px;
}
.bk-select-bar .btn {
    padding: 4px 12px;
    font-size: 0.8rem;
}
.bk-count {
    margin-left: auto;
    font-size: 0.85rem;
    font-weight: 700;
    color: #1a3d6d;
}
.bk-grid {
    /* なぞっている間に画面がスクロールしないようにする */
    touch-action: none;
    user-select: none;
    -webkit-user-select: none;
    margin-bottom: 12px;
}
.bk-day {
    height: 40px;
    border: 1px solid #e2e8f0;
    border-radius: 6px;
    background: #ffffff;
    color: #2d3748;
    font: inherit;
    font-size: 0.9rem;
    font-weight: 700;
    cursor: pointer;
    background-color: #ffffff !important;
}
.bk-day.sunday { color: #c53030; }
.bk-day.saturday { color: #2b6cb0; }
.bk-day.selected {
    background-color: #1a3d6d !important;
    border-color: #1a3d6d;
    color: #ffffff;
}
.bk-day:focus-visible {
    outline: 3px solid #3182ce;
    outline-offset: 1px;
}
.bk-fields {
    margin-top: 4px;
}
body.kintai-calendar-view .month-calendar {
    display: block;
}
body.kintai-calendar-view .table-responsive {
    display: none;
}
.mc-week, .mc-grid {
    display: grid;
    grid-template-columns: repeat(7, minmax(0, 1fr));
    gap: 4px;
}
.mc-dow {
    text-align: center;
    font-size: 0.8rem;
    font-weight: 700;
    padding: 4px 0;
    background: transparent !important;
    color: #4a5568;
}
.mc-dow.sunday { color: #c53030; }
.mc-dow.saturday { color: #2b6cb0; }
.mc-day {
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: stretch;
    gap: 1px;
    min-height: 76px;
    padding: 4px 3px 5px;
    border: 1px solid #e2e8f0;
    border-radius: 6px;
    background: #ffffff;
    color: #2d3748;
    font: inherit;
    text-align: left;
    cursor: pointer;
    overflow: hidden;
}
.mc-day:hover {
    border-color: #90cdf4;
}
.mc-day:focus-visible {
    outline: 3px solid #3182ce;
    outline-offset: 1px;
}
.mc-day.filled {
    border-color: #cbd5e0;
}
.mc-day.dirty {
    border-color: #dd6b20;
    border-style: dashed;
}
.mc-day.error {
    border: 2px solid #c50000;
}
.mc-date {
    font-size: 0.85rem;
    font-weight: 800;
    line-height: 1.2;
}
.mc-day.sunday .mc-date, .mc-day.holiday .mc-date { color: #c53030; }
.mc-day.saturday .mc-date { color: #2b6cb0; }
.mc-type {
    display: block;
    font-size: 0.68rem;
    font-weight: 700;
    line-height: 1.3;
    padding: 0 3px;
    border-radius: 3px;
    background: #ebf4ff;
    color: #1a3d6d;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}
.mc-holiday {
    display: block;
    font-size: 0.62rem;
    font-weight: 700;
    line-height: 1.2;
    color: #c53030;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}
.mc-time {
    display: block;
    font-size: 0.72rem;
    line-height: 1.25;
    font-variant-numeric: tabular-nums;
    color: #2d3748;
    white-space: nowrap;
}
.mc-memo {
    margin-left: 2px;
    font-size: 0.65rem;
    font-weight: 400;
    color: #718096;
}
.mc-dot {
    display: inline-block;
    width: 8px;
    height: 8px;
    border-radius: 50%;
    vertical-align: middle;
}
.mc-day > .mc-dot {
    position: absolute;
    top: 5px;
    right: 4px;
}
.mc-dot.dirty { background: #dd6b20; }
.mc-dot.ng { background: #c50000; }
.mc-dot.wait { background: #d69e2e; }
/* --- 時間外と残業事前申請（カレンダーで目立たせる） --- */
.mc-ot {
    display: block;
    margin-top: 2px;
    padding: 1px 2px;
    border-radius: 3px;
    font-size: 0.64rem;
    font-weight: 700;
    line-height: 1.25;
    text-align: center;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}
.mc-ot b {
    display: block;
    font-size: 0.66rem;
}
.mc-ot.ng { background: #c50000; color: #ffffff; }
.mc-ot.wait { background: #fefcbf; color: #744210; border: 1px solid #d69e2e; }
.mc-ot.ok { background: #e6fffa; color: #22543d; border: 1px solid #38a169; }
.mc-day.ot-ng {
    border: 2px solid #c50000;
    background-color: #fff5f5 !important;
}
.mc-day.ot-wait {
    border: 2px solid #d69e2e;
}
.mc-ot-key {
    display: inline-block;
    margin-right: 4px;
    padding: 0 4px;
    border-radius: 3px;
    font-size: 0.68rem;
}
.mc-ot-key.ng { background: #c50000; color: #ffffff; }
.mc-ot-key.wait { background: #fefcbf; color: #744210; border: 1px solid #d69e2e; }
.mc-ot-key.ok { background: #e6fffa; color: #22543d; border: 1px solid #38a169; }
.mc-ot-alert {
    margin-bottom: 8px;
    padding: 10px 12px;
    border: 2px solid #c50000;
    border-radius: 8px;
    background: #fff5f5;
    color: #742a2a;
}
.mc-ot-alert[hidden] {
    display: none;
}
.mc-ot-alert-title {
    font-size: 0.9rem;
    font-weight: 800;
    margin-bottom: 6px;
}
.mc-ot-alert-list {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
}
.mc-ot-alert-list button {
    min-height: 36px;
    padding: 4px 10px;
    border: 1px solid #c50000;
    border-radius: 999px;
    background: #ffffff;
    color: #c50000;
    font: inherit;
    font-size: 0.82rem;
    font-weight: 700;
    cursor: pointer;
}
.dd-ot-alert {
    margin-bottom: 8px;
    padding: 8px 10px;
    border-left: 4px solid #c50000;
    border-radius: 4px;
    background: #fff5f5;
    color: #742a2a;
    font-weight: 700;
}
.mc-legend {
    display: flex;
    flex-wrap: wrap;
    gap: 4px 12px;
    margin-top: 8px;
    font-size: 0.75rem;
    color: #4a5568;
}
.mc-legend .mc-dot {
    margin-right: 4px;
}
.mc-legend-hint {
    color: #718096;
}

/* 入力ポップアップ（スマホでは画面下から出す） */
.day-dialog {
    width: min(480px, calc(100vw - 24px));
    max-height: calc(100dvh - 24px);
    padding: 0;
    border: 0;
    border-radius: 12px;
    box-shadow: 0 20px 50px rgba(0, 0, 0, 0.3);
    color: #2d3748;
}
.day-dialog::backdrop {
    background: rgba(26, 32, 44, 0.45);
}
.dd-form {
    display: flex;
    flex-direction: column;
    max-height: calc(100dvh - 24px);
}
.dd-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 12px 8px;
    border-bottom: 1px solid #e2e8f0;
}
.dd-title-wrap {
    flex: 1;
    text-align: center;
}
.dd-title {
    margin: 0;
    font-size: 1.15rem;
    font-weight: 800;
}
.dd-title.dd-sun { color: #c53030; }
.dd-title.dd-sat { color: #2b6cb0; }
.dd-holiday {
    font-size: 0.8rem;
    font-weight: 700;
    color: #c53030;
}
.dd-holiday:empty { display: none; }
.dd-nav {
    width: 44px;
    height: 44px;
    border: 1px solid #cbd5e0;
    border-radius: 50%;
    background: #ffffff;
    color: #1a3d6d;
    font-size: 1.4rem;
    line-height: 1;
    cursor: pointer;
}
.dd-nav:disabled {
    opacity: 0.35;
    cursor: default;
}
.dd-body {
    padding: 12px 16px;
    overflow-y: auto;
}
.dd-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 10px;
}
.dd-row label, .dd-label {
    display: block;
    font-size: 0.85rem;
    font-weight: 700;
    color: #4a5568;
    margin-bottom: 8px;
}
.dd-row .form-input, .dd-label .form-input {
    display: block;
    width: 100%;
    margin-top: 4px;
    font-size: 1rem;
}
.dd-field-note {
    align-self: end;
    margin: 0 0 8px;
    font-size: 0.72rem;
    color: #718096;
}
.dd-quick {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    margin-bottom: 6px;
}
.dd-quick .btn {
    padding: 4px 10px;
    font-size: 0.8rem;
}
.dd-worktime {
    font-size: 0.85rem;
    color: #1a3d6d;
    font-weight: 700;
    margin-bottom: 10px;
}
.dd-worktime:empty { display: none; }
.dd-more {
    margin: 4px 0 10px;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    padding: 8px 10px;
}
.dd-more summary {
    font-size: 0.85rem;
    font-weight: 700;
    color: #1a3d6d;
    cursor: pointer;
}
.dd-more[open] summary {
    margin-bottom: 8px;
}
.dd-memo-row {
    display: flex;
    gap: 6px;
}
.dd-memo-row .form-input {
    flex: 1;
    min-width: 0;
}
.dd-template {
    padding: 4px 8px;
    font-size: 0.8rem;
}
.dd-status {
    font-size: 0.85rem;
    margin-bottom: 8px;
}
.dd-status:empty { display: none; }
.dd-status-label {
    display: inline-block;
    margin-right: 6px;
    color: #4a5568;
    font-weight: 700;
}
.dd-check {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 0.85rem;
    color: #4a5568;
}
.dd-actions {
    display: flex;
    gap: 8px;
    padding: 10px 16px 4px;
    border-top: 1px solid #e2e8f0;
}
.dd-actions .btn {
    flex: 1;
    min-height: 44px;
}
.dd-note {
    margin: 0;
    padding: 0 16px 12px;
    font-size: 0.72rem;
    color: #718096;
}
@media (max-width: 768px) {
    .mc-week, .mc-grid {
        gap: 2px;
    }
    .mc-day {
        min-height: 68px;
        padding: 3px 2px 4px;
        border-radius: 4px;
    }
    .mc-time {
        font-size: 0.66rem;
        letter-spacing: -0.02em;
    }
    .mc-type {
        font-size: 0.62rem;
        padding: 0 2px;
    }
    /* 「未申請」など3文字が狭いマスに収まるようにする */
    .mc-ot {
        padding: 1px 0;
        letter-spacing: -0.06em;
    }
    .mc-ot b {
        font-size: 0.6rem;
    }
    .day-dialog {
        width: 100vw;
        max-width: 100vw;
        margin: auto 0 0;
        border-radius: 14px 14px 0 0;
    }
    .dd-form {
        max-height: 88dvh;
    }
}
</style>
<%@ include file="_icons.jspf" %>
</head>
<body data-context-path="${pageContext.request.contextPath}">

	<!-- ▼ ステータスエリア ▼ -->
	<div class="status-area">
	    <c:if test="${attendanceData.totalWorkMinutes > 9600}">
		    <div class="status-banner status-warning">
		        <span class="icon">⚠️</span>
		        <span class="msg">36協定：時間外労働が上限に近づいています（目安160H超）</span>
		    </div>
	    </c:if>
	    <c:if test="${proxyMode}">
		    <div class="status-banner status-warning">
		        <span class="icon">👀</span>
		        <span class="msg">
		            <c:out value="${attendanceData.employeeName}" />さん（社員ID <c:out value="${staffId}" />）の勤怠表を表示しています。
		            保存すると、この社員の勤怠として記録されます。
		            <c:url value="/subordinateAttendance.do" var="subordinateUrl">
		                <c:param name="ym" value="${attendanceData.yearMonth}" />
		            </c:url>
		            <a href="${subordinateUrl}">部下の一覧に戻る</a>
		        </span>
		    </div>
	    </c:if>
	    <c:if test="${not empty kintaiSuccessMessage}">
		    <div class="status-banner status-success" style="background-color:#e8f8e8;color:#1a7f1a;">
		        <span class="icon">✔️</span>
		        <span class="msg"><c:out value="${kintaiSuccessMessage}" /></span>
		    </div>
	    </c:if>
	    <c:if test="${not empty kintaiErrorMessage}">
		    <div class="status-banner status-danger">
		        <span class="icon">⚠️</span>
		        <span class="msg"><c:out value="${kintaiErrorMessage}" /></span>
		    </div>
	    </c:if>
	</div>

	<div id="kintai-app-root" class="card" style="max-width: 98%; margin: 0 auto;"
		data-staff-id="${staffId}"
		data-proxy-mode="${proxyMode ? 'true' : 'false'}"
		data-work-types-json='${workTypesJson}'
		data-break-periods='${breakPeriodsJson}'
		data-year="${attendanceData.yearMonth.year}"
		data-month="${attendanceData.yearMonth.monthValue}">
		<div class="kintai-header">
			<div>
				<h1>勤怠管理</h1>
				<p class="user-info" style="margin: 0; font-size: 1.2rem; font-weight: 700;">
					<c:out value="${attendanceData.employeeName}" />
					様 |
					<c:out value="${attendanceData.yearMonth.year}" />
					年
					<c:out value="${attendanceData.yearMonth.monthValue}" />
					月
				</p>

				<div class="month-nav" style="display: flex; align-items: center; gap: 0.75rem; margin-top: 0.5rem;">
					<a class="btn btn-secondary" style="padding: 4px 12px;"
						href="${pageContext.request.contextPath}/kintaiDisplay.do?id=${staffId}&year=${prevYear}&month=${prevMonth}">◀ 前月</a>
					<a class="btn btn-secondary" style="padding: 4px 12px;"
						href="${pageContext.request.contextPath}/kintaiDisplay.do?id=${staffId}&year=${nextYear}&month=${nextMonth}">次月 ▶</a>
				</div>

				<div class="info-toggle" onclick="toggleInfo()">
				    <span id="info-arrow">▼</span> 勤務情報一覧
				</div>
			</div>
			
			<div class="header-actions">
				<div class="stat-card">
					<div class="label">合計勤務時間</div>
					<div class="value">
						<c:out value="${attendanceData.totalWorkHours}" />
					</div>
				</div>
				<c:if test="${not empty attendanceData.paidLeaveDays}">
					<%-- 有給の残日数。保存していない「有給」の変更があれば、保存後の日数も出す（kintai.js） --%>
					<div class="stat-card" id="paid-leave-card" data-saved-days="${attendanceData.paidLeaveDays}">
						<div class="label">有給残日数</div>
						<div class="value"><span id="paid-leave-days"><c:out value="${attendanceData.paidLeaveDaysLabel}" /></span><span class="unit">日</span></div>
						<div class="paid-leave-note" id="paid-leave-note" role="status"></div>
					</div>
				</c:if>
				<a href="${pageContext.request.contextPath}/menu.do" class="btn btn-secondary">メニュー</a>
				<c:if test="${sessionScope.can.OVERTIME_REQUEST and not proxyMode}">
					<a href="${pageContext.request.contextPath}/overtimeRequestMenu.do" class="btn btn-secondary">🕐 残業事前申請</a>
				</c:if>
				<c:if test="${proxyMode}">
					<a href="${subordinateUrl}" class="btn btn-secondary">👥 部下の一覧</a>
				</c:if>
				<%-- スマホでは使う機会の少ない操作を「その他の操作」にまとめる。PC では最初から開いておく（下の script） --%>
				<details class="more-actions" id="more-actions">
					<summary class="btn btn-secondary">その他の操作</summary>
					<div class="more-actions-menu">
						<c:if test="${sessionScope.can.MASTER_CORRECTION}">
							<a href="${pageContext.request.contextPath}/correctionMasterList.do" class="btn btn-secondary">🔧 補正マスタ設定</a>
						</c:if>
						<c:if test="${not proxyMode}">
						<c:url value="/workReportExport.do" var="workReportExportUrl">
							<c:param name="year" value="${attendanceData.yearMonth.year}" />
							<c:param name="month" value="${attendanceData.yearMonth.monthValue}" />
						</c:url>
						<a href="${workReportExportUrl}" class="btn btn-secondary">📊 作業報告書(Excel)出力</a>
						<form id="work-report-import-form" action="${pageContext.request.contextPath}/workReportImport.do"
							method="post" enctype="multipart/form-data" style="display: inline;">
							<input type="file" id="work-report-import-file" name="workReportFile" accept=".xlsm"
								style="display: none;" onchange="document.getElementById('work-report-import-form').submit();">
							<button type="button" class="btn btn-secondary"
								onclick="document.getElementById('work-report-import-file').click();">📥 作業報告書(Excel)取込</button>
						</form>
						</c:if>
						<a href="${pageContext.request.contextPath}/passwordChange.do" class="btn btn-secondary">パスワード変更</a>
						<a href="${pageContext.request.contextPath}/logout.do" class="btn btn-secondary">ログアウト</a>
					</div>
				</details>
			</div>
		</div>

		<%-- 上長からのお知らせ。見落とさないよう、ステータス通知とは別の枠で常に表示する --%>
		<c:if test="${not empty notices}">
			<section class="notice-card" id="notice-list" aria-labelledby="notice-title">
				<h2 id="notice-title">📢 上長からのお知らせ <span class="notice-count"><c:out value="${fn:length(notices)}" />件</span></h2>
				<c:forEach var="n" items="${notices}">
					<div class="notice-item">
						<div class="notice-meta"><c:out value="${n.authorName}" />（<c:out value="${n.departmentName}" />あて）<c:out value="${n.createdAtLabel}" /><c:if test="${not empty n.expiresOn}">　<c:out value="${n.expiresOn}" />まで</c:if></div>
						<div class="notice-body"><c:out value="${n.body}" /></div>
					</div>
				</c:forEach>
			</section>
		</c:if>

		<div class="info-section" id="info-section">
			<div class="info-grid">
				<!-- 労働基準法チェック -->
				<div class="law-check-card">
					<h2>労働基準法に基づく制限チェック</h2>
					<table class="law-check-table">
						<tr>
							<th>当月時間外</th>
							<td><c:out value="${attendanceData.totalOvertimeFormatted}" /></td>
							<td>
								<c:choose>
									<c:when test="${attendanceData.monthlyOvertimeWithinLimit}">〇 原則45H迄</c:when>
									<c:otherwise><span style="color: red; font-weight: bold;">× 原則45H迄</span></c:otherwise>
								</c:choose>
							</td>
						</tr>
						<tr>
							<th>年間時間外</th>
							<td><c:out value="${attendanceData.annualOvertimeFormatted}" /></td>
							<td>
								<c:choose>
									<c:when test="${attendanceData.annualOvertimeWithinLimit}">〇 540H/年迄</c:when>
									<c:otherwise><span style="color: red; font-weight: bold;">× 540H/年迄</span></c:otherwise>
								</c:choose>
							</td>
						</tr>
					</table>
					<div class="law-check-notes">
						<p>※チェック結果がOKではない場合、内容を必ず確認すること。</p>
					</div>
				</div>

				<!-- 勤務区分一覧（参照用なので小さく表示） -->
				<div class="worktype-card">
					<h2>勤務区分一覧</h2>
					<ul class="worktype-list">
						<c:forEach var="opt" items="${workTypes}">
							<li><span class="worktype-id"><c:out value="${opt.id}"/></span><c:out value="${opt.name}"/></li>
						</c:forEach>
					</ul>
				</div>

				<!-- ステータス通知カード -->
				<div class="status-wrapper">
					<div class="status-card">
						<h2>ステータス通知</h2>
						<div class="status-item status-success">✔ 勤怠システム稼働中</div>
						<div class="status-item status-info">ℹ 提出期限：毎月3営業日以内</div>

						<c:if test="${not empty datesNeedingRequest and not proxyMode}">
							<div class="status-item status-danger">
								⚠ 残業の事前申請が必要な日があります（<c:out value="${fn:length(datesNeedingRequest)}" />件）：
								<c:forEach var="d" items="${datesNeedingRequest}" varStatus="loop">
									<c:url value="/overtimeRequestMenu.do" var="reqUrl"><c:param name="targetDate" value="${d}" /></c:url>
									<a href="${reqUrl}"><c:out value="${d}" /></a>${!loop.last ? ', ' : ''}
								</c:forEach>
								（クリックした日の内容で申請フォームに自動入力されます）
							</div>
						</c:if>
						<c:if test="${pendingRequestCount > 0 and not proxyMode}">
							<div class="status-item status-info">
								ℹ 残業事前申請が承認待ちです（<c:out value="${pendingRequestCount}" />件）
								<a href="${pageContext.request.contextPath}/overtimeRequestMenu.do">確認する ›</a>
							</div>
						</c:if>
						<c:if test="${rejectedRequestCount > 0 and not proxyMode}">
							<div class="status-item status-warning">
								⚠ 却下された残業事前申請があります（<c:out value="${rejectedRequestCount}" />件）
								<a href="${pageContext.request.contextPath}/overtimeRequestMenu.do">再申請する ›</a>
							</div>
						</c:if>
					</div>
				</div>

				<!-- 勤務入力の注意事項（作業報告書の注記と同じ内容） -->
				<div class="rules-card">
					<h2>勤務入力の注意事項</h2>
					<ul class="rules-list">
						<li>※1. チェック結果がOKではない場合、その内容を必ず確認すること。</li>
						<li>※2. 開始・終了時刻は、総勤務時間の開始・終了時刻を記入する。</li>
						<li>※3. 勤務時間（直接）は、各プロジェクトでの作業時間（時間外を含む）。</li>
						<li>※4. 勤務時間（間接）は、社内会議・教育全般の時間。</li>
						<li>※ 次の時間帯は休憩時間（勤務時間から自動で引きます）：<br>
							5:00〜5:30、8:30〜9:00、12:00〜13:00、18:00〜18:30、22:00〜22:30、2:00〜3:00</li>
						<li class="rules-important">※ 18:30以降の稼働は事前申請が必要。</li>
						<li class="rules-important">※ 22:00以降の稼働は原則禁止。</li>
					</ul>
				</div>
			</div>
		</div>

		<div class="card actions-card">
			<c:if test="${not proxyMode}">
			<div class="today-panel">
				<div class="today-label">今日 <span id="today-date"></span></div>
				<div class="today-times" id="today-times">出勤 <strong id="today-start">--:--</strong> ／ 退勤 <strong id="today-end">--:--</strong></div>
				<div class="today-message" id="today-message" role="status"></div>
			</div>
			<div class="clock-buttons">
				<button id="clock-in-btn" onclick="recordAttendance('出勤')"
					class="btn btn-primary">☀️ 出勤</button>
				<button id="clock-out-btn" onclick="recordAttendance('退勤')"
					class="btn btn-primary">🌙 退勤</button>
				<button id="today-jump-btn" type="button" onclick="scrollToToday()"
					class="btn btn-secondary">📍 今日の行へ</button>
			</div>
			</c:if>
			<div id="overtime-summary">時間外労働: <c:out value="${attendanceData.totalOvertimeFormatted}" />（保存済みデータに基づく。未保存の変更は反映されません）</div>
		</div>

		<%-- カレンダー表示のときは隠し、カレンダーの「一括入力」ボタンから開く画面で入力する（month-calendar.js） --%>
		<div class="card bulk-card" id="bulk-card" style="margin-bottom: 1rem;">
			<h4>一括入力</h4>
			<%-- スマホでは表の見出し（全選択のチェック）が隠れるので、ここでも選べるようにする --%>
			<div class="bulk-select">
				<label class="radio-line"><input type="checkbox" id="bulk-select-all" onchange="toggleAllRows(this)"> すべての日を選択</label>
				<button type="button" class="btn btn-secondary" onclick="selectWeekdays()">平日だけ選択</button>
				<span class="bulk-count" id="bulk-selected-count">0日選択中</span>
			</div>
			<div class="bulk-input" style="display: flex; gap: 1rem; align-items: flex-end; flex-wrap: wrap;">
				<div>
					<label class="form-label">出勤</label>
					<input type="time" id="bulk-start" class="form-input">
				</div>
				<div>
					<label class="form-label">退勤</label>
					<input type="time" id="bulk-end" class="form-input">
				</div>
				<div>
					<label class="form-label">勤務区分</label>
					<select id="bulk-status" class="form-input">
						<option value="">(変更なし)</option>
						<option value="unset">未設定にする</option>
						<c:forEach var="opt" items="${workTypes}">
							<option value="${opt.id}"><c:out value="${opt.name}" /></option>
						</c:forEach>
					</select>
				</div>
				<div>
					<label class="form-label" for="bulk-correction">補正CD</label>
					<%-- 「なし」は補正CDを外し、補正(通)・補正(深)も空にする --%>
					<select id="bulk-correction" class="form-input">
						<option value="keep">(変更なし)</option>
						<option value="">なし（補正を外す）</option>
						<c:forEach var="cm" items="${correctionMasters}">
							<option value="${cm.id}"><c:out value="${cm.name}" /></option>
						</c:forEach>
					</select>
				</div>
				<button class="btn btn-primary" onclick="applyBulkInput()">選択行に反映</button>
			</div>
			<div class="inline-message" id="bulk-message" role="status"></div>
		</div>

		<div class="table-responsive">
		<table id="calendar-log" class="table table-kintai">
			<thead>
				<tr>
					<th style="width: 40px; text-align: center;"><input type="checkbox" id="bulk-check-all" onclick="toggleAllRows(this)"></th>
					<th>日</th>
					<th>曜日</th>
					<th>出勤</th>
					<th>退勤</th>
					<th>時間</th>
					<th>時間外</th>
					<th>勤務区分</th>
					<th class="detail-col">備考</th>
					<th>状態</th>
					<th class="detail-col">補正CD</th>
					<th class="detail-col">補正(通)</th>
					<th class="detail-col">補正(深)</th>
					<th class="detail-col" title="出退勤時刻の外で行った社内会議・教育などの時間。総勤務時間と時間外に含めます">間接</th>
					<th class="toggle-col mobile-only">詳細</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="daily" items="${attendanceData.dailyRecords}">
					<c:set var="week" value="${daily.date.dayOfWeek.toString()}" />
					<c:set var="holidayName" value="${holidayNames[daily.date]}" />
					<tr data-date="${daily.date}" data-week="${fn:substring(week, 0, 3)}"
						class="${week == 'SATURDAY' ? 'saturday' : ''} ${week == 'SUNDAY' ? 'sunday' : ''} ${not empty holidayName ? 'holiday' : ''}">
						<td data-label="選択"><input type="checkbox" class="row-checkbox" value="${daily.date}"></td>
						<td data-label="日"><c:out value="${daily.date.dayOfMonth}" /></td>
						<td data-label="曜日">${fn:substring(week, 0, 3)}<c:if test="${not empty holidayName}"><span class="holiday-name"><c:out value="${holidayName}" /></span></c:if></td>
						<td data-label="出勤" class="editable-time"><input type="time" name="kintaifrom" class="form-input"
							value="${daily.startTime == '-' ? '' : fn:escapeXml(daily.startTime)}" aria-label="${daily.date.dayOfMonth}日の出勤"></td>
						<td data-label="退勤" class="editable-time"><input type="time" name="kintaito" class="form-input"
							value="${daily.endTime == '-' ? '' : fn:escapeXml(daily.endTime)}" aria-label="${daily.date.dayOfMonth}日の退勤">
							<%-- 退勤が出勤より前の時刻なら、日をまたいだ翌日の退勤として扱う --%>
							<span class="next-day-badge" hidden title="出勤より前の時刻なので、翌日の退勤として扱います">翌日</span></td>
						<td data-label="時間"><c:out value="${daily.workHours}" /></td>
						<td data-label="時間外">
							<c:choose>
								<c:when test="${daily.overtimeMinutes > 0}"><c:out value="${daily.overtimeMinutesFormatted}" /></c:when>
								<c:otherwise>ー</c:otherwise>
							</c:choose>
						</td>
						<td data-label="勤務区分"><select name="status" class="form-input">
								<option value="" ${empty daily.abstractId ? 'selected' : ''}>未設定</option>
								<c:forEach var="opt" items="${workTypes}">
									<option value="${opt.id}"
										${opt.id == daily.abstractId ? 'selected' : ''}>
										<c:out value="${opt.name}" />
									</option>
								</c:forEach>
						</select></td>
						<td data-label="備考" class="detail-col"><div style="display: flex; gap: 4px;">
							<input type="text" name="workDescription" value="<c:out value='${daily.workDescription}'/>" class="form-input" maxlength="500" style="flex: 1;" />
							<button type="button" class="btn btn-secondary" onclick="insertTemplate(this)" style="padding: 2px 4px; font-size: 0.8rem;">定型</button>
						</div></td>
						<td data-label="状態">
							<c:set var="isPaidLeaveDay" value="false" />
							<c:forEach var="wt" items="${workTypes}">
								<c:if test="${wt.id == daily.abstractId && wt.paid}">
									<c:set var="isPaidLeaveDay" value="true" />
								</c:if>
							</c:forEach>
							<%--
								時間外の日は「残業事前申請」の状態を出す。勤怠の承認状態（approvalStatus）は、
								事前申請のない時間外や有給を提出したときに承認者の確認待ちになる別の状態なので、
								「申請中」とは書かない。
							--%>
							<c:set var="otRequest" value="${overtimeRequestStatus[daily.date]}" />
							<c:choose>
								<c:when test="${daily.overtimeMinutes == 0 && !isPaidLeaveDay}">ー</c:when>
								<c:when test="${isPaidLeaveDay}">
									<c:choose>
										<c:when test="${daily.approvalStatus == 2}"><span style="color: green; font-weight: bold;">承認済</span></c:when>
										<c:when test="${daily.approvalStatus == 3}"><span style="color: red; font-weight: bold;">却下</span></c:when>
										<c:when test="${daily.approvalStatus == 1}"><span style="color: orange; font-weight: bold;">承認待ち</span></c:when>
										<c:otherwise><span style="color: gray;">未提出</span></c:otherwise>
									</c:choose>
								</c:when>
								<c:when test="${otRequest == 'APPROVED'}"><span style="color: green; font-weight: bold;">事前申請 承認済</span></c:when>
								<c:when test="${otRequest == 'PENDING'}"><span style="color: orange; font-weight: bold;">事前申請 申請中</span></c:when>
								<c:when test="${otRequest == 'REJECTED'}"><span style="color: red; font-weight: bold;">事前申請 却下</span></c:when>
								<c:when test="${daily.approvalStatus == 2}"><span style="color: green; font-weight: bold;">勤怠 承認済</span></c:when>
								<c:when test="${daily.approvalStatus == 3}"><span style="color: red; font-weight: bold;">勤怠 却下</span></c:when>
								<c:otherwise>
									<span style="color: #c50000; font-weight: bold;">事前申請 未申請</span>
									<c:if test="${daily.approvalStatus == 1}"><br><span style="color: #718096; font-size: 0.8rem;">勤怠は承認者の確認待ち</span></c:if>
									<c:if test="${not proxyMode}">
										<c:url value="/overtimeRequestMenu.do" var="dayRequestUrl"><c:param name="targetDate" value="${daily.date}" /></c:url>
										<br><a href="${dayRequestUrl}" style="font-size: 0.85rem;">申請する ›</a>
									</c:if>
								</c:otherwise>
							</c:choose>
						</td>
						<td data-label="補正CD" class="detail-col"><select name="correctionId" class="form-input correction-select">
								<option value="">なし</option>
								<c:forEach var="cm" items="${correctionMasters}">
									<option value="${cm.id}" data-us="${cm.correctionUsTimeFormatted}" data-mid="${cm.correctionMidTimeFormatted}"
										${cm.id == daily.correctionId ? 'selected' : ''}><c:out value="${cm.name}" /></option>
								</c:forEach>
						</select></td>
						<td data-label="補正(通)" class="detail-col"><input type="time" name="correctionUsTime"
							value="${daily.correctionUsTimeFormatted}" class="form-input" /></td>
						<td data-label="補正(深)" class="detail-col"><input type="time" name="correctionMidTime"
							value="${daily.correctionMidTimeFormatted}" class="form-input" /></td>
						<%-- 勤務時間(間接)：出退勤時刻の外で行った間接作業（社内会議・教育など）。総勤務時間と時間外に足す --%>
						<td data-label="間接" class="detail-col"><input type="time" name="indirectTime"
							value="${daily.indirectTimeFormatted}" class="form-input" aria-label="${daily.date.dayOfMonth}日の勤務時間(間接)" /></td>
						<td class="toggle-col mobile-only"><button type="button" class="btn btn-secondary" style="padding: 4px 12px; font-size: 0.8rem; width: 100%;" onclick="toggleRowDetails(this)">詳細を開く ∨</button></td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
		</div>

		<%-- 月の途中の日を直しても、すぐ保存できるよう画面の下に固定する --%>
		<div class="save-bar" id="save-bar">
			<div class="save-status">
				<div class="dirty-indicator" id="dirty-indicator" role="status"></div>
				<div class="inline-message" id="save-message" role="alert"></div>
			</div>
			<div class="save-buttons">
				<button id="temp-save-btn" onclick="saveAttendance(true)"
					class="btn btn-secondary">📝 一時保存</button>
				<button id="save-btn" onclick="saveAttendance(false)"
					class="btn btn-success">💾 保存 (提出)</button>
			</div>
		</div>
	</div>

	<div class="card" id="weekly-report-card">
		<h4>週次作業報告（木～翌水、作業報告書Excel出力に反映されます）</h4>
		<p style="font-size: 0.85rem; color: #666; margin-top: -0.5rem;">
			作業報告書Excelの週ごとの欄と同じ並びです。リーダー所見欄はExcel出力後に手入力してください。
		</p>
		<div style="display: flex; align-items: center; justify-content: center; gap: 1rem; margin-bottom: 1rem;">
			<button type="button" class="btn btn-secondary" id="wwr-prev-btn" onclick="moveWeeklyReportCard(-1)">◀ 前の週</button>
			<span id="wwr-page-indicator" style="font-weight: 700; min-width: 8em; text-align: center;"></span>
			<button type="button" class="btn btn-secondary" id="wwr-next-btn" onclick="moveWeeklyReportCard(1)">次の週 ▶</button>
		</div>
		<%--
			作業報告書Excelの週ブロック（10行）と同じ並び・同じ文字数の制限にする。
			行数のある欄は「1行20文字・最大n行」で、超えた分はExcelに出力されない。
		--%>
		<c:forEach var="week" items="${weeklyWorkReports}" varStatus="loop">
			<c:set var="summary" value="${weeklyWorkSummaries[loop.index]}" />
			<div class="card weekly-report-block" data-week-end="${week.weekEndDate}">
				<h4 class="wwr-title">第${loop.index + 1}週 <c:out value="${week.weekLabel}" /></h4>
				<div class="wwr-grid">
					<%--
						作業報告書の左2列（作業件名、工程名・作業名／作業進捗、作業内容と状況）は、
						同じ行どうしが対応する（例：工程名の行の右に「→4/4 件完了」）ので、行ごとに左右の入力欄を並べる。
					--%>
					<section class="wwr-section wwr-sheet">
						<div class="wwr-sheet-head" aria-hidden="true">
							<div class="wwr-heading">作業件名、工程名・作業名</div>
							<div class="wwr-heading">作業進捗、作業内容と状況</div>
						</div>
						<div class="wwr-row">
							<div class="wwr-cell">
								<label class="wwr-cell-label">客先名 <span class="wwr-limit">20文字まで</span></label>
								<input type="text" class="form-input wwr-client" maxlength="20" placeholder="(客先名)" aria-label="客先名"
									value="<c:out value='${week.clientName}' />">
							</div>
							<div class="wwr-cell">
								<label class="wwr-cell-label">作業進捗 <span class="wwr-limit">10文字まで</span></label>
								<div class="wwr-prefixed">
									<span class="wwr-prefix">作業進捗：</span>
									<input type="text" class="form-input wwr-status" maxlength="10" placeholder="オンスケ、〇日遅れ等" aria-label="作業進捗"
										value="<c:out value='${week.progressStatus}' />">
								</div>
							</div>
						</div>
						<div class="wwr-row">
							<div class="wwr-cell">
								<label class="wwr-cell-label">PJ名 <span class="wwr-limit">18文字まで</span></label>
								<div class="wwr-prefixed">
									<span class="wwr-prefix">・</span>
									<input type="text" class="form-input wwr-project" maxlength="18" placeholder="(PJ名)" aria-label="PJ名"
										value="<c:out value='${week.projectName}' />">
								</div>
							</div>
							<div class="wwr-cell">
								<label class="wwr-cell-label">作業内容と状況 1行目 <span class="wwr-limit">20文字まで</span></label>
								<input type="text" class="form-input wwr-line wwr-detail-line" data-group="detail" maxlength="20"
									aria-label="作業内容と状況 1行目" value="<c:out value='${week.progressDetailLines[0]}' />">
							</div>
						</div>
						<c:forEach begin="0" end="4" var="i">
							<div class="wwr-row">
								<div class="wwr-cell">
									<label class="wwr-cell-label">工程名・作業名等 ${i + 1}行目 <span class="wwr-limit">18文字まで</span></label>
									<div class="wwr-prefixed">
										<span class="wwr-prefix">　</span>
										<input type="text" class="form-input wwr-line wwr-process-line" data-group="process" maxlength="18"
											aria-label="工程名・作業名等 ${i + 1}行目" placeholder="${i == 0 ? '(工程名・作業名等)' : ''}"
											value="<c:out value='${week.processNameLines[i]}' />">
									</div>
								</div>
								<div class="wwr-cell">
									<label class="wwr-cell-label">作業内容と状況 ${i + 2}行目 <span class="wwr-limit">20文字まで</span></label>
									<input type="text" class="form-input wwr-line wwr-detail-line" data-group="detail" maxlength="20"
										aria-label="作業内容と状況 ${i + 2}行目" placeholder="${i == 0 ? '→4/4 件完了 など' : ''}"
										value="<c:out value='${week.progressDetailLines[i + 1]}' />">
								</div>
							</div>
						</c:forEach>
						<div class="wwr-row">
							<div class="wwr-cell">
								<c:if test="${not empty summary}">
									<div class="wwr-summary" aria-label="累計/残業時間（勤怠から自動計算）">
										<div><c:out value="${summary.headingLine}" /></div>
										<div class="wwr-summary-indent"><c:out value="${summary.totalsLine}" /></div>
										<div class="wwr-summary-indent"><c:out value="${summary.previousMonthLine}" /></div>
										<div class="wwr-limit">稼働/残業。勤怠から自動で計算します（入力不要）</div>
									</div>
								</c:if>
							</div>
							<div class="wwr-cell">
								<label class="wwr-cell-label wwr-cell-label-always">問題点・対処 <span class="wwr-limit">1行20文字・2行まで</span></label>
								<textarea class="form-input wwr-text wwr-problem" rows="2" data-max-lines="2" data-max-chars="20"
									aria-label="問題点・対処" placeholder="問題とその対処を記載（問題なければ「問題なし」）"><c:out value="${week.problemCountermeasure}" /></textarea>
								<div class="wwr-counter" aria-live="polite"></div>
								<div class="wwr-limit">「問題なし」のように短い1行は「問題点・対処：」と同じ行に出力します。</div>
							</div>
						</div>
						<p class="wwr-limit wwr-sheet-note">Enterで次の行へ移ります。複数行をまとめて貼り付けると下の行へ分けて入ります。左右の同じ行は、作業報告書でも同じ行に出力されます。</p>
					</section>

					<section class="wwr-section">
						<h5 class="wwr-heading">本人所感</h5>
						<div class="form-group">
							<label class="form-label">本人所感 <span class="wwr-limit">1行15文字・10列まで（15文字を超えると自動で改行します）</span></label>
							<%-- data-auto-wrap：15文字を超えた分を自動で次の列へ送る。data-line-unit：件数の数え方（列） --%>
							<textarea class="form-input wwr-text wwr-note" rows="10" data-max-lines="10" data-max-chars="15"
								data-auto-wrap="true" data-line-unit="列"><c:out value="${week.personalNote}" /></textarea>
							<div class="wwr-counter" aria-live="polite"></div>
						</div>
						<p class="wwr-limit">リーダー所見は、Excel出力後にリーダーが記入します。</p>
					</section>
				</div>
			</div>
		</c:forEach>
		<button type="button" class="btn btn-primary" onclick="saveWeeklyWorkReports()">週次報告を保存</button>
		<div class="inline-message" id="wwr-message" role="status"></div>
	</div>

	<!-- Status dropdown template for JS -->
	<select id="status-template" style="display: none;">
		<c:forEach var="opt" items="${workTypes}">
			<option value="<c:out value="${opt.id}"/>">
				<c:out value="${opt.name}" />
			</option>
		</c:forEach>
	</select>

	<script
		src="${pageContext.request.contextPath}/resources/js/kintai/kintai.js"
		type="module">
	</script>
	<%-- カレンダー表示と入力ポップアップ。kintai.js の後に読み込む --%>
	<script
		src="${pageContext.request.contextPath}/resources/js/kintai/month-calendar.js"
		type="module">
	</script>

	<script>
	function toggleInfo() {
	    const section = document.getElementById("info-section");
	    const arrow = document.getElementById("info-arrow");

	    if (section.style.display === "none" || section.style.display === "") {
	        section.style.display = "block";
	        arrow.textContent = "▲";
	    } else {
	        section.style.display = "none";
	        arrow.textContent = "▼";
	    }
	}

	/**
	 * 行数のある欄（1行20文字・最大n行）の入力状況を数えて表示する。
	 * 制限を超えていれば true を返す（超えた分は作業報告書Excelに出力されないため）。
	 */
	function updateWeeklyCounter(textarea) {
	    const maxLines = Number(textarea.dataset.maxLines);
	    const maxChars = Number(textarea.dataset.maxChars);
	    const unit = textarea.dataset.lineUnit || "行";
	    const lines = textarea.value === "" ? [] : textarea.value.split(/\r?\n/);
	    const longLines = [];
	    lines.forEach((line, i) => {
	        if (line.length > maxChars) longLines.push(i + 1);
	    });
	    const tooManyLines = lines.length > maxLines;
	    const over = tooManyLines || longLines.length > 0;
	    const counter = textarea.parentElement.querySelector(".wwr-counter");
	    if (counter) {
	        let text = lines.length + " / " + maxLines + unit;
	        if (tooManyLines) text += "：" + (maxLines + 1) + unit + "目以降はExcelに出力されません";
	        if (longLines.length > 0) text += "：" + longLines.join("・") + unit + "目が" + maxChars + "文字を超えています";
	        counter.textContent = text;
	        counter.classList.toggle("over", over);
	    }
	    textarea.classList.toggle("over", over);
	    return over;
	}

	/**
	 * 1行の文字数の上限（data-max-chars）を超えた分を、自動で次の行へ送る（改行を入れる）。
	 * 入力中の位置（カーソル）は、改行を足した後も同じ文字の後ろに保つ。
	 * 日本語入力の変換中は動かさない（変換が確定してから折り返す）。
	 */
	function autoWrapTextarea(textarea) {
	    const maxChars = Number(textarea.dataset.maxChars);
	    if (!maxChars) return;
	    const value = textarea.value;
	    const lines = value.split(/\r?\n/);
	    if (lines.every((line) => Array.from(line).length <= maxChars)) return;

	    // カーソルより前にある「改行以外の文字」の数を覚えておき、折り返し後の位置に戻す
	    const caret = textarea.selectionEnd;
	    const charsBeforeCaret = Array.from(value.slice(0, caret).replace(/\r?\n/g, "")).length;
	    const caretAtLineStart = caret > 0 && value.charAt(caret - 1) === "\n";

	    // 上限ちょうど（またはそれ以上）の行の次の行は、自動で折り返した続きとみなしてつなげ直す。
	    // こうすると、途中に文字を足したときに後ろの行も詰め直される（短い行が途中に残らない）
	    const paragraphs = [];
	    lines.forEach((line, i) => {
	        if (i > 0 && Array.from(lines[i - 1]).length >= maxChars) {
	            paragraphs[paragraphs.length - 1] += line;
	        } else {
	            paragraphs.push(line);
	        }
	    });

	    const wrapped = [];
	    paragraphs.forEach((line) => {
	        const chars = Array.from(line);
	        if (chars.length === 0) {
	            wrapped.push("");
	            return;
	        }
	        for (let i = 0; i < chars.length; i += maxChars) {
	            wrapped.push(chars.slice(i, i + maxChars).join(""));
	        }
	    });
	    const result = wrapped.join("\n");
	    textarea.value = result;

	    // 折り返し後の文字列で、同じ数の文字（改行以外）の後ろの位置を探す
	    let count = 0;
	    let position = result.length;
	    for (let i = 0; i < result.length; i++) {
	        if (count === charsBeforeCaret) {
	            position = i;
	            break;
	        }
	        if (result.charAt(i) !== "\n") count++;
	    }
	    // 改行の直後にカーソルがあった（行の先頭にいた）ときは、改行の後ろに置く
	    if (caretAtLineStart) {
	        while (position < result.length && result.charAt(position) === "\n") position++;
	    }
	    textarea.setSelectionRange(position, position);
	}

	/** 同じ週・同じ列（工程名／作業内容）の行ごとの入力欄 */
	function wwrLineInputs(input) {
	    return Array.from(input.closest(".weekly-report-block")
	        .querySelectorAll(".wwr-line[data-group='" + input.dataset.group + "']"));
	}

	/** 行ごとの入力欄の値を、改行区切りの1つの値にする（途中の空行は位置を保ち、末尾の空行は除く）。 */
	function wwrJoinLines(block, selector) {
	    const values = Array.from(block.querySelectorAll(selector)).map((input) => input.value);
	    while (values.length > 0 && values[values.length - 1].trim() === "") values.pop();
	    return values.join("\n");
	}

	document.addEventListener("DOMContentLoaded", () => {
	    // 行ごとの入力欄：Enter で次の行へ、複数行の貼り付けは下の行へ分けて入れる
	    document.querySelectorAll(".weekly-report-block .wwr-line").forEach((input) => {
	        input.addEventListener("keydown", (e) => {
	            if (e.key !== "Enter" || e.isComposing) return;
	            e.preventDefault();
	            const inputs = wwrLineInputs(input);
	            const next = inputs[inputs.indexOf(input) + 1];
	            if (next) next.focus();
	        });
	        input.addEventListener("paste", (e) => {
	            const text = (e.clipboardData || window.clipboardData).getData("text");
	            if (!/\r?\n/.test(text)) return;
	            e.preventDefault();
	            const pasted = text.replace(/\r?\n$/, "").split(/\r?\n/);
	            const inputs = wwrLineInputs(input);
	            let index = inputs.indexOf(input);
	            pasted.forEach((line) => {
	                if (index < inputs.length) {
	                    inputs[index].value = line.slice(0, Number(inputs[index].maxLength) || line.length);
	                    index++;
	                }
	            });
	            if (pasted.length > inputs.length - inputs.indexOf(input)) {
	                const message = document.getElementById("wwr-message");
	                message.className = "inline-message error";
	                message.textContent = "貼り付けた行が欄の数より多いため、入りきらない行は省きました。";
	            }
	        });
	    });

	    document.querySelectorAll(".weekly-report-block textarea.wwr-text").forEach((textarea) => {
	        const autoWrap = textarea.dataset.autoWrap === "true";
	        if (autoWrap) autoWrapTextarea(textarea);
	        updateWeeklyCounter(textarea);
	        textarea.addEventListener("input", (e) => {
	            if (autoWrap && !e.isComposing) autoWrapTextarea(textarea);
	            updateWeeklyCounter(textarea);
	        });
	        // 日本語入力は、変換を確定したときに折り返す
	        textarea.addEventListener("compositionend", () => {
	            if (autoWrap) autoWrapTextarea(textarea);
	            updateWeeklyCounter(textarea);
	        });
	    });
	});

	function saveWeeklyWorkReports() {
	    const contextPath = document.body.dataset.contextPath || "";
	    const blocks = document.querySelectorAll(".weekly-report-block");
	    const message = document.getElementById("wwr-message");

	    // 制限を超えた欄があれば、その週を表示して保存しない
	    for (let i = 0; i < blocks.length; i++) {
	        const overArea = Array.from(blocks[i].querySelectorAll("textarea.wwr-text")).find((t) => updateWeeklyCounter(t));
	        if (overArea) {
	            wwrCurrentIndex = i;
	            renderWeeklyReportCard();
	            overArea.focus();
	            message.className = "inline-message error";
	            message.textContent = "第" + (i + 1) + "週に、行数か1行の文字数の制限を超えた欄があります。直してから保存してください。";
	            return;
	        }
	    }

	    const reports = Array.from(blocks).map((block) => ({
	        weekEndDate: block.dataset.weekEnd,
	        clientName: block.querySelector(".wwr-client").value,
	        projectName: block.querySelector(".wwr-project").value,
	        processName: wwrJoinLines(block, ".wwr-process-line"),
	        progressStatus: block.querySelector(".wwr-status").value,
	        progressDetail: wwrJoinLines(block, ".wwr-detail-line"),
	        problemCountermeasure: block.querySelector(".wwr-problem").value,
	        personalNote: block.querySelector(".wwr-note").value
	    }));

	    message.className = "inline-message";
	    message.textContent = "保存しています…";
	    const staffId = document.getElementById("kintai-app-root").dataset.staffId;
	    fetch(contextPath + "/weeklyWorkReportSave.do?staffId=" + encodeURIComponent(staffId), {
	        method: "POST",
	        headers: { "Content-Type": "application/json" },
	        body: JSON.stringify(reports)
	    }).then((res) => {
	        if (!res.ok) throw new Error("save failed");
	        message.className = "inline-message success";
	        message.textContent = "週次作業報告を保存しました。";
	    }).catch(() => {
	        message.className = "inline-message error";
	        message.textContent = "週次作業報告を保存できませんでした。通信状態を確かめて、もう一度保存してください。";
	    });
	}

	// ---- 週次作業報告：カード形式（1週ずつ表示）＋左右ナビゲーション ----
	let wwrCurrentIndex = 0;

	function renderWeeklyReportCard() {
	    const blocks = document.querySelectorAll(".weekly-report-block");
	    if (blocks.length === 0) return;

	    blocks.forEach((block, i) => {
	        block.style.display = (i === wwrCurrentIndex) ? "" : "none";
	    });

	    const indicator = document.getElementById("wwr-page-indicator");
	    if (indicator) {
	        indicator.textContent = (wwrCurrentIndex + 1) + " / " + blocks.length;
	    }
	    const prevBtn = document.getElementById("wwr-prev-btn");
	    const nextBtn = document.getElementById("wwr-next-btn");
	    if (prevBtn) prevBtn.disabled = (wwrCurrentIndex === 0);
	    if (nextBtn) nextBtn.disabled = (wwrCurrentIndex === blocks.length - 1);
	}

	function moveWeeklyReportCard(delta) {
	    const blocks = document.querySelectorAll(".weekly-report-block");
	    const newIndex = wwrCurrentIndex + delta;
	    if (newIndex < 0 || newIndex >= blocks.length) return;
	    wwrCurrentIndex = newIndex;
	    renderWeeklyReportCard();
	}

	document.addEventListener("DOMContentLoaded", renderWeeklyReportCard);

	// PC では「その他の操作」を最初から開いておく（スマホでは閉じておき、上部を短くする）
	document.addEventListener("DOMContentLoaded", () => {
	    const more = document.getElementById("more-actions");
	    if (more && window.matchMedia("(min-width: 769px)").matches) {
	        more.open = true;
	        more.classList.add("always-open");
	    }
	});
	</script>
</body>
</html>

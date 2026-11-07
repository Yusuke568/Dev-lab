<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<title>承認設定</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
<style>
.as-lead { font-size: 0.9rem; color: #4a5568; }
/* ---- 状況のまとめ ---- */
.as-summary { display: grid; grid-template-columns: repeat(auto-fit, minmax(min(200px, 100%), 1fr)); gap: 10px; margin: 1rem 0; }
.as-stat { border: 1px solid #e2e8f0; border-radius: 10px; padding: 10px 14px; background: #fff; text-align: left; font: inherit; color: inherit; cursor: pointer; }
.as-stat:hover { border-color: #90cdf4; }
.as-stat .num { font-size: 1.6rem; font-weight: 800; line-height: 1.2; }
.as-stat .lbl { font-size: 0.8rem; color: #4a5568; }
.as-stat.ng { border-color: #c50000; background: #fff5f5; }
.as-stat.ng .num { color: #c50000; }
/* ---- タブ ---- */
.as-tabs { display: flex; gap: 4px; border-bottom: 2px solid #e2e8f0; margin-bottom: 1rem; overflow-x: auto; }
.as-tabs button { border: 0; background: none; padding: 10px 14px; font: inherit; font-weight: 700; color: #4a5568; cursor: pointer; white-space: nowrap; border-bottom: 3px solid transparent; margin-bottom: -2px; }
.as-tabs button[aria-selected="true"] { color: #1a3d6d; border-bottom-color: #1a3d6d; }
.as-panel[hidden] { display: none; }
/* ---- 絞り込み ---- */
.as-filters { display: flex; flex-wrap: wrap; gap: 8px 12px; align-items: flex-end; margin-bottom: 10px; }
.as-filters .form-group { margin: 0; }
.as-filters .form-input { min-width: 0; }
.as-seg { display: inline-flex; border: 1px solid #cbd5e0; border-radius: 8px; overflow: hidden; }
.as-seg button { border: 0; background: #fff; padding: 8px 12px; font: inherit; font-weight: 700; color: #1a3d6d; cursor: pointer; }
.as-seg button + button { border-left: 1px solid #cbd5e0; }
.as-seg button[aria-pressed="true"] { background: #1a3d6d; color: #fff; }
.as-count { font-size: 0.85rem; color: #4a5568; margin-left: auto; }
/* ---- 表 ---- */
.as-table { width: 100%; border-collapse: collapse; font-size: 0.9rem; }
.as-table th, .as-table td { border-bottom: 1px solid #e2e8f0; padding: 8px 10px; text-align: left; vertical-align: top; }
.as-table th { background: #f7fafc; font-size: 0.8rem; color: #4a5568; white-space: nowrap; }
.as-table tr.ng td { background: #fff5f5; }
.as-table td.as-who { min-width: 11em; }
.as-table td.as-dept { min-width: 8em; }
.as-route { margin: 0; padding: 0; list-style: none; }
.as-route li { white-space: nowrap; }
.as-route li + li::before { content: "↓ "; color: #a0aec0; }
.as-tag { display: inline-block; padding: 1px 8px; border-radius: 999px; font-size: 0.75rem; font-weight: 700; margin: 1px 2px 1px 0; white-space: nowrap; }
.as-tag.org { background: #edf2f7; color: #2d3748; }
.as-tag.ex { background: #ebf4ff; color: #1a3d6d; border: 1px solid #90cdf4; }
.as-tag.auto { background: #e6fffa; color: #22543d; }
.as-tag.ng { background: #c50000; color: #fff; }
.as-tag.sub { background: #f7fafc; color: #4a5568; border: 1px solid #e2e8f0; }
.as-tag.warn { background: #fefcbf; color: #744210; }
.as-muted { color: #a0aec0; }
.as-error { color: #c50000; font-weight: 700; font-size: 0.85rem; }
.as-indent { color: #a0aec0; }
.as-actions { white-space: nowrap; text-align: right; }
.as-actions .btn { padding: 4px 12px; font-size: 0.85rem; }
.as-toolbar { display: flex; flex-wrap: wrap; gap: 8px; justify-content: space-between; align-items: center; margin: 1rem 0 0.5rem; }
.as-toolbar h3 { margin: 0; }
.as-box { border: 1px solid #e2e8f0; border-radius: 10px; padding: 12px 14px; margin-bottom: 1rem; }
.as-levels-form { display: flex; flex-wrap: wrap; gap: 10px 16px; align-items: flex-end; }
.as-levels-form label { font-weight: 700; font-size: 0.85rem; }
.as-levels-form input { width: 5rem; }
/* ---- 編集ダイアログ ---- */
.as-dialog { width: min(560px, calc(100vw - 24px)); max-height: calc(100dvh - 24px); padding: 0; border: 0; border-radius: 12px; box-shadow: 0 20px 50px rgba(0,0,0,0.3); }
.as-dialog::backdrop { background: rgba(26, 32, 44, 0.45); }
.as-dialog form { display: flex; flex-direction: column; max-height: calc(100dvh - 24px); }
.as-dialog header { padding: 14px 16px 8px; border-bottom: 1px solid #e2e8f0; }
.as-dialog header h3 { margin: 0; font-size: 1.1rem; }
.as-dialog .body { padding: 12px 16px; overflow-y: auto; }
.as-dialog footer { display: flex; gap: 8px; padding: 10px 16px; border-top: 1px solid #e2e8f0; }
.as-dialog footer .btn { flex: 1; min-height: 44px; }
.as-field { margin-bottom: 12px; }
.as-field > label, .as-field > .lbl { display: block; font-weight: 700; font-size: 0.85rem; color: #4a5568; margin-bottom: 4px; }
.as-field .hint { font-size: 0.75rem; color: #718096; margin-top: 2px; }
.as-field .form-input { width: 100%; }
/* 検索つきチェックリスト */
.as-pick { border: 1px solid #cbd5e0; border-radius: 8px; }
.as-pick input[type="search"] { width: 100%; border: 0; border-bottom: 1px solid #e2e8f0; padding: 8px 10px; font: inherit; border-radius: 8px 8px 0 0; }
.as-pick .list { max-height: 180px; overflow-y: auto; padding: 4px 6px; }
.as-pick label { display: flex; gap: 6px; align-items: center; padding: 4px 4px; font-size: 0.9rem; border-radius: 4px; cursor: pointer; }
.as-pick label:hover { background: #f7fafc; }
.as-pick label[hidden] { display: none; }
.as-pick .foot { font-size: 0.75rem; color: #4a5568; padding: 4px 10px; border-top: 1px solid #e2e8f0; }
.as-dialog.wide { width: min(980px, calc(100vw - 24px)); }
/* 段ごとの承認者（選択済 ← 未選択） */
.as-rl { border: 1px solid #c9b99a; border-radius: 6px; overflow: hidden; }
.as-rl-row { display: grid; grid-template-columns: 5em 1fr auto; border-top: 1px solid #c9b99a; }
.as-rl-row:first-child { border-top: 0; }
.as-rl-no { background: #f3ede2; padding: 10px 8px; font-weight: 700; font-size: 0.85rem; display: flex; align-items: center; }
.as-rl-body { padding: 8px; min-width: 0; }
.as-rl-ctrl { display: flex; flex-direction: column; gap: 6px; padding: 8px 8px 8px 0; align-items: center; }
.as-rl-ctrl button { width: 30px; height: 30px; border: 1px solid #cbd5e0; border-radius: 50%; background: #fff; color: #4a5568; cursor: pointer; font-size: 0.8rem; line-height: 1; }
.as-rl-ctrl button:disabled { opacity: 0.35; cursor: default; }
.as-rl-ctrl .as-rl-del { background: #e53e3e; border-color: #e53e3e; color: #fff; }
.as-rl-add { margin-top: 6px; }
.as-dl-search { display: block; margin: 0 0 6px auto; width: min(260px, 100%); padding: 6px 8px; border: 1px solid #cbd5e0; border-radius: 6px; font: inherit; font-size: 0.85rem; }
.as-dl { display: grid; grid-template-columns: minmax(0, 2fr) auto minmax(0, 3fr); gap: 8px; align-items: center; }
.as-dl-arrow { color: #8c7b5e; font-size: 1.3rem; }
.as-dl-box { border: 1px solid #c9b99a; background: #fff; min-width: 0; }
.as-dl-head { display: flex; justify-content: space-between; gap: 8px; background: #ddd0b8; padding: 4px 8px; font-size: 0.85rem; }
.as-dl-head > span { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.as-dl-link { border: 0; background: none; padding: 0; color: #2b6cb0; cursor: pointer; font: inherit; font-size: 0.85rem; white-space: nowrap; }
.as-dl-link:hover { text-decoration: underline; }
.as-dl-split { display: grid; grid-template-columns: minmax(0, 1fr) minmax(0, 1fr); }
.as-dl-list, .as-dl-groups { list-style: none; margin: 0; padding: 2px 0; height: 150px; overflow-y: auto; }
.as-dl-groups { background: #f2f2f2; border-right: 1px solid #e2e8f0; }
.as-dl-item, .as-dl-group { display: flex; gap: 4px; align-items: flex-start; width: 100%; border: 0; background: none; padding: 3px 8px; text-align: left; font: inherit; font-size: 0.85rem; color: #2d3748; cursor: pointer; }
.as-dl-item:hover, .as-dl-group:hover { background: #ebf4ff; }
.as-dl-group[aria-pressed="true"] { background: #fff; font-weight: 700; color: #1a3d6d; }
.as-dl-icon { flex: none; font-size: 0.8rem; }
.as-dl-empty { padding: 6px 8px; font-size: 0.8rem; color: #a0aec0; }
@media (max-width: 768px) {
    /* 表をカードにする */
    .as-table thead { display: none; }
    .as-table, .as-table tbody, .as-table tr, .as-table td { display: block; width: 100%; }
    .as-table tr { border: 1px solid #e2e8f0; border-radius: 8px; margin-bottom: 8px; padding: 6px 4px; }
    .as-table td { border: 0; padding: 4px 8px; }
    .as-table td.as-who, .as-table td.as-dept { min-width: 0; }
    .as-table td[data-label]::before { content: attr(data-label); display: block; font-size: 0.72rem; color: #718096; font-weight: 700; }
    .as-actions { text-align: left; }
    .as-count { margin-left: 0; width: 100%; }
    .as-dialog { width: 100vw; max-width: 100vw; margin: auto 0 0; border-radius: 14px 14px 0 0; }
    .as-dialog form { max-height: 90dvh; }
    .as-dialog.wide { width: 100vw; }
    /* 段の編集：選択済と未選択を縦に並べる */
    .as-rl-row { grid-template-columns: 1fr auto; }
    .as-rl-no { grid-column: 1 / -1; padding: 6px 8px; }
    .as-rl-ctrl { padding: 8px 6px 8px 0; }
    .as-dl { grid-template-columns: minmax(0, 1fr); }
    .as-dl-arrow { justify-self: center; transform: rotate(90deg); }
    .as-dl-list, .as-dl-groups { height: 120px; }
    .as-dl-search { width: 100%; }
}
</style>
<%@ include file="../_icons.jspf" %>
</head>
<body>
<div class="card">
	<div class="text-center page-title-container">
		<h1>承認設定</h1>
		<p class="as-lead">残業事前申請・休暇申請を「誰が承認するか」をまとめて設定します。<br>
		ふだんは<strong>組織図</strong>（部署の承認者・副承認者を、上位部署へ決めた段数までたどる）で決まり、組織図どおりにしたくない社員・部署だけ<strong>例外ルート</strong>を割り当てます。</p>
	</div>

	<%@ include file="_header.jspf" %>

	<div id="approval-settings" aria-live="polite">
		<noscript>この画面を使うには JavaScript を有効にしてください。</noscript>
	</div>
</div>

<%-- 画面に表示するデータ（MasterApprovalSettingsAction が作る） --%>
<script type="application/json" id="settings-data">${settingsJson}</script>
<script src="${pageContext.request.contextPath}/resources/js/master/approval-settings.js"></script>
</body>
</html>

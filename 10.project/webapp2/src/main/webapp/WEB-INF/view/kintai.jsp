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
</style>
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
		            <a href="${subordinateUrl}">配下の一覧に戻る</a>
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
		data-work-types-json='${workTypesJson}'
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
				<a href="${pageContext.request.contextPath}/menu.do" class="btn btn-secondary">メニュー</a>
				<c:if test="${sessionScope.can.OVERTIME_REQUEST and not proxyMode}">
					<a href="${pageContext.request.contextPath}/overtimeRequestMenu.do" class="btn btn-secondary">🕐 残業事前申請</a>
				</c:if>
				<c:if test="${proxyMode}">
					<a href="${subordinateUrl}" class="btn btn-secondary">👥 配下の一覧</a>
				</c:if>
				<%-- スマホでは使う機会の少ない操作を「その他の操作」にまとめる。PC では最初から開いておく（下の script） --%>
				<details class="more-actions" id="more-actions">
					<summary class="btn btn-secondary">その他の操作</summary>
					<div class="more-actions-menu">
						<c:if test="${sessionScope.can.MASTER_ADMIN}">
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

				<!-- 勤務区分一覧 -->
				<div class="worktype-card">
					<h2>勤務区分一覧</h2>
					<table class="worktype-table" style="width: 100%; font-size: 0.9rem;">
						<c:forEach var="opt" items="${workTypes}">
							<tr>
								<th><c:out value="${opt.name}"/></th>
								<td>ID: <c:out value="${opt.id}"/></td>
							</tr>
						</c:forEach>
					</table>
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

		<div class="card" style="margin-bottom: 1rem;">
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
					<th class="toggle-col mobile-only">詳細</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="daily" items="${attendanceData.dailyRecords}">
					<c:set var="week" value="${daily.date.dayOfWeek.toString()}" />
					<tr data-date="${daily.date}" data-week="${fn:substring(week, 0, 3)}"
						class="${week == 'SATURDAY' ? 'saturday' : ''} ${week == 'SUNDAY' ? 'sunday' : ''}">
						<td data-label="選択"><input type="checkbox" class="row-checkbox" value="${daily.date}"></td>
						<td data-label="日"><c:out value="${daily.date.dayOfMonth}" /></td>
						<td data-label="曜日">${fn:substring(week, 0, 3)}</td>
						<td data-label="出勤" class="editable-time"><input type="time" name="kintaifrom" class="form-input"
							value="${daily.startTime == '-' ? '' : fn:escapeXml(daily.startTime)}" aria-label="${daily.date.dayOfMonth}日の出勤"></td>
						<td data-label="退勤" class="editable-time"><input type="time" name="kintaito" class="form-input"
							value="${daily.endTime == '-' ? '' : fn:escapeXml(daily.endTime)}" aria-label="${daily.date.dayOfMonth}日の退勤"></td>
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
			※リーダー所見欄はExcel出力後に手入力してください。
		</p>
		<div style="display: flex; align-items: center; justify-content: center; gap: 1rem; margin-bottom: 1rem;">
			<button type="button" class="btn btn-secondary" id="wwr-prev-btn" onclick="moveWeeklyReportCard(-1)">◀ 前の週</button>
			<span id="wwr-page-indicator" style="font-weight: 700; min-width: 8em; text-align: center;"></span>
			<button type="button" class="btn btn-secondary" id="wwr-next-btn" onclick="moveWeeklyReportCard(1)">次の週 ▶</button>
		</div>
		<c:forEach var="week" items="${weeklyWorkReports}" varStatus="loop">
			<div class="card weekly-report-block" style="background: #f8fafc; margin-bottom: 1rem;"
				data-week-end="${week.weekEndDate}">
				<h4 style="margin-top: 0;"><c:out value="${week.weekLabel}" /></h4>
				<div style="display: flex; gap: 1rem; flex-wrap: wrap;">
					<div class="form-group">
						<label class="form-label">客先名（20文字まで）</label>
						<input type="text" class="form-input wwr-client" maxlength="20" value="${week.clientName}" style="width: 240px;">
					</div>
					<div class="form-group">
						<label class="form-label">作業進捗（オンスケ／〇日遅れ等・10文字まで）</label>
						<input type="text" class="form-input wwr-status" maxlength="10" value="${week.progressStatus}" style="width: 130px;">
					</div>
				</div>
				<div class="form-group">
					<label class="form-label">PJ名（18文字まで）</label>
					<input type="text" class="form-input wwr-project" maxlength="18" value="${week.projectName}" style="width: 320px;">
				</div>
				<div class="form-group">
					<label class="form-label">工程名・作業名等（18文字まで）</label>
					<input type="text" class="form-input wwr-process" maxlength="18" value="${week.processName}" style="width: 320px;">
				</div>
				<div class="form-group">
					<label class="form-label">作業進捗、作業内容と状況（改行区切りで最大6行、1行20文字まで）</label>
					<textarea class="form-input wwr-detail" maxlength="150" style="width: 160px; height: 150px; resize: both;"><c:out value="${week.progressDetail}" /></textarea>
				</div>
				<div class="form-group">
					<label class="form-label">本人所感（改行区切りで最大10行、1行20文字まで）</label>
					<textarea class="form-input wwr-note" maxlength="250" style="width: 320px; height: 240px; resize: both;"><c:out value="${week.personalNote}" /></textarea>
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

	function saveWeeklyWorkReports() {
	    const contextPath = document.body.dataset.contextPath || "";
	    const blocks = document.querySelectorAll(".weekly-report-block");
	    const reports = Array.from(blocks).map((block) => ({
	        weekEndDate: block.dataset.weekEnd,
	        clientName: block.querySelector(".wwr-client").value,
	        projectName: block.querySelector(".wwr-project").value,
	        processName: block.querySelector(".wwr-process").value,
	        progressStatus: block.querySelector(".wwr-status").value,
	        progressDetail: block.querySelector(".wwr-detail").value,
	        personalNote: block.querySelector(".wwr-note").value
	    }));

	    const message = document.getElementById("wwr-message");
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

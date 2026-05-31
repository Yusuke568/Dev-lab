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
</style>
</head>
<body data-context-path="${pageContext.request.contextPath}">

	<!-- ▼ ステータスエリア ▼ -->
	<div class="status-area">
	    <div class="status-banner status-info">
	        <span class="icon">ℹ️</span>
	        <span class="msg">勤怠データは最新です</span>
	    </div>
	    <c:if test="${attendanceData.totalWorkMinutes > 9600}">
		    <div class="status-banner status-warning">
		        <span class="icon">⚠️</span>
		        <span class="msg">36協定：時間外労働が上限に近づいています（目安160H超）</span>
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
				
				<div class="info-toggle" onclick="toggleInfo()">
				    <span id="info-arrow">▼</span> 勤務情報一覧
				</div>
			</div>
			
			<div style="display: flex; gap: 1rem; align-items: center;">
				<div class="stat-card">
					<div class="label">合計勤務時間</div>
					<div class="value">
						<c:out value="${attendanceData.totalWorkHours}" />
					</div>
				</div>
				<a href="${pageContext.request.contextPath}/logout.do" class="btn btn-secondary">ログアウト</a>
				<a href="${pageContext.request.contextPath}/menu.do" class="btn btn-secondary">メニュー</a>
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
							<td>0:00</td>
							<td>〇 原則45H迄</td>
						</tr>
						<tr>
							<th>年間時間外</th>
							<td>93:10</td>
							<td>〇 540H/年迄</td>
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
					</div>
				</div>
			</div>
		</div>

		<div class="card actions-card">
			<div style="display: flex; gap: 1rem;">
				<button id="clock-in-btn" onclick="recordAttendance('出勤')"
					class="btn btn-primary">☀️ 出勤</button>
				<button id="clock-out-btn" onclick="recordAttendance('退勤')"
					class="btn btn-primary">🌙 退勤</button>
			</div>
			<div id="overtime-summary"></div>
			<div style="display: flex; gap: 1rem;">
				<button id="temp-save-btn" onclick="saveAttendance(true)"
					class="btn btn-secondary">📝 一時保存</button>
				<button id="save-btn" onclick="saveAttendance(false)"
					class="btn btn-success">💾 保存 (提出)</button>
			</div>
		</div>

		<div class="card" style="margin-bottom: 1rem;">
			<h4>一括入力</h4>
			<div style="display: flex; gap: 1rem; align-items: flex-end; flex-wrap: wrap;">
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
						<c:forEach var="opt" items="${workTypes}">
							<option value="${opt.id}"><c:out value="${opt.name}" /></option>
						</c:forEach>
					</select>
				</div>
				<button class="btn btn-primary" onclick="applyBulkInput()">選択行に反映</button>
			</div>
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
					<tr data-date="${daily.date}"
						class="${week == 'SATURDAY' ? 'saturday' : ''} ${week == 'SUNDAY' ? 'sunday' : ''}">
						<td data-label="選択"><input type="checkbox" class="row-checkbox" value="${daily.date}"></td>
						<td data-label="日"><c:out value="${daily.date.dayOfMonth}" /></td>
						<td data-label="曜日">${fn:substring(week, 0, 3)}</td>
						<td data-label="出勤" class="editable-time"><c:out value="${daily.startTime}" /></td>
						<td data-label="退勤" class="editable-time"><c:out value="${daily.endTime}" /></td>
						<td data-label="時間"><c:out value="${daily.workHours}" /></td>
						<td data-label="勤務区分"><select name="status" class="form-input">
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
							<c:choose>
								<c:when test="${daily.approvalStatus == 2}"><span style="color: green; font-weight: bold;">承認済</span></c:when>
								<c:when test="${daily.approvalStatus == 1}"><span style="color: orange; font-weight: bold;">申請中</span></c:when>
								<c:otherwise><span style="color: gray;">未申請</span></c:otherwise>
							</c:choose>
						</td>
						<td data-label="補正CD" class="detail-col"><input type="number" name="correctionId"
							value="${daily.correctionId}" class="form-input" /></td>
						<td data-label="補正(通)" class="detail-col"><input type="number" name="correctionUsTime"
							value="${daily.correctionUsTime}" class="form-input" /></td>
						<td data-label="補正(深)" class="detail-col"><input type="number" name="correctionMidTime"
							value="${daily.correctionMidTime}" class="form-input" /></td>
						<td class="toggle-col mobile-only"><button type="button" class="btn btn-secondary" style="padding: 4px 12px; font-size: 0.8rem; width: 100%;" onclick="toggleRowDetails(this)">詳細を開く ∨</button></td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
		</div>
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
	</script>
</body>
</html>

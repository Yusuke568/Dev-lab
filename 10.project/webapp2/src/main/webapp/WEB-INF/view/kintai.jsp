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
</head>
<body data-context-path="${pageContext.request.contextPath}">
	<div id="kintai-app-root" class="card" 
		data-staff-id="${attendanceData.employeeName}"
		data-work-types-json='${workTypesJson}'
		data-year="${attendanceData.yearMonth.year}"
		data-month="${attendanceData.yearMonth.monthValue}">
		<div class="kintai-header">
			<div>
				<h1>勤怠管理</h1>
				<p class="user-info">
					<c:out value="${attendanceData.employeeName}" />
					様 |
					<c:out value="${attendanceData.yearMonth.year}" />
					年
					<c:out value="${attendanceData.yearMonth.monthValue}" />
					月
				</p>
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

		<table id="calendar-log" class="table">
			<thead>
				<tr>
					<th style="width: 3%;"><input type="checkbox" id="bulk-check-all" onclick="toggleAllRows(this)"></th>
					<th style="width: 5%;">日</th>
					<th style="width: 8%;">曜日</th>
					<th style="width: 8%;">出勤</th>
					<th style="width: 8%;">退勤</th>
					<th style="width: 8%;">時間</th>
					<th style="width: 15%;">勤務区分</th>
					<th>備考</th>
					<th style="width: 8%;">状態</th>
					<th style="width: 7%;">補正CD</th>
					<th style="width: 7%;">補正(通)</th>
					<th style="width: 7%;">補正(深)</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="daily" items="${attendanceData.dailyRecords}">
					<c:set var="week" value="${daily.date.dayOfWeek.toString()}" />
					<tr data-date="${daily.date}"
						class="${week == 'SATURDAY' ? 'saturday' : ''} ${week == 'SUNDAY' ? 'sunday' : ''}">
						<td><input type="checkbox" class="row-checkbox" value="${daily.date}"></td>
						<td><c:out value="${daily.date.dayOfMonth}" /></td>
						<td>${fn:substring(week, 0, 3)}</td>
						<td><c:out value="${daily.startTime}" /></td>
						<td><c:out value="${daily.endTime}" /></td>
						<td><c:out value="${daily.workHours}" /></td>
						<td><select name="status" class="form-input">
								<c:forEach var="opt" items="${workTypes}">
									<option value="${opt.id}"
										${opt.id == daily.abstractId ? 'selected' : ''}>
										<c:out value="${opt.name}" />
									</option>
								</c:forEach>
						</select></td>
						<td><div style="display: flex; gap: 4px;">
							<input type="text" name="workDescription" value="${daily.workDescription}" class="form-input" maxlength="500" style="flex: 1;" />
							<button type="button" class="btn btn-secondary" onclick="insertTemplate(this)" style="padding: 2px 4px; font-size: 0.8rem;">定型</button>
						</div></td>
						<td>
							<c:choose>
								<c:when test="${daily.approvalStatus == 2}"><span style="color: green; font-weight: bold;">承認済</span></c:when>
								<c:when test="${daily.approvalStatus == 1}"><span style="color: orange; font-weight: bold;">申請中</span></c:when>
								<c:otherwise><span style="color: gray;">未申請</span></c:otherwise>
							</c:choose>
						</td>
						<td><input type="number" name="correctionId"
							value="${daily.correctionId}" class="form-input" /></td>
						<td><input type="number" name="correctionUsTime"
							value="${daily.correctionUsTime}" class="form-input" /></td>
						<td><input type="number" name="correctionMidTime"
							value="${daily.correctionMidTime}" class="form-input" /></td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
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

</body>
</html>

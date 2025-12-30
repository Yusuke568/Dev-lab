<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@page import="java.util.ArrayList"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@page import="beans.*"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8" />
<title>勤怠管理システム</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
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
</style>

<div class="card">
	<div class="kintai-header">
		<div>
			<h1>勤怠管理</h1>
			<p style="margin: 0; font-size: 1.2rem; font-weight: 700;">
				<c:out value="${shainbean.name}" />
				様 |
				<c:out value="${year}" />
				年
				<c:out value="${month}" />
				月
			</p>
		</div>
		<div class="stat-card">
			<div class="label">有給休暇残日数</div>
			<div class="value">
				<c:out value="${shainbean.paidLeaveDays}" />日
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
		<div id="overtime-summary" style="font-weight: 700;"></div>
		<div>
			<button id="save-btn" onclick="saveAttendance()"
				class="btn btn-success">💾 保存</button>
		</div>
	</div>

	<table id="calendar-log" class="table">
		<thead>
			<tr>
				<th style="width: 5%;">日</th>
				<th style="width: 8%;">曜日</th>
				<th style="width: 8%;">出勤</th>
				<th style="width: 8%;">退勤</th>
				<th style="width: 8%;">時間外</th>
				<th style="width: 15%;">勤務区分</th>
				<th>備考</th>
				<th style="width: 7%;">補正CD</th>
				<th style="width: 7%;">補正(通)</th>
				<th style="width: 7%;">補正(深)</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="calendarbean" items="${calendarBeanList}">
				<c:set var="week" value="${calendarbean.week.toString()}" />
				<tr data-date="${calendarbean.kintaidate}"
					class="${week == 'SATURDAY' ? 'saturday' : ''} ${week == 'SUNDAY' ? 'sunday' : ''}">
					<td><fmt:formatDate value="${calendarbean.getDateOfKintaidate()}"
							pattern="d" /></td>
					<td>${week.substring(0,3)}</td>
					<td><fmt:formatDate value="${calendarbean.kintaifrom}"
							pattern="HH:mm" /></td>
					<td><fmt:formatDate value="${calendarbean.kintaito}"
							pattern="HH:mm" /></td>
					<td><c:out value="${calendarbean.jikangai}" /></td>
					<td><select name="status" class="form-input">
							<c:forEach var="opt" items="${statusOptions}">
								<option value="${opt.id}"
									${opt.id == calendarbean.abstractId ? 'selected' : ''}>
									<c:out value="${opt.name}" />
								</option>
							</c:forEach>
					</select></td>
					<td><c:out value="${calendarbean.memo}" /></td>
					<td><input type="number" name="correctionId"
						value="${calendarbean.correctionId}" class="form-input" /></td>
					<td><input type="number" name="correctionUsTime"
						value="${calendarbean.correctionUsTime}" class="form-input" /></td>
					<td><input type="number" name="correctionMidTime"
						value="${calendarbean.correctionMidTime}" class="form-input" /></td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>

<!-- Status dropdown template for JS -->
<select id="status-template" style="display: none;">
	<c:forEach var="opt" items="${statusOptions}">
		<option value="<c:out value="${opt.id}"/>">
			<c:out value="${opt.name}" />
		</option>
	</c:forEach>
</select>

<script>
	// Pass JSP variables to the global scope for the external JS file
	const staffId = <c:out value="${staff_id}" />;
	const statusOptionsJson = '${statusOptionsJson}';
	const year = <c:out value="${year}" />;
	const month = <c:out value="${month}" />;
</script>
<script
	src="${pageContext.request.contextPath}/resources/js/kintai/kintai.js"
	type="module">
</script>

</body>
</html>

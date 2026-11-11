<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>勤怠承認</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
<%@ include file="_icons.jspf" %>
<style>
/* 残業事前申請ごとの、申請者のその月の時間外の累計（折れ線・基準線） */
.ot-chart-row td { background: #fcfcfb; padding: 4px 12px 12px !important; border-top: 0 !important; }
.otc { --actual: #2a78d6; --proj: #eb6834; --approved: #1baf7a; --pending: #6250d6; --ink: #0b0b0b; --ink2: #52514e; --grid: #e6e5e0;
  font-size: 0.82rem; color: var(--ink); max-width: 760px; }
.otc-head { display: flex; flex-wrap: wrap; gap: 4px 12px; align-items: baseline; margin-bottom: 2px; }
.otc-next { color: var(--ink2); }
.otc-flag { font-weight: 700; border-radius: 999px; padding: 0 8px; }
.otc-flag.is-law { background: #fdecec; color: #8a1c1c; }
.otc-flag.is-route { background: #fff4d6; color: #6b4a00; }
.otc-svg { display: block; width: 100%; height: auto; }
.otc-grid { stroke: var(--grid); stroke-width: 1; }
.otc-tick { fill: var(--ink2); font-size: 11px; }
.otc-ref { stroke: var(--ink2); stroke-width: 1.5; stroke-dasharray: 5 4; }
.otc-ref.is-law { stroke: var(--ink); stroke-width: 2; stroke-dasharray: none; }
.otc-ref-label { fill: var(--ink2); font-size: 11px; font-weight: 700; }
.otc-ref-label.is-law { fill: var(--ink); }
.otc-actual { fill: none; stroke: var(--actual); stroke-width: 2.5; stroke-linejoin: round; }
.otc-proj { fill: none; stroke: var(--proj); stroke-width: 2; stroke-dasharray: 6 4; stroke-linejoin: round; }
.otc-dot { stroke: #fcfcfb; stroke-width: 2; }
.otc-dot.k-actual { fill: var(--actual); }
.otc-dot.k-approved { fill: var(--approved); }
.otc-dot.k-pending { fill: var(--pending); }
.otc-dot.k-this { fill: var(--proj); }
.otc-this-label { fill: var(--ink); font-size: 11px; font-weight: 700; }
.otc-end-label { fill: var(--ink2); font-size: 11px; font-weight: 700; }
.otc-cross { stroke: var(--ink2); stroke-width: 1; }
.otc-hit { fill: transparent; cursor: crosshair; }
.otc-legend { display: flex; flex-wrap: wrap; gap: 4px 14px; margin-top: 2px; color: var(--ink2); }
.otc-legend-item { display: inline-flex; align-items: center; gap: 5px; }
.otc-swatch { display: inline-block; width: 10px; height: 10px; border-radius: 50%; }
.otc-swatch.line-actual { width: 16px; height: 0; border-radius: 0; border-top: 3px solid var(--actual); }
.otc-swatch.line-proj { width: 16px; height: 0; border-radius: 0; border-top: 2px dashed var(--proj); }
.otc-swatch.k-approved { background: var(--approved); }
.otc-swatch.k-pending { background: var(--pending); }
.otc-swatch.k-this { background: var(--proj); }
.otc-tip { position: absolute; z-index: 50; background: #0b0b0b; color: #fff; font-size: 0.78rem; padding: 6px 9px; border-radius: 6px; pointer-events: none; }
.otc-tip-title { font-weight: 700; }
</style>
</head>
<body>

<div class="card">
	<div class="text-center page-title-container">
		<h1>勤怠承認</h1>
		<p>あなたが承認者になっている申請を確認し、承認または却下します。残業事前申請・休暇申請は組織図に沿った承認ルートの順に回ってきます。勤怠は部下の分だけ表示されます。（通常の勤務は自動承認され、ここには表示されません）</p>
	</div>

	<div class="page-header-actions">
		<a href="${pageContext.request.contextPath}/menu.do" class="btn btn-secondary">‹ メニューへ戻る</a>
		<a href="${pageContext.request.contextPath}/approvalHistory.do" class="btn btn-secondary">📜 承認履歴を見る</a>
	</div>

	<c:if test="${not empty errorMessage}">
		<div class="error-message" style="margin: 1rem 0;"><c:out value="${errorMessage}" /></div>
	</c:if>

	<h2 class="table-title">残業事前申請（承認待ち）</h2>
	<table class="table">
		<thead>
			<tr>
				<th>社員ID</th>
				<th>氏名</th>
				<th>対象日</th>
				<th>予定出勤時刻</th>
				<th>予定退勤時刻</th>
				<th>時間外内訳</th>
				<th>申請理由</th>
				<th>申請日時</th>
				<th>承認ルート</th>
				<th class="text-center">操作</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="req" items="${pendingOvertimeRequests}">
				<tr>
					<td><c:out value="${req.employeeId}" /></td>
					<td><c:out value="${req.employeeName}" /></td>
					<td><c:out value="${req.targetDate}" /></td>
					<td><c:out value="${not empty req.plannedStartTime ? req.plannedStartTime : '09:00'}" /></td>
					<td><c:out value="${not empty req.plannedEndTime ? req.plannedEndTime : '18:00'}" /></td>
					<td><c:out value="${req.plannedOvertimeBreakdown}" /></td>
					<td><c:if test="${not empty req.projectName}"><span style="font-size: 0.85rem; color: #4a5568;">[<c:out value="${req.projectName}" /><c:if test="${not empty req.category}"> ・ <c:out value="${req.category}" /></c:if>]</span><br></c:if><c:out value="${req.reason}" /></td>
					<td><c:out value="${req.createdAt}" /></td>
					<td>
						<c:forEach var="step" items="${overtimeRoutes[req.id]}" varStatus="st">
							<c:out value="${step.label}" /><c:if test="${!st.last}"> → </c:if>
						</c:forEach>
					</td>
					<td>
						<div style="display: flex; gap: 0.5rem; justify-content: center;">
							<form action="${pageContext.request.contextPath}/overtimeRequestExecute.do" method="post">
								<input type="hidden" name="requestId" value="${req.id}">
								<input type="hidden" name="decision" value="approve">
								<button type="submit" class="btn btn-primary">承認</button>
							</form>
							<form action="${pageContext.request.contextPath}/overtimeRequestExecute.do" method="post">
								<input type="hidden" name="requestId" value="${req.id}">
								<input type="hidden" name="decision" value="reject">
								<button type="submit" class="btn btn-danger">却下</button>
							</form>
						</div>
					</td>
				</tr>
				<%-- 申請者のその月の時間外（グラフは approval-overtime-chart.js が描く） --%>
				<tr class="ot-chart-row">
					<td colspan="10"><div class="ot-chart" data-request-id="${req.id}"></div></td>
				</tr>
			</c:forEach>
			<c:if test="${empty pendingOvertimeRequests}">
				<tr>
					<td colspan="10" class="text-center">承認待ちの残業事前申請はありません。</td>
				</tr>
			</c:if>
		</tbody>
	</table>

	<h2 class="table-title">休暇申請（承認待ち）</h2>
	<table class="table">
		<thead>
			<tr>
				<th>社員ID</th>
				<th>氏名</th>
				<th>対象日</th>
				<th>区分</th>
				<th>理由</th>
				<th>申請日時</th>
				<th>承認ルート</th>
				<th class="text-center">操作</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="req" items="${pendingLeaveRequests}">
				<tr>
					<td><c:out value="${req.employeeId}" /></td>
					<td><c:out value="${req.employeeName}" /></td>
					<td><c:out value="${req.targetDate}" /></td>
					<td><c:out value="${req.abstractName}" /></td>
					<td><c:out value="${req.reason}" /></td>
					<td><c:out value="${req.createdAt}" /></td>
					<td>
						<c:forEach var="step" items="${leaveRoutes[req.id]}" varStatus="st">
							<c:out value="${step.label}" /><c:if test="${!st.last}"> → </c:if>
						</c:forEach>
					</td>
					<td>
						<div style="display: flex; gap: 0.5rem; justify-content: center;">
							<form action="${pageContext.request.contextPath}/leaveRequestExecute.do" method="post">
								<input type="hidden" name="requestId" value="${req.id}">
								<input type="hidden" name="decision" value="approve">
								<button type="submit" class="btn btn-primary">承認</button>
							</form>
							<form action="${pageContext.request.contextPath}/leaveRequestExecute.do" method="post">
								<input type="hidden" name="requestId" value="${req.id}">
								<input type="hidden" name="decision" value="reject">
								<button type="submit" class="btn btn-danger">却下</button>
							</form>
						</div>
					</td>
				</tr>
			</c:forEach>
			<c:if test="${empty pendingLeaveRequests}">
				<tr>
					<td colspan="8" class="text-center">承認待ちの休暇申請はありません。</td>
				</tr>
			</c:if>
		</tbody>
	</table>

	<h2 class="table-title">勤怠申請（有給・想定外の時間外）</h2>
	<div class="filter-form" style="display: flex; flex-wrap: wrap; gap: 1rem; align-items: flex-end; margin: 1rem 0;">
		<div class="form-group">
			<label class="form-label">社員ID</label>
			<input type="text" id="filterEmployeeId" class="form-input" placeholder="例: 1">
		</div>
		<div class="form-group">
			<label class="form-label">理由</label>
			<select id="filterReason" class="form-input">
				<option value="">すべて</option>
				<option value="overtime">時間外</option>
				<option value="leave">有給取得</option>
			</select>
		</div>
	</div>

	<table class="table" id="approval-table">
		<thead>
			<tr>
				<th>社員ID</th>
				<th>氏名</th>
				<th>日付</th>
				<th>曜日</th>
				<th>出勤</th>
				<th>退勤</th>
				<th>勤務区分</th>
				<th>理由</th>
				<th>備考</th>
				<th class="text-center">操作</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="item" items="${pendingItems}">
				<tr data-employee-id="${item.employeeId}"
					data-reason="${item.abstractName == '有給' ? 'leave' : (item.overtimeMinutes > 0 ? 'overtime' : '')}">
					<td><c:out value="${item.employeeId}" /></td>
					<td>
						<c:url value="/kintaiDisplay.do" var="memberKintaiUrl">
							<c:param name="id" value="${item.employeeId}" />
							<c:param name="year" value="${fn:substring(item.date, 0, 4)}" />
							<c:param name="month" value="${fn:substring(item.date, 5, 7)}" />
						</c:url>
						<a href="${memberKintaiUrl}" title="この月の勤怠表を開く"><c:out value="${item.employeeName}" /></a>
					</td>
					<td><c:out value="${item.date}" /></td>
					<td><c:out value="${item.week}" /></td>
					<td><c:out value="${item.startTime}" /></td>
					<td><c:out value="${item.endTime}" /></td>
					<td><c:out value="${item.abstractName}" /></td>
					<td>
						<c:if test="${item.abstractName == '有給'}">有給取得</c:if>
						<c:if test="${item.overtimeMinutes > 0}">時間外 (${item.overtimeMinutes}分)</c:if>
					</td>
					<td><c:out value="${item.memo}" /></td>
					<td>
						<div style="display: flex; gap: 0.5rem; justify-content: center;">
							<form action="${pageContext.request.contextPath}/approvalExecute.do" method="post">
								<input type="hidden" name="employeeId" value="${item.employeeId}">
								<input type="hidden" name="date" value="${item.date}">
								<input type="hidden" name="decision" value="approve">
								<button type="submit" class="btn btn-primary">承認</button>
							</form>
							<form action="${pageContext.request.contextPath}/approvalExecute.do" method="post">
								<input type="hidden" name="employeeId" value="${item.employeeId}">
								<input type="hidden" name="date" value="${item.date}">
								<input type="hidden" name="decision" value="reject">
								<button type="submit" class="btn btn-danger">却下</button>
							</form>
						</div>
					</td>
				</tr>
			</c:forEach>
			<c:if test="${empty pendingItems}">
				<tr>
					<td colspan="10" class="text-center">承認待ちの勤怠はありません。</td>
				</tr>
			</c:if>
		</tbody>
	</table>
</div>

<script type="application/json" id="overtime-status">${overtimeStatusJson}</script>
<script src="${pageContext.request.contextPath}/resources/js/approval-overtime-chart.js?v=20261112"></script>
<script>
document.addEventListener('DOMContentLoaded', () => {
	const employeeIdInput = document.getElementById('filterEmployeeId');
	const reasonSelect = document.getElementById('filterReason');
	const rows = document.querySelectorAll('#approval-table tbody tr[data-employee-id]');

	function applyFilter() {
		const employeeId = employeeIdInput.value.trim();
		const reason = reasonSelect.value;

		rows.forEach(row => {
			const matchesEmployee = !employeeId || row.dataset.employeeId === employeeId;
			const matchesReason = !reason || row.dataset.reason === reason;
			row.style.display = (matchesEmployee && matchesReason) ? '' : 'none';
		});
	}

	employeeIdInput.addEventListener('input', applyFilter);
	reasonSelect.addEventListener('change', applyFilter);
});
</script>
</body>
</html>

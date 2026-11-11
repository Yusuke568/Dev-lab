<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<title>部下の勤怠表</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
<style>
.subordinate-month {
	display: flex;
	flex-wrap: wrap;
	align-items: flex-end;
	gap: 0.5rem 1rem;
	margin-bottom: 1rem;
}
.subordinate-month .form-group {
	margin-bottom: 0;
}
.subordinate-list {
	list-style: none;
	margin: 0;
	padding: 0;
}
.subordinate-list li {
	display: flex;
	flex-wrap: wrap;
	justify-content: space-between;
	align-items: center;
	gap: 0.5rem;
	padding: 12px 0;
	border-bottom: 1px solid #e2e8f0;
}
.subordinate-list .name {
	font-weight: 700;
}
.subordinate-list .meta {
	font-size: 0.85rem;
	color: #718096;
}
/* ---- サマリー ---- */
.sum-note {
	font-size: 0.85rem;
	color: #4a5568;
	margin: 0 0 0.75rem;
}
.sum-wrap {
	overflow-x: auto;
}
.sum-table {
	width: 100%;
	border-collapse: collapse;
	font-size: 0.9rem;
}
.sum-table th, .sum-table td {
	padding: 10px 8px;
	border-bottom: 1px solid #e2e8f0;
	text-align: left;
	vertical-align: top;
}
.sum-table th {
	font-size: 0.8rem;
	color: #4a5568;
	background: #f7fafc;
	white-space: nowrap;
}
.sum-table .name { font-weight: 700; white-space: nowrap; }
.sum-table td.sum-staff { min-width: 120px; }
.sum-table .meta { font-size: 0.8rem; color: #718096; }
.sum-num { font-variant-numeric: tabular-nums; white-space: nowrap; }
.sum-num b { font-size: 1rem; }
.sum-sub { font-size: 0.8rem; color: #718096; white-space: nowrap; }
.meter {
	width: 120px;
	height: 8px;
	margin: 4px 0;
	background: #edf2f7;
	border-radius: 4px;
	overflow: hidden;
}
.meter span {
	display: block;
	height: 100%;
	border-radius: 4px;
	background: #2a78d6;
}
.meter.warn span { background: #e8a33d; }
.meter.over span { background: #d64545; }
.badge-lv {
	display: inline-block;
	font-size: 0.72rem;
	font-weight: 700;
	padding: 1px 6px;
	border-radius: 10px;
	margin-left: 4px;
}
.badge-lv.warn { background: #fdf0d5; color: #8a5a00; }
.badge-lv.over { background: #fde2e2; color: #a12626; }
.mini-bars {
	position: relative;
	display: flex;
	align-items: flex-end;
	gap: 2px;
	height: 40px;
	width: 140px;
	border-bottom: 1px solid #cbd5e0;
}
.mini-bars .bar {
	flex: 1 1 0;
	max-width: 14px;
	background: #a9c7ee;
	border-radius: 2px 2px 0 0;
}
.mini-bars .bar.current { background: #2a78d6; }
.mini-bars .bar.over { background: #d64545; }
.mini-bars .limit {
	position: absolute;
	left: 0;
	right: 0;
	border-top: 1px dashed #d64545;
	pointer-events: none;
}
.mini-labels {
	display: flex;
	justify-content: space-between;
	font-size: 0.7rem;
	color: #718096;
	width: 140px;
}
.req-chips { display: flex; flex-wrap: wrap; gap: 4px; }
.req-chip {
	font-size: 0.78rem;
	padding: 2px 8px;
	border-radius: 10px;
	background: #edf2f7;
	color: #2d3748;
	white-space: nowrap;
}
.req-chip.pending { background: #e3eefb; color: #1d4f8f; }
.req-chip.approved { background: #dff3ea; color: #146b4b; }
.req-chip.unapplied { background: #fde2e2; color: #a12626; font-weight: 700; }
.sum-table .btn { white-space: nowrap; margin: 0; }
.sum-table .sum-open .btn { display: block; text-align: center; }
.sum-table .sum-form-btn { margin-top: 6px; }
@media (max-width: 800px) {
	.sum-table thead { display: none; }
	.sum-table, .sum-table tbody, .sum-table tr, .sum-table td { display: block; width: 100%; box-sizing: border-box; }
	.sum-table tr {
		border: 1px solid #e2e8f0;
		border-radius: 6px;
		margin-bottom: 12px;
		padding: 4px 10px;
	}
	.sum-table td {
		display: flex;
		justify-content: space-between;
		gap: 12px;
		border-bottom: 1px dashed #edf2f7;
		padding: 8px 0;
	}
	.sum-table td:last-child { border-bottom: none; }
	.sum-table td::before {
		content: attr(data-label);
		font-size: 0.8rem;
		font-weight: 700;
		color: #4a5568;
		flex: 0 0 auto;
	}
	.sum-table td.sum-staff, .sum-table td.sum-open { display: block; }
	.sum-table td.sum-staff::before, .sum-table td.sum-open::before { content: none; }
	.sum-table td > .cell { display: flex; flex-direction: column; align-items: flex-end; }
	.sum-table td > .req-chips { flex-direction: row; justify-content: flex-end; }
	.sum-table td.sum-open .btn { display: block; text-align: center; }
}
@media (max-width: 600px) {
	.subordinate-list .btn {
		flex: 1 1 100%;
		margin: 0;
	}
}
</style>
<%@ include file="_icons.jspf" %>
</head>
<body>
	<div class="card">
		<h1>部下の勤怠表</h1>
		<p>組織図で自分が承認者になっている部署と、その下の部署に所属する社員の勤怠表を参照・編集できます。</p>

		<c:if test="${not empty sessionScope.subordinateMessage}">
			<div class="error-message" style="margin: 0 0 1rem;" role="alert">⚠ <c:out value="${sessionScope.subordinateMessage}" /></div>
			<c:remove var="subordinateMessage" scope="session" />
		</c:if>

		<form class="subordinate-month" method="get" action="${pageContext.request.contextPath}/subordinateAttendance.do">
			<div class="form-group">
				<label for="ym" class="form-label">表示する月</label>
				<input type="month" id="ym" name="ym" class="form-input" value="<c:out value='${targetYm}' />">
			</div>
			<button type="submit" class="btn btn-secondary">切り替え</button>
		</form>

		<c:choose>
			<c:when test="${empty subordinates}">
				<p>部下がいません。組織図（マスタ管理）で、自分が承認者の部署に社員が所属しているか確かめてください。</p>
			</c:when>
			<c:when test="${not empty summaries}">
				<p class="sum-note">
					当月は ${targetYear}年${targetMonth}月、年間は ${targetYear}年1月〜${targetMonth}月の累計です。
					時間外は、月 ${monthLimitHours}時間・年 ${yearLimitHours}時間（36協定の原則の上限）と比べ、80%以上で「注意」、上限以上で「超過」と出します。
				</p>
				<div class="sum-wrap">
				<table class="sum-table">
					<thead>
						<tr>
							<th>社員</th>
							<th>当月の時間外</th>
							<th>年間の時間外</th>
							<th>月ごとの時間外</th>
							<th>総勤務時間<br>当月 / 年間</th>
							<th>出勤日数<br>当月 / 年間</th>
							<th>有給（日）<br>当月 / 年間</th>
							<th>残業事前申請（当月）</th>
							<th></th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="v" items="${summaries}">
							<tr>
								<td class="sum-staff">
									<div class="name"><c:out value="${v.name}" /></div>
									<div class="meta">社員ID <c:out value="${v.staffId}" /> ・ <c:out value="${v.departmentName}" /></div>
								</td>
								<td data-label="当月の時間外">
									<div class="cell">
										<div class="sum-num"><b>${v.monthOvertime}</b> / ${monthLimitHours}:00<c:if test="${v.monthLevel == 'warn'}"><span class="badge-lv warn">⚠ 注意</span></c:if><c:if test="${v.monthLevel == 'over'}"><span class="badge-lv over">⛔ 超過</span></c:if></div>
										<div class="meter ${v.monthLevel}" role="img" aria-label="上限の${v.monthOvertimePercent}%"><span style="width: ${v.monthBarPercent}%"></span></div>
										<div class="sum-sub">上限の ${v.monthOvertimePercent}%</div>
									</div>
								</td>
								<td data-label="年間の時間外">
									<div class="cell">
										<div class="sum-num"><b>${v.yearOvertime}</b> / ${yearLimitHours}:00<c:if test="${v.yearLevel == 'warn'}"><span class="badge-lv warn">⚠ 注意</span></c:if><c:if test="${v.yearLevel == 'over'}"><span class="badge-lv over">⛔ 超過</span></c:if></div>
										<div class="meter ${v.yearLevel}" role="img" aria-label="上限の${v.yearOvertimePercent}%"><span style="width: ${v.yearBarPercent}%"></span></div>
										<div class="sum-sub">上限の ${v.yearOvertimePercent}%</div>
									</div>
								</td>
								<td data-label="月ごとの時間外">
									<div class="cell">
										<div class="mini-bars" role="img" aria-label="1月〜${targetMonth}月の時間外。点線は${monthLimitHours}時間">
											<span class="limit" style="bottom: ${v.monthLimitLinePercent}%"></span>
											<c:forEach var="b" items="${v.monthBars}">
												<span class="bar${b.current ? ' current' : ''}${b.overLimit ? ' over' : ''}" style="height: ${b.heightPercent}%" title="${b.label} ${b.value}"></span>
											</c:forEach>
										</div>
										<div class="mini-labels"><span>1月</span><span>${targetMonth}月</span></div>
									</div>
								</td>
								<td data-label="総勤務時間（当月 / 年間）">
									<div class="cell sum-num"><span><b>${v.monthTotal}</b> / ${v.yearTotal}</span></div>
								</td>
								<td data-label="出勤日数（当月 / 年間）">
									<div class="cell">
										<div class="sum-num"><b>${v.monthWorkDays}日</b> / ${v.yearWorkDays}日</div>
										<c:if test="${v.yearHolidayWorkDays > 0}">
											<div class="sum-sub">うち休日出勤 ${v.monthHolidayWorkDays}日 / ${v.yearHolidayWorkDays}日</div>
										</c:if>
									</div>
								</td>
								<td data-label="有給（当月 / 年間）">
									<div class="cell">
										<div class="sum-num"><b>${v.monthPaidLeave}日</b> / ${v.yearPaidLeave}日</div>
										<div class="sum-sub">残 ${v.paidLeaveRemaining}日</div>
									</div>
								</td>
								<td data-label="残業事前申請">
									<div class="cell req-chips">
										<span class="req-chip pending">申請中 ${v.pendingRequests}件</span>
										<span class="req-chip approved">承認済 ${v.approvedRequests}件</span>
										<c:if test="${v.unappliedDays > 0}">
											<span class="req-chip unapplied" title="時間外があるのに、申請中・承認済みの事前申請がない日">⚠ 未申請 ${v.unappliedDays}日</span>
										</c:if>
									</div>
								</td>
								<td class="sum-open">
									<c:url value="/kintaiDisplay.do" var="kintaiUrl">
										<c:param name="id" value="${v.staffId}" />
										<c:param name="year" value="${targetYear}" />
										<c:param name="month" value="${targetMonth}" />
									</c:url>
									<a href="${kintaiUrl}" class="btn btn-primary">勤怠表を開く</a>
									<c:url value="/subordinateOvertimeForm.do" var="otFormUrl">
										<c:param name="id" value="${v.staffId}" />
										<c:param name="year" value="${targetYear}" />
										<c:param name="month" value="${targetMonth}" />
									</c:url>
									<a href="${otFormUrl}" class="btn btn-secondary sum-form-btn" title="${targetMonth}月の個人別時間外勤務申請書（Excel）をダウンロード">時間外申請書</a>
								</td>
							</tr>
						</c:forEach>
					</tbody>
				</table>
				</div>
			</c:when>
			<c:otherwise>
				<ul class="subordinate-list">
					<c:forEach var="s" items="${subordinates}">
						<li>
							<div>
								<div class="name"><c:out value="${s.name}" /></div>
								<div class="meta">社員ID <c:out value="${s.staffId}" /> ・ <c:out value="${s.departmentName}" /></div>
							</div>
							<c:url value="/kintaiDisplay.do" var="kintaiUrl">
								<c:param name="id" value="${s.staffId}" />
								<c:param name="year" value="${targetYear}" />
								<c:param name="month" value="${targetMonth}" />
							</c:url>
							<a href="${kintaiUrl}" class="btn btn-primary">${targetMonth}月の勤怠表を開く</a>
						</li>
					</c:forEach>
				</ul>
			</c:otherwise>
		</c:choose>

		<div style="margin-top: 1.5rem;">
			<a href="${pageContext.request.contextPath}/menu.do" class="btn btn-secondary">‹ メニューに戻る</a>
		</div>
	</div>
</body>
</html>

<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>残業事前申請</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
<%@ include file="_icons.jspf" %>
<style>
/* 複数日をまとめて申請するカレンダー */
.ot-multi { flex: 1 1 100%; border: 1px solid #e2e8f0; border-radius: 6px; padding: 10px 12px; background: #fbfcfe; }
.ot-multi[hidden] { display: none; }
#ot-single-date[hidden] { display: none; }
.ot-multi-head { display: flex; align-items: center; justify-content: space-between; gap: 8px; margin-bottom: 6px; }
.ot-multi-head strong { font-size: 0.95rem; }
.ot-cal { display: grid; grid-template-columns: repeat(7, minmax(0, 1fr)); gap: 4px; max-width: 420px; }
.ot-cal .dow { font-size: 0.75rem; text-align: center; color: #4a5568; }
.ot-cal .dow.sun, .ot-cal .day.sun { color: #c53030; }
.ot-cal .dow.sat, .ot-cal .day.sat { color: #2b6cb0; }
.ot-cal .day {
	min-height: 40px; border: 1px solid #e2e8f0; border-radius: 6px; background: #fff; font: inherit; font-size: 0.85rem;
	cursor: pointer; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 2px;
}
.ot-cal .day small { font-size: 0.65rem; color: #718096; line-height: 1.1; }
.ot-cal .day.is-selected { background: #2a78d6; border-color: #2a78d6; color: #fff; font-weight: 700; }
.ot-cal .day.is-selected small { color: #e6f0fb; }
.ot-cal .day.is-applied:not(.is-selected) { background: #f1f3f5; color: #718096; border-style: dashed; }
.ot-cal .day.is-today { outline: 2px solid #1baf7a; outline-offset: -2px; }
.ot-multi-selected { font-size: 0.85rem; color: #2d3748; margin-top: 6px; }
.ot-prev-note { flex: 1 1 100%; font-size: 0.82rem; color: #4a5568; margin: -0.25rem 0 0; }
</style>
</head>
<body>

<div class="card">
	<div class="text-center page-title-container">
		<h1>残業事前申請</h1>
		<p>時間外労働を行う前に、この画面から申請してください。承認結果はこの画面で確認できます。申請中の申請は、承認される前なら「取り下げる」で取り消せます。却下・取り下げた日は、この画面から再申請できます。</p>
	</div>

	<div class="page-header-actions">
		<a href="${pageContext.request.contextPath}/menu.do" class="btn btn-secondary">‹ メニューへ戻る</a>
	</div>

	<c:if test="${not empty errorMessage}">
		<div class="status-banner status-danger" role="alert" style="margin: 1rem 0;">
			<span class="icon">⚠️</span>
			<span class="msg"><strong>できませんでした。</strong><c:out value="${errorMessage}" /></span>
		</div>
	</c:if>
	<c:if test="${not empty successMessage}">
		<div class="status-banner status-success" role="status" style="margin: 1rem 0; background-color: #e8f8e8; color: #1a7f1a;">
			<span class="icon">✔️</span>
			<span class="msg"><c:out value="${successMessage}" /></span>
		</div>
	</c:if>

	<c:if test="${not empty prefillDate and empty form}">
		<div class="status-item status-info" style="margin: 1rem 0;">
			ℹ <c:out value="${prefillDate}" /> の勤怠実績から予定出勤・予定退勤時刻を自動入力しました。内容を確認のうえ申請してください。
		</div>
	</c:if>

	<div class="card">
		<h2>新規申請</h2>
		<form action="${pageContext.request.contextPath}/overtimeRequestSubmit.do" method="post"
			style="display: flex; flex-wrap: wrap; gap: 1rem; align-items: flex-end;">
			<div class="form-group" id="ot-single-date">
				<label class="form-label" for="targetDate">対象日</label>
				<input type="date" id="targetDate" name="targetDate" class="form-input" value="<c:out value='${not empty form ? form.targetDate : prefillDate}' />" required>
			</div>
			<div class="form-group">
				<button type="button" class="btn btn-secondary" id="ot-multi-toggle" aria-expanded="false" aria-controls="ot-multi">📅 複数日をまとめて申請</button>
			</div>
			<%-- 複数日をまとめて申請（resources/js/kintai/overtime-request-dates.js がカレンダーを描く） --%>
			<div class="ot-multi" id="ot-multi" hidden>
				<div class="ot-multi-head">
					<button type="button" class="btn btn-secondary" id="ot-cal-prev" aria-label="前の月">‹</button>
					<strong id="ot-cal-title"></strong>
					<button type="button" class="btn btn-secondary" id="ot-cal-next" aria-label="次の月">›</button>
				</div>
				<div class="ot-cal" id="ot-cal" role="group" aria-label="申請する日"></div>
				<div class="ot-multi-selected" id="ot-multi-selected" aria-live="polite"></div>
				<input type="hidden" name="targetDates" id="targetDates" value="<c:out value='${form.targetDates}' />">
			</div>
			<div class="form-group">
				<label class="form-label">予定出勤時刻</label>
				<input type="time" id="plannedStartTime" name="plannedStartTime" class="form-input" value="<c:out value='${not empty form ? form.plannedStartTime : prefillStartTime}' />">
			</div>
			<div class="form-group">
				<label class="form-label">予定退勤時刻</label>
				<input type="time" id="plannedEndTime" name="plannedEndTime" class="form-input" value="<c:out value='${not empty form ? form.plannedEndTime : prefillEndTime}' />">
			</div>
			<div class="form-group">
				<label class="form-label" for="correctionTime">補正（通常）</label>
				<input type="text" id="correctionTime" name="correctionTime" class="form-input" style="width: 6.5rem;"
					placeholder="0:30" inputmode="numeric" pattern="\d{1,2}:[0-5]\d" title="時:分で入れてください（例 0:30）"
					value="<c:out value='${not empty form ? form.correctionTime : prefillCorrection}' />">
			</div>
			<div class="form-group">
				<label class="form-label" for="correctionMidTime">補正（深夜）</label>
				<input type="text" id="correctionMidTime" name="correctionMidTime" class="form-input" style="width: 6.5rem;"
					placeholder="0:30" inputmode="numeric" pattern="\d{1,2}:[0-5]\d" title="時:分で入れてください（例 0:30）"
					value="<c:out value='${not empty form ? form.correctionMidTime : prefillCorrectionMid}' />">
			</div>
			<%-- 兼任している人だけ：どの部署の業務か（承認ルートが部署で変わる） --%>
			<c:if test="${fn:length(myDepartments) > 1}">
				<div class="form-group">
					<label class="form-label" for="departmentId">業務の部署</label>
					<select id="departmentId" name="departmentId" class="form-input">
						<c:forEach var="d" items="${myDepartments}" varStatus="st">
							<option value="${d.id}" <c:if test="${(not empty form and form.departmentId == d.id.toString()) or (empty form and st.first)}">selected</c:if>><c:out value="${d.name}" /><c:if test="${st.first}">（主所属）</c:if></option>
						</c:forEach>
					</select>
				</div>
			</c:if>
			<div class="form-group" style="min-width: 160px;">
				<label class="form-label" for="ot-project">PJ名 <span style="color: #c50000; font-size: 0.8rem;">（必須）</span></label>
				<input type="text" id="ot-project" name="projectName" class="form-input" placeholder="例: 税保守"
					required maxlength="100" aria-required="true" title="時間外申請書の PJ名欄に出ます" list="ot-project-list"
					value="<c:out value='${not empty form ? form.projectName : lastProjectName}' />">
				<datalist id="ot-project-list">
					<c:forEach var="p" items="${recentProjectNames}"><option value="<c:out value='${p}' />"></c:forEach>
				</datalist>
			</div>
			<div class="form-group">
				<label class="form-label" for="ot-category">摘要</label>
				<select id="ot-category" name="category" class="form-input" title="時間外申請書の摘要欄に出ます">
					<c:forEach var="cat" items="${['①自責', '②他責', '③緊急', '④客先都合', '⑤その他']}">
						<option value="${cat}" <c:set var="selectedCategory" value="${not empty form.category ? form.category : (not empty lastCategory ? lastCategory : '⑤その他')}" /><c:if test="${selectedCategory == cat}">selected</c:if>>${cat}</option>
					</c:forEach>
				</select>
			</div>
			<div class="form-group" style="flex: 1; min-width: 200px;">
				<label class="form-label" for="ot-reason">申請理由 <span style="color: #c50000; font-size: 0.8rem;">（必須）</span></label>
				<input type="text" id="ot-reason" name="reason" class="form-input" placeholder="例: 月末締め作業のため" style="width: 100%;"
					required maxlength="255" aria-required="true" title="時間外が必要な理由を入れてください" list="ot-reason-list"
					value="<c:out value='${not empty form ? form.reason : lastReason}' />">
				<datalist id="ot-reason-list">
					<c:forEach var="r" items="${recentReasons}"><option value="<c:out value='${r}' />"></c:forEach>
				</datalist>
			</div>
			<button type="submit" class="btn btn-primary" id="ot-submit">申請</button>
			<c:if test="${empty form and not empty lastProjectName}">
				<p class="ot-prev-note">ℹ PJ名・摘要・申請理由には、前回の申請の内容を入れています。入力欄をクリックすると、これまでに使った内容から選べます。</p>
			</c:if>
		</form>
		<p style="font-size: 0.85rem; color: #666; margin-top: 0.5rem;">
			※ 標準勤務時間（09:00〜18:00）より早い予定出勤・遅い予定退勤の分が、それぞれ早出・残業として時間外申請されます（両方入力すれば同日で両方申請できます）。標準どおりの時刻は空欄のままで構いません。<br>
			※ 予定退勤が予定出勤より前の時刻なら、日をまたいだ翌日の退勤として数えます（例：13:00〜1:30 は翌1:30まで）。<br>
			※ 休日（土日・祝日・会社休日）の申請は、予定出勤〜予定退勤から休憩を除いた時間を全部、時間外の見込みとして申請します（例：9:00〜12:00 は 3:00）。実績は週40時間の決まりで計算するので、見込みより少なくなることがあります。<br>
			※ 補正（通常・深夜）は、予定の出退勤から出した時間に足して申請します。勤怠を入力済みの日は、空欄なら勤怠の補正を自動で使います。<br>
			※ 補正（深夜）を入れた申請は、深夜残業として承認ルートが決まります。<br>
			※ 間接の時間で時間外になった日は、勤怠に入力済みの時間外で申請できます（出退勤が 9:00〜18:00 の中でも申請できます）。<br>
			※ 却下・取り下げた対象日も、同じ対象日を指定して再度申請するとその申請が更新され、再度承認待ちになります。<br>
			※「複数日をまとめて申請」では、カレンダーで選んだ日ごとに、同じ予定時刻・補正・PJ名・摘要・理由で申請します（最大31日）。<br>
			※ 申請中・承認済みの日も申請し直せます（間違えた時間の訂正など）。申請し直すと内容を書き換えて承認待ちに戻り、もう一度承認を受けます。
		</p>
	</div>

	<h2 class="table-title">申請履歴</h2>
	<table class="table">
		<thead>
			<tr>
				<th>対象日</th>
				<th>予定出勤時刻</th>
				<th>予定退勤時刻</th>
				<th>時間外内訳</th>
				<th>PJ名・摘要</th>
				<th>申請理由</th>
				<th>ステータス</th>
				<th>申請日時</th>
				<th>承認/却下日時</th>
				<th>実際の勤怠実績</th>
				<th class="text-center">操作</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="req" items="${myRequests}">
				<c:set var="actual" value="${attendanceByDate[req.targetDate]}" />
				<tr>
					<td><c:out value="${req.targetDate}" /></td>
					<td><c:out value="${not empty req.plannedStartTime ? req.plannedStartTime : '09:00'}" /></td>
					<td><c:out value="${not empty req.plannedEndTime ? req.plannedEndTime : '18:00'}" /></td>
					<td><c:out value="${req.plannedOvertimeBreakdown}" /></td>
					<td><c:out value="${req.projectName}" /><c:if test="${not empty req.category}"><br><span style="font-size: 0.85rem; color: #4a5568;"><c:out value="${req.category}" /></span></c:if></td>
					<td><c:out value="${req.reason}" /></td>
					<td>
						<c:choose>
							<c:when test="${req.status == 'APPROVED'}"><span style="color: green; font-weight: bold;">承認済み</span></c:when>
							<c:when test="${req.status == 'REJECTED'}"><span style="color: red; font-weight: bold;">却下</span></c:when>
							<c:when test="${req.status == 'WITHDRAWN'}"><span style="color: #718096; font-weight: bold;">取り下げ</span></c:when>
							<c:otherwise><span style="color: orange; font-weight: bold;">申請中</span></c:otherwise>
						</c:choose>
					</td>
					<td><c:out value="${req.createdAt}" /></td>
					<td><c:out value="${req.decidedAt}" /></td>
					<td>
						<c:choose>
							<c:when test="${not empty actual}">
								出勤<c:out value="${actual.startTime}" />〜退勤<c:out value="${actual.endTime}" />（実働<c:out value="${actual.workHours}" />）
							</c:when>
							<c:otherwise>未入力</c:otherwise>
						</c:choose>
					</td>
					<td class="text-center">
						<c:if test="${req.status == 'REJECTED' or req.status == 'WITHDRAWN'}">
							<button type="button" class="btn btn-secondary" onclick="prefillReapply('${req.targetDate}', '${req.plannedStartTime}', '${req.plannedEndTime}', '${fn:escapeXml(req.projectName)}', '${req.category}')">この日で再申請</button>
						</c:if>
						<c:if test="${req.status == 'PENDING' or req.status == 'APPROVED'}">
							<%-- 間違えた時間で申請・承認された申請の訂正：内容を直して申請し直す（承認待ちに戻り、もう一度承認を受ける） --%>
							<button type="button" class="btn btn-secondary" onclick="prefillReapply('${req.targetDate}', '${req.plannedStartTime}', '${req.plannedEndTime}', '${fn:escapeXml(req.projectName)}', '${req.category}')">訂正して申請し直す</button>
						</c:if>
						<c:if test="${req.status == 'PENDING'}">
							<%-- 承認される前なら取り下げられる。取り下げた日はもう一度申請できる --%>
							<form action="${pageContext.request.contextPath}/overtimeRequestWithdraw.do" method="post" style="display: inline;"
								onsubmit="return confirm('${req.targetDate} の残業事前申請を取り下げますか？\n取り下げたあと、内容を直して申請し直せます。');">
								<input type="hidden" name="requestId" value="${req.id}">
								<button type="submit" class="btn btn-danger">取り下げる</button>
							</form>
						</c:if>
					</td>
				</tr>
			</c:forEach>
			<c:if test="${empty myRequests}">
				<tr>
					<td colspan="11" class="text-center">申請履歴はまだありません。</td>
				</tr>
			</c:if>
		</tbody>
	</table>
</div>

<script type="application/json" id="ot-applied-dates">${appliedDatesJson}</script>
<script src="${pageContext.request.contextPath}/resources/js/kintai/overtime-request-dates.js?v=20261113b"></script>
<script>
function prefillReapply(targetDate, plannedStartTime, plannedEndTime, projectName, category) {
	if (window.otMultiDates) window.otMultiDates.close();
	document.getElementById('targetDate').value = targetDate;
	if (projectName) document.getElementById('ot-project').value = projectName;
	if (category) document.getElementById('ot-category').value = category;
	document.getElementById('plannedStartTime').value = plannedStartTime || '';
	document.getElementById('plannedEndTime').value = plannedEndTime || '';
	document.getElementById('targetDate').scrollIntoView({ behavior: 'smooth', block: 'center' });
}
</script>
</body>
</html>

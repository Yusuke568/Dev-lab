<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>休暇申請</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
<%@ include file="_icons.jspf" %>
</head>
<body>

<div class="card">
	<div class="text-center page-title-container">
		<h1>休暇申請</h1>
		<p>有給・代休などの休暇を申請します。所属部署の承認者から順に、組織図に沿って承認されます。<br>
		※ 承認されても勤怠には自動で反映されません。勤怠入力画面で当日の勤務区分を入力してください。</p>
	</div>

	<div class="page-header-actions">
		<a href="${pageContext.request.contextPath}/menu.do" class="btn btn-secondary">‹ メニューへ戻る</a>
	</div>

	<c:if test="${not empty errorMessage}">
		<div class="error-message" style="margin: 1rem 0;"><c:out value="${errorMessage}" /></div>
	</c:if>

	<div class="card">
		<h2>新規申請</h2>
		<form action="${pageContext.request.contextPath}/leaveRequestSubmit.do" method="post"
			style="display: flex; flex-wrap: wrap; gap: 1rem; align-items: flex-end;">
			<div class="form-group">
				<label class="form-label">対象日</label>
				<input type="date" id="targetDate" name="targetDate" class="form-input" required>
			</div>
			<div class="form-group">
				<label class="form-label">区分</label>
				<select id="abstractId" name="abstractId" class="form-input" required>
					<option value="">選択してください</option>
					<c:forEach var="t" items="${leaveTypes}">
						<option value="${t.id}"><c:out value="${t.name}" /></option>
					</c:forEach>
				</select>
			</div>
			<div class="form-group" style="flex: 1; min-width: 200px;">
				<label class="form-label">理由</label>
				<input type="text" name="reason" class="form-input" maxlength="255" placeholder="例: 私用のため" style="width: 100%;">
			</div>
			<button type="submit" class="btn btn-primary">申請</button>
		</form>
		<p style="font-size: 0.85rem; color: #666; margin-top: 0.5rem;">
			※ 1日につき1件です。却下された日付は、同じ日付で申請し直すと再申請になります。
		</p>
	</div>

	<h2 class="table-title">申請履歴</h2>
	<table class="table">
		<thead>
			<tr>
				<th>対象日</th>
				<th>区分</th>
				<th>理由</th>
				<th>ステータス</th>
				<th>承認ルート</th>
				<th>申請日時</th>
				<th class="text-center">操作</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="req" items="${myRequests}">
				<tr>
					<td><c:out value="${req.targetDate}" /></td>
					<td><c:out value="${req.abstractName}" /></td>
					<td><c:out value="${req.reason}" /></td>
					<td>
						<c:choose>
							<c:when test="${req.status == 'APPROVED'}"><span style="color: green; font-weight: bold;">承認済み</span></c:when>
							<c:when test="${req.status == 'REJECTED'}"><span style="color: red; font-weight: bold;">却下</span></c:when>
							<c:otherwise><span style="color: orange; font-weight: bold;">申請中（<c:out value="${req.currentLevel}" />段目）</span></c:otherwise>
						</c:choose>
					</td>
					<td>
						<c:forEach var="step" items="${routes[req.id]}" varStatus="st">
							<c:out value="${step.label}" /><c:if test="${!st.last}"> → </c:if>
						</c:forEach>
					</td>
					<td><c:out value="${req.createdAt}" /></td>
					<td class="text-center">
						<c:if test="${req.status == 'REJECTED'}">
							<button type="button" class="btn btn-secondary"
								onclick="prefillReapply('${req.targetDate}', '${req.abstractId}')">この日で再申請</button>
						</c:if>
					</td>
				</tr>
			</c:forEach>
			<c:if test="${empty myRequests}">
				<tr>
					<td colspan="7" class="text-center">申請履歴はまだありません。</td>
				</tr>
			</c:if>
		</tbody>
	</table>
</div>

<script>
function prefillReapply(targetDate, abstractId) {
	document.getElementById('targetDate').value = targetDate;
	document.getElementById('abstractId').value = abstractId;
	document.getElementById('targetDate').scrollIntoView({ behavior: 'smooth', block: 'center' });
}
</script>
</body>
</html>

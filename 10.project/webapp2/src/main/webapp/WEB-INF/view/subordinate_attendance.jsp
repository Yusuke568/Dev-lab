<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<title>配下の勤怠表</title>
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
		<h1>配下の勤怠表</h1>
		<p>組織図で自分が承認者になっている部署と、その下の部署に所属する社員の勤怠表を参照・編集できます。</p>

		<form class="subordinate-month" method="get" action="${pageContext.request.contextPath}/subordinateAttendance.do">
			<div class="form-group">
				<label for="ym" class="form-label">表示する月</label>
				<input type="month" id="ym" name="ym" class="form-input" value="<c:out value='${targetYm}' />">
			</div>
			<button type="submit" class="btn btn-secondary">切り替え</button>
		</form>

		<c:choose>
			<c:when test="${empty subordinates}">
				<p>配下の社員がいません。組織図（マスタ管理）で、自分が承認者の部署に社員が所属しているか確かめてください。</p>
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

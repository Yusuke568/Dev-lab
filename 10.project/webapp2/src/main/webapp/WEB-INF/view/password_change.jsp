<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<title>パスワード変更</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
	<div class="card password-card">
		<h1>パスワード変更</h1>

		<c:if test="${sessionScope.mustChangePassword}">
			<div class="status-banner status-warning">
				<span class="icon">⚠️</span>
				<span>管理者がパスワードを設定しました。使い続けるには、新しいパスワードに変更してください。</span>
			</div>
		</c:if>

		<c:if test="${not empty successMessage}">
			<div class="status-banner status-info">
				<span class="icon">✔️</span>
				<span><c:out value="${successMessage}" /></span>
			</div>
		</c:if>

		<c:if test="${not empty errors}">
			<div class="status-banner status-danger">
				<ul class="error-list">
					<c:forEach var="error" items="${errors}">
						<li><c:out value="${error}" /></li>
					</c:forEach>
				</ul>
			</div>
		</c:if>

		<%-- 入力されたパスワードは、エラーのときも value に戻さない --%>
		<form action="${pageContext.request.contextPath}/passwordChangeExecute.do" method="post" autocomplete="off">
			<input type="hidden" name="_csrf" value="<c:out value='${sessionScope.csrfToken}' />">
			<div class="form-group">
				<label for="currentPassword" class="form-label">現在のパスワード</label>
				<input type="password" id="currentPassword" name="currentPassword" class="form-input"
					autocomplete="current-password" required>
			</div>
			<div class="form-group">
				<label for="newPassword" class="form-label">新しいパスワード（8文字以上）</label>
				<input type="password" id="newPassword" name="newPassword" class="form-input"
					autocomplete="new-password" minlength="8" required>
			</div>
			<div class="form-group">
				<label for="confirmPassword" class="form-label">新しいパスワード（確認）</label>
				<input type="password" id="confirmPassword" name="confirmPassword" class="form-input"
					autocomplete="new-password" minlength="8" required>
			</div>
			<div class="form-buttons">
				<button type="submit" class="btn btn-primary">変更する</button>
				<c:choose>
					<c:when test="${sessionScope.mustChangePassword}">
						<a href="${pageContext.request.contextPath}/logout.do" class="btn">ログアウト</a>
					</c:when>
					<c:otherwise>
						<a href="${pageContext.request.contextPath}/menu.do" class="btn">メニューに戻る</a>
					</c:otherwise>
				</c:choose>
			</div>
		</form>
	</div>
</body>
</html>

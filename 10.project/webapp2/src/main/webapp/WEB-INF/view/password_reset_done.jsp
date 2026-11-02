<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<title>パスワード初期化完了</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
	<div class="card password-card">
		<h1>パスワードを初期化しました</h1>
		<p>
			<strong><c:out value="${shain.name}" /></strong>さん（ユーザー名：<c:out value="${shain.id.value}" />）は、
			次回ログイン時に新しいパスワードへの変更を求められます。
		</p>
		<c:choose>
			<c:when test="${not empty temporaryPassword}">
				<%@ include file="_temporary_password.jspf" %>
			</c:when>
			<c:otherwise>
				<p>指定したパスワードを本人に伝えてください。</p>
			</c:otherwise>
		</c:choose>
		<div class="form-buttons">
			<a href="${pageContext.request.contextPath}/shainList.do" class="btn btn-primary">社員一覧に戻る</a>
		</div>
	</div>
</body>
</html>

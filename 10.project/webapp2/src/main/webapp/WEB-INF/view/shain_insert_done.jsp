<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<title>社員登録完了</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
<%@ include file="_icons.jspf" %>
</head>
<body>
	<div class="card password-card">
		<h1>社員を登録しました</h1>
		<p>
			<strong><c:out value="${shainName}" /></strong>さんを登録し、ログイン用のアカウントを作りました。
			ユーザー名は社員ID（<strong><c:out value="${shainId}" /></strong>）です。
			初回ログイン時に、本人にパスワードを変更してもらいます。
		</p>
		<c:choose>
			<c:when test="${not empty temporaryPassword}">
				<%@ include file="_temporary_password.jspf" %>
			</c:when>
			<c:otherwise>
				<p>指定した初期パスワードを本人に伝えてください。</p>
			</c:otherwise>
		</c:choose>
		<div class="form-buttons">
			<a href="${pageContext.request.contextPath}/shainList.do" class="btn btn-primary">社員一覧に戻る</a>
		</div>
	</div>
</body>
</html>

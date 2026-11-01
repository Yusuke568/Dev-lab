<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>エラー</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
	<div class="card text-center" style="max-width: 500px;">
		<div style="font-size: 4rem; margin-bottom: 1rem;">🤔</div>
		<h1>おっと、問題が発生しました</h1>
		<c:choose>
			<c:when test="${not empty errorMessage}">
				<p style="font-size: 1.1rem; color: #6c757d;"><c:out value="${errorMessage}" /></p>
			</c:when>
			<c:otherwise>
				<p style="font-size: 1.1rem; color: #6c757d;">
			申し訳ありませんが、予期せぬエラーが発生しました。<br>
			管理者にお問い合わせいただくか、時間をおいて再度お試しください。
		</p>
			</c:otherwise>
		</c:choose>
		<div style="margin-top: 30px;">
			<a href="${pageContext.request.contextPath}/menu.do"
				class="btn btn-primary">‹ メニューに戻る</a>
		</div>
	</div>
</body>
</html>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
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
		<p style="font-size: 1.1rem; color: #6c757d;">
			申し訳ありませんが、予期せぬエラーが発生しました。<br>
			管理者にお問い合わせいただくか、時間をおいて再度お試しください。
		</p>
		<div style="margin-top: 30px;">
			<a href="${pageContext.request.contextPath}/ShainMenu"
				class="btn btn-primary">‹ メニューに戻る</a>
		</div>
	</div>
</body>
</html>
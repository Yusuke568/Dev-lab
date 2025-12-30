<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@page import="beans.*"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>社員削除画面</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
</head>

<body>
	<div class="card" style="max-width: 600px;">
		<div class="text-center" style="margin-bottom: 2rem;">
			<h1>社員情報の削除</h1>
			<p class="error-message" style="font-size: 1.2rem;">本当にこの社員情報を削除しますか？<br>この操作は元に戻せません。</p>
		</div>

		<form action="ShainDeleteComplete" method="post">
			<input type="hidden" name="id"
				value="<c:out value="${shainBean.id}"/>">

			<div class="form-group">
				<label class="form-label">社員ID</label>
				<input type="text" class="form-input" value="<c:out value="${shainBean.id}"/>" readonly>
			</div>

			<div class="form-group">
				<label class="form-label">名前</label>
				<input type="text" class="form-input" value="<c:out value="${shainBean.name}"/>" readonly>
			</div>
			
			<div class="form-group">
				<label class="form-label">カナ氏名</label>
				<input type="text" class="form-input" value="<c:out value="${shainBean.namekana}"/>" readonly>
			</div>

			<div class="form-group">
				<label class="form-label">入社年</label>
				<input type="text" class="form-input" value="<c:out value="${shainBean.entryyear}"/>" readonly>
			</div>

			<div class="form-group">
				<label class="form-label">性別</label>
				<input type="text" class="form-input" value="<c:out value="${shainBean.gender}"/>" readonly>
			</div>

			<div class="form-group">
				<label class="form-label">役職</label>
				<input type="text" class="form-input" value="<c:out value="${shainBean.jobclass}"/>" readonly>
			</div>

			<div
				style="display: flex; justify-content: space-between; margin-top: 40px;">
				<a href="ShainList" class="btn btn-secondary">‹ キャンセル</a>
				<button type="submit" class="btn btn-danger">🗑️ 削除</button>
			</div>
		</form>
	</div>
</body>
</html>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>社員登録画面</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
	<div class="card" style="max-width: 600px;">
		<div class="text-center" style="margin-bottom: 2rem;">
			<h1>新しい仲間を登録</h1>
			<p>新しい社員の情報を入力してください。</p>
		</div>
		<form action="ShainInsertComplete" method="post">
			<div class="form-group">
				<label for="id" class="form-label">社員ID</label> <input type="text"
					name="id" value="<c:out value='${nextId}'/>" class="form-input"
					readonly>
			</div>
			<div class="form-group">
				<label for="name" class="form-label">名前</label> <input type="text"
					id="name" name="name" class="form-input" required>
			</div>
			<div class="form-group">
				<label for="namekana" class="form-label">カナ氏名</label> <input
					type="text" id="namekana" name="namekana" class="form-input"
					required>
			</div>
			<div class="form-group">
				<label for="entryyear" class="form-label">入社年</label> <select
					id="entryyear" name="entryyear" class="form-input" required>
					<option value="">選択してください</option>
					<c:forEach var="entryyear" items="${yearList}">
						<option value="<c:out value="${entryyear}"/>">
							<c:out value="${entryyear}" />
						</option>
					</c:forEach>
				</select>
			</div>
			<div class="form-group">
				<label for="gender" class="form-label">性別</label> <select id="gender"
					name="gender" class="form-input" required>
					<option value="">選択してください</option>
					<option value="男">男</option>
					<option value="女">女</option>
				</select>
			</div>
			<div class="form-group">
				<label for="jobclass" class="form-label">役職</label> <select
					id="jobclass" name="jobclass" class="form-input" required>
					<option value="">選択してください</option>
					<c:forEach var="jobclass" items="${jobList}">
						<option value="<c:out value="${jobclass.name}"/>">
							<c:out value="${jobclass.name}" />
						</option>
					</c:forEach>
				</select>
			</div>

			<div
				style="display: flex; justify-content: space-between; margin-top: 40px;">
				<a href="${pageContext.request.contextPath}/ShainList"
					class="btn btn-secondary">‹ 戻る</a>
				<button type="submit" class="btn btn-primary">✓ 登録</button>
			</div>
		</form>
	</div>
</body>
</html>
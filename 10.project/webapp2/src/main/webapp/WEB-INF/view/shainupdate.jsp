<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@page import="beans.*"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>社員更新画面</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
	<div class="card" style="max-width: 600px;">
		<div class="text-center" style="margin-bottom: 2rem;">
			<h1>社員情報の更新</h1>
			<p>
				<c:out value="${shainBean.name}" />
				さんの情報を編集します。
			</p>
		</div>
		<form action="ShainUpdateComplete" method="post">
			<input type="hidden" name="id"
				value="<c:out value="${shainBean.id}"/>">

			<div class="form-group">
				<label class="form-label">社員ID</label> <input type="text"
					class="form-input" value="<c:out value="${shainBean.id}"/>"
					readonly>
			</div>

			<div class="form-group">
				<label for="name" class="form-label">名前</label> <input type="text"
					id="name" name="name" value="<c:out value="${shainBean.name}"/>"
					class="form-input" required>
			</div>
			
			<div class="form-group">
				<label for="namekana" class="form-label">カナ氏名</label> <input type="text"
					id="namekana" name="namekana" value="<c:out value="${shainBean.namekana}"/>"
					class="form-input" required>
			</div>

			<div class="form-group">
				<label for="entryyear" class="form-label">入社年</label> <input
					type="number" id="entryyear" name="entryyear"
					value="<c:out value="${shainBean.entryyear}"/>" class="form-input"
					required>
			</div>

			<div class="form-group">
				<label for="gender" class="form-label">性別</label> <select id="gender"
					name="gender" class="form-input" required>
					<option value="男"
						<c:if test="${shainBean.gender == '男'}">selected</c:if>>男</option>
					<option value="女"
						<c:if test="${shainBean.gender == '女'}">selected</c:if>>女</option>
				</select>
			</div>

			<div class="form-group">
				<label for="jobclass" class="form-label">役職</label> <select
					id="jobclass" name="jobclass" class="form-input" required>
					<c:forEach var="job" items="${jobclassList}">
						<option value="<c:out value="${job.name}"/>"
							<c:if test="${shainBean.jobclass == job.name}">selected</c:if>>
							<c:out value="${job.name}" />
						</option>
					</c:forEach>
				</select>
			</div>

			<div style="display: flex; justify-content: space-between; margin-top: 40px;">
				<a href="ShainList" class="btn btn-secondary">‹ キャンセル</a>
				<button type="submit" class="btn btn-primary">✓ 更新</button>
			</div>
		</form>
	</div>
</body>
</html>
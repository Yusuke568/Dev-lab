<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@page import="java.util.ArrayList"%>
<%@page import="beans.*"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<title>社員一覧</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
	<div class="card">
		<div class="text-center" style="margin-bottom: 2rem;">
			<h1>社員一覧</h1>
			<p>登録されている社員の一覧です。ここから情報の編集や削除ができます。</p>
		</div>

		<div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; flex-wrap: wrap; gap: 10px;">
			<div>
				<a href="${pageContext.request.contextPath}/ShainMenu" class="btn btn-secondary">‹ メニューに戻る</a>
			</div>
			<div style="display: flex; gap: 10px;">
				<a href="${pageContext.request.contextPath}/PaidLeaveAdmin" class="btn btn-secondary">有給管理</a>
				<a href="${pageContext.request.contextPath}/ShainInsert" class="btn btn-primary">+ 新規社員を登録</a>
			</div>
		</div>

		<table class="table">
			<thead>
				<tr>
					<th>社員ID</th>
					<th>名前</th>
					<th>カナ氏名</th>
					<th>入社年</th>
					<th>性別</th>
					<th>役職</th>
					<th style="text-align: center;">操作</th>
				</tr>
			</thead>
			<tbody>

				<c:forEach var="shainBean" items="${shainList}">
					<tr>
						<td><c:out value="${shainBean.id}" /></td>
						<td><c:out value="${shainBean.name}" /></td>
						<td><c:out value="${shainBean.namekana}" /></td>
						<td><c:out value="${shainBean.entryyear}" /></td>
						<td><c:out value="${shainBean.gender}" /></td>
						<td><c:out value="${shainBean.jobclass}" /></td>
						<td style="text-align: center; display: flex; gap: 8px;">
							<a href="<c:url value='ShainUpdate'>
								<c:param name='id' value='${shainBean.id}'/>
							</c:url>"
							class="btn btn-secondary" style="padding: 8px 16px;">変更</a>
							<a href="<c:url value='ShainDelete'>
								<c:param name='id' value='${shainBean.id}'/>
							</c:url>"
							class="btn btn-danger" style="padding: 8px 16px;">削除</a>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>
</body>
</html>

<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>有給付与ルール</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
<%@ include file="../_icons.jspf" %>
</head>
<body>
<div class="card">
	<div class="text-center page-title-container">
		<h1>有給付与ルール</h1>
		<p>勤続年数ごとの有給付与日数を設定します。有給休暇管理の「年度更新」で使われます。</p>
	</div>

	<%@ include file="_header.jspf" %>

	<table class="table">
		<thead>
			<tr>
				<th>勤続年数</th>
				<th>付与日数</th>
				<th class="text-center">操作</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="r" items="${rules}">
				<tr>
					<td><c:out value="${r.yearsOfService}" /> 年</td>
					<td><input type="number" name="grantDays" form="l${r.yearsOfService}" class="form-input" min="0" value="${r.grantDays}" style="width: 6rem;" required> 日</td>
					<td class="text-center">
						<div style="display: flex; gap: 0.5rem; justify-content: center;">
							<form id="l${r.yearsOfService}" action="${pageContext.request.contextPath}/masterLeaveRule.do" method="post">
								<input type="hidden" name="yearsOfService" value="${r.yearsOfService}">
								<button type="submit" class="btn btn-primary">保存</button>
							</form>
							<form action="${pageContext.request.contextPath}/masterLeaveRule.do" method="post"
								onsubmit="return confirm('勤続${r.yearsOfService}年のルールを削除しますか？');">
								<input type="hidden" name="mode" value="delete">
								<input type="hidden" name="yearsOfService" value="${r.yearsOfService}">
								<button type="submit" class="btn btn-danger">削除</button>
							</form>
						</div>
					</td>
				</tr>
			</c:forEach>
			<tr>
				<td><input type="number" name="yearsOfService" form="lNew" class="form-input" min="0" placeholder="年数" style="width: 6rem;" required> 年</td>
				<td><input type="number" name="grantDays" form="lNew" class="form-input" min="0" placeholder="日数" style="width: 6rem;" required> 日</td>
				<td class="text-center">
					<form id="lNew" action="${pageContext.request.contextPath}/masterLeaveRule.do" method="post">
						<button type="submit" class="btn btn-secondary">追加</button>
					</form>
				</td>
			</tr>
		</tbody>
	</table>
	<p style="font-size: 0.85rem; color: #666;">※ 同じ勤続年数で追加すると、その年数の付与日数が上書きされます。</p>
</div>
</body>
</html>

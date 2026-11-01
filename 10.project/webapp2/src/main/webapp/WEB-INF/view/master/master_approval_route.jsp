<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>承認ルート設定</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="card">
	<div class="text-center page-title-container">
		<h1>承認ルート設定</h1>
		<p>申請者の所属部署の承認者を1段目とし、上位部署へ何段までたどって承認するかを申請種別ごとに設定します。<br>
		承認者が未設定の部署や、申請者本人が承認者の部署は飛ばして数えます。</p>
	</div>

	<%@ include file="_header.jspf" %>

	<table class="table">
		<thead>
			<tr>
				<th>申請種別</th>
				<th>承認段数</th>
				<th class="text-center">操作</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="s" items="${settings}">
				<tr>
					<td><c:out value="${s.label}" /></td>
					<td><input type="number" name="maxLevels" form="r${s.requestTypeCode}" class="form-input" min="1" max="10" value="${s.maxLevels}" style="width: 6rem;" required> 段</td>
					<td class="text-center">
						<form id="r${s.requestTypeCode}" action="${pageContext.request.contextPath}/masterApprovalRoute.do" method="post">
							<input type="hidden" name="requestType" value="${s.requestTypeCode}">
							<button type="submit" class="btn btn-primary">保存</button>
						</form>
					</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
	<p style="font-size: 0.85rem; color: #666;">※ 変更は、これから出される申請（再申請を含む）から反映されます。承認待ちの申請のルートは変わりません。</p>
</div>
</body>
</html>

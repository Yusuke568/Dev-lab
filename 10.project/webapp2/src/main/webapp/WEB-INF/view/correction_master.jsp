<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>補正マスタ設定</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<div class="card">
	<div class="text-center page-title-container">
		<h1>補正マスタ設定</h1>
		<p>派遣先ごとの勤務時間補正ルールを補正CDとして登録します。勤怠入力画面で補正CDを選択すると、ここで登録した補正(通)・補正(深)の値が反映されます。</p>
	</div>

	<div class="page-header-actions">
		<a href="${pageContext.request.contextPath}/menu.do" class="btn btn-secondary">‹ メニューへ戻る</a>
	</div>

	<c:if test="${not empty errorMessage}">
		<div class="error-message" style="margin: 1rem 0;"><c:out value="${errorMessage}" /></div>
	</c:if>

	<div class="card">
		<h2>新規登録</h2>
		<form action="${pageContext.request.contextPath}/correctionMasterRegister.do" method="post"
			style="display: flex; flex-wrap: wrap; gap: 1rem; align-items: flex-end;">
			<div class="form-group">
				<label class="form-label">補正名称</label>
				<input type="text" name="name" class="form-input" placeholder="例: A社基準補正" required>
			</div>
			<div class="form-group">
				<label class="form-label">補正(通)</label>
				<input type="time" name="correctionUsTime" class="form-input" value="00:00">
			</div>
			<div class="form-group">
				<label class="form-label">補正(深)</label>
				<input type="time" name="correctionMidTime" class="form-input" value="00:00">
			</div>
			<button type="submit" class="btn btn-primary">登録</button>
		</form>
	</div>

	<h2 class="table-title">登録済み補正CD一覧</h2>
	<table class="table">
		<thead>
			<tr>
				<th>補正CD</th>
				<th>補正名称</th>
				<th>補正(通)</th>
				<th>補正(深)</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="cm" items="${correctionMasters}">
				<tr>
					<td><c:out value="${cm.id}" /></td>
					<td><c:out value="${cm.name}" /></td>
					<td><c:out value="${cm.correctionUsTimeFormatted}" /></td>
					<td><c:out value="${cm.correctionMidTimeFormatted}" /></td>
				</tr>
			</c:forEach>
			<c:if test="${empty correctionMasters}">
				<tr>
					<td colspan="4" class="text-center">補正CDはまだ登録されていません。</td>
				</tr>
			</c:if>
		</tbody>
	</table>
</div>
</body>
</html>

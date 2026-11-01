<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>適用マスタ</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="card">
	<div class="text-center page-title-container">
		<h1>適用マスタ</h1>
		<p>勤怠入力で選ぶ勤務区分を設定します。「労働」がない区分は、休暇申請で選べます。</p>
	</div>

	<%@ include file="_header.jspf" %>

	<table class="table">
		<thead>
			<tr>
				<th>ID</th>
				<th>区分名</th>
				<th class="text-center">労働</th>
				<th class="text-center">有給</th>
				<th class="text-center">法定休日</th>
				<th class="text-center">所定休日</th>
				<th class="text-center">操作</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="a" items="${abstracts}">
				<tr>
					<td><c:out value="${a.id}" /></td>
					<td><input type="text" name="name" form="a${a.id}" class="form-input" value="<c:out value='${a.name}'/>" required></td>
					<td class="text-center"><input type="checkbox" name="isWork" form="a${a.id}" <c:if test="${a.work}">checked</c:if>></td>
					<td class="text-center"><input type="checkbox" name="isPaid" form="a${a.id}" <c:if test="${a.paid}">checked</c:if>></td>
					<td class="text-center"><input type="checkbox" name="isLegalHoliday" form="a${a.id}" <c:if test="${a.legalHoliday}">checked</c:if>></td>
					<td class="text-center"><input type="checkbox" name="isPrescribedHoliday" form="a${a.id}" <c:if test="${a.prescribedHoliday}">checked</c:if>></td>
					<td class="text-center">
						<form id="a${a.id}" action="${pageContext.request.contextPath}/masterAbstract.do" method="post">
							<input type="hidden" name="id" value="${a.id}">
							<button type="submit" class="btn btn-primary">保存</button>
						</form>
					</td>
				</tr>
			</c:forEach>
			<tr>
				<td><input type="number" name="id" form="aNew" class="form-input" min="0" placeholder="ID" style="width: 5rem;" required></td>
				<td><input type="text" name="name" form="aNew" class="form-input" placeholder="例: 慶弔" required></td>
				<td class="text-center"><input type="checkbox" name="isWork" form="aNew"></td>
				<td class="text-center"><input type="checkbox" name="isPaid" form="aNew"></td>
				<td class="text-center"><input type="checkbox" name="isLegalHoliday" form="aNew"></td>
				<td class="text-center"><input type="checkbox" name="isPrescribedHoliday" form="aNew"></td>
				<td class="text-center">
					<form id="aNew" action="${pageContext.request.contextPath}/masterAbstract.do" method="post">
						<input type="hidden" name="mode" value="new">
						<button type="submit" class="btn btn-secondary">追加</button>
					</form>
				</td>
			</tr>
		</tbody>
	</table>
	<p style="font-size: 0.85rem; color: #666;">※ ID は勤怠データに保存されるため、追加後は変更できません。勤怠計算では ID 1=出勤、10=休日 などを前提にしている箇所があるので、既存の ID の意味は変えないでください。</p>
</div>
</body>
</html>

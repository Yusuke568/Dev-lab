<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<title>会社休日</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
<style>
.year-nav { display: flex; align-items: center; gap: 1rem; margin: 1rem 0; }
.year-nav strong { font-size: 1.2rem; }
.holiday-form { display: flex; flex-wrap: wrap; gap: 0.75rem 1rem; align-items: flex-end; }
.holiday-form .form-group { margin-bottom: 0; }
.national-list { columns: 2; font-size: 0.9rem; color: #4a5568; padding-left: 1.2em; }
@media (max-width: 600px) {
	.holiday-form .form-group, .holiday-form .btn { flex: 1 1 100%; }
	.national-list { columns: 1; }
}
</style>
</head>
<body>
<div class="card">
	<div class="text-center page-title-container">
		<h1>会社休日</h1>
		<p>年末年始・創立記念日など、国民の祝日以外の会社の休日を登録します。勤怠画面で祝日と同じく色が付き、まだ作られていない月の行は「休日」になります。</p>
	</div>

	<%@ include file="_header.jspf" %>

	<div class="year-nav">
		<a class="btn btn-secondary" href="${pageContext.request.contextPath}/masterCompanyHoliday.do?year=${year - 1}">◀ ${year - 1}年</a>
		<strong>${year}年</strong>
		<a class="btn btn-secondary" href="${pageContext.request.contextPath}/masterCompanyHoliday.do?year=${year + 1}">${year + 1}年 ▶</a>
	</div>

	<h2>登録する</h2>
	<form class="holiday-form" action="${pageContext.request.contextPath}/masterCompanyHoliday.do" method="post">
		<div class="form-group">
			<label class="form-label" for="from">日付（開始）</label>
			<input type="date" id="from" name="from" class="form-input" required>
		</div>
		<div class="form-group">
			<label class="form-label" for="to">終了日（期間で登録するとき）</label>
			<input type="date" id="to" name="to" class="form-input">
		</div>
		<div class="form-group">
			<label class="form-label" for="name">名前</label>
			<input type="text" id="name" name="name" class="form-input" maxlength="100" placeholder="例: 年末年始休暇" required>
		</div>
		<button type="submit" class="btn btn-primary">登録</button>
	</form>
	<p style="font-size: 0.85rem; color: #666;">※ 期間で登録すると、その間の毎日（土日も含む）を会社休日にします。同じ日付があれば名前を上書きします。一度に31日まで。</p>

	<h2>${year}年の会社休日</h2>
	<c:choose>
		<c:when test="${empty holidays}">
			<p>登録されていません。</p>
		</c:when>
		<c:otherwise>
			<table class="table">
				<thead>
					<tr>
						<th>日付</th>
						<th>曜日</th>
						<th>名前</th>
						<th class="text-center">操作</th>
					</tr>
				</thead>
				<tbody>
					<c:forEach var="h" items="${holidays}">
						<tr>
							<td><c:out value="${h.date}" /></td>
							<td><c:out value="${h.dayOfWeekLabel}" /></td>
							<td><c:out value="${h.name}" /></td>
							<td class="text-center">
								<form action="${pageContext.request.contextPath}/masterCompanyHoliday.do" method="post"
									onsubmit="return confirm('<c:out value="${h.date}" /> を会社休日から外しますか？');">
									<input type="hidden" name="mode" value="delete">
									<input type="hidden" name="date" value="${h.date}">
									<button type="submit" class="btn btn-danger">外す</button>
								</form>
							</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
		</c:otherwise>
	</c:choose>

	<h2>${year}年の国民の祝日（参考・自動で反映されるので登録は不要）</h2>
	<ul class="national-list">
		<c:forEach var="n" items="${nationalHolidays}">
			<li><c:out value="${n.key}" />　<c:out value="${n.value}" /></li>
		</c:forEach>
	</ul>
</div>
</body>
</html>

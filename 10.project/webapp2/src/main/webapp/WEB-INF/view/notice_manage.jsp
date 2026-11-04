<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<title>配下へのお知らせ</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
<style>
.notice-form textarea { min-height: 8rem; resize: vertical; }
.notice-form .form-row { display: flex; flex-wrap: wrap; gap: 0.75rem 1rem; }
.notice-form .form-row .form-group { flex: 1 1 220px; }
.body-count { font-size: 0.8rem; color: #718096; text-align: right; }
.notice-list { list-style: none; margin: 0; padding: 0; }
.notice-list li { border: 1px solid #e2e8f0; border-radius: 6px; padding: 10px 12px; margin-bottom: 10px; }
.notice-list .meta { font-size: 0.8rem; color: #4a5568; margin-bottom: 4px; }
.notice-list .body { white-space: pre-wrap; word-break: break-word; }
.notice-list .expired { color: #a0aec0; }
.notice-list form { margin-top: 6px; text-align: right; }
</style>
</head>
<body>
<div class="card">
	<div class="text-center page-title-container">
		<h1>配下へのお知らせ</h1>
		<p>組織図で自分が承認者になっている部署（とその下の部署）のメンバーに、お知らせを出します。メンバーの勤怠画面の上部にある「上長からのお知らせ」欄に表示されます。</p>
	</div>

	<div class="page-header-actions">
		<a href="${pageContext.request.contextPath}/menu.do" class="btn btn-secondary">‹ メニューに戻る</a>
	</div>

	<c:if test="${not empty message}">
		<div class="status-item status-info" style="margin: 1rem 0;">✓ <c:out value="${message}" /></div>
	</c:if>
	<c:if test="${not empty errorMessage}">
		<div class="error-message" style="margin: 1rem 0; color: #c50000; font-weight: 700;"><c:out value="${errorMessage}" /></div>
	</c:if>

	<c:choose>
		<c:when test="${empty departments}">
			<p>お知らせを出せる部署がありません。マスタ管理の「組織図」で、自分を部署の承認者に設定してください。</p>
		</c:when>
		<c:otherwise>
			<h2>新しいお知らせ</h2>
			<form class="notice-form" action="${pageContext.request.contextPath}/noticeManage.do" method="post">
				<input type="hidden" name="_csrf" value="<c:out value='${sessionScope.csrfToken}' />">
				<div class="form-row">
					<div class="form-group">
						<label class="form-label" for="departmentId">宛先（その部署と下の部署のメンバー）</label>
						<select id="departmentId" name="departmentId" class="form-input" required>
							<c:forEach var="d" items="${departments}">
								<option value="${d.id}"><c:out value="${d.name}" /></option>
							</c:forEach>
						</select>
					</div>
					<div class="form-group">
						<label class="form-label" for="expiresOn">掲載期限（この日まで表示。空欄なら取り下げるまで）</label>
						<input type="date" id="expiresOn" name="expiresOn" class="form-input" min="${today}">
					</div>
				</div>
				<div class="form-group">
					<label class="form-label" for="body">本文（500文字まで。改行できます）</label>
					<textarea id="body" name="body" class="form-input" maxlength="500" required
						placeholder="例：今月の作業報告書は 11/30 までに提出してください。"></textarea>
					<div class="body-count" id="body-count">0 / 500</div>
				</div>
				<button type="submit" class="btn btn-primary">お知らせを出す</button>
			</form>
		</c:otherwise>
	</c:choose>

	<h2>出したお知らせ</h2>
	<c:choose>
		<c:when test="${empty notices}">
			<p>まだありません。</p>
		</c:when>
		<c:otherwise>
			<ul class="notice-list">
				<c:forEach var="n" items="${notices}">
					<li>
						<div class="meta">
							<c:out value="${n.createdAtLabel}" />　宛先：<c:out value="${n.departmentName}" />
							<c:choose>
								<c:when test="${empty n.expiresOn}">　掲載：取り下げるまで</c:when>
								<c:when test="${n.expiresOn lt today}"><span class="expired">　掲載終了（<c:out value="${n.expiresOn}" />まで）</span></c:when>
								<c:otherwise>　掲載：<c:out value="${n.expiresOn}" />まで</c:otherwise>
							</c:choose>
						</div>
						<div class="body"><c:out value="${n.body}" /></div>
						<form action="${pageContext.request.contextPath}/noticeManage.do" method="post"
							onsubmit="return confirm('このお知らせを取り下げますか？');">
							<input type="hidden" name="_csrf" value="<c:out value='${sessionScope.csrfToken}' />">
							<input type="hidden" name="mode" value="delete">
							<input type="hidden" name="id" value="${n.id}">
							<button type="submit" class="btn btn-danger">取り下げる</button>
						</form>
					</li>
				</c:forEach>
			</ul>
		</c:otherwise>
	</c:choose>
</div>
<script>
(function () {
    const body = document.getElementById('body');
    const count = document.getElementById('body-count');
    if (!body || !count) return;
    const update = () => { count.textContent = body.value.length + ' / 500'; };
    body.addEventListener('input', update);
    update();
})();
</script>
</body>
</html>

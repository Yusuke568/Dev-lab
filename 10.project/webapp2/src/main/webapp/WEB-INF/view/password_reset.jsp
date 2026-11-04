<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<title>パスワード初期化</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
<%@ include file="_icons.jspf" %>
</head>
<body>
	<div class="card password-card">
		<h1>パスワード初期化</h1>
		<p>
			<strong><c:out value="${shain.name}" /></strong>さん（社員ID：<c:out value="${shain.id.value}" />）のパスワードを初期化します。
			本人は次回ログイン時に、新しいパスワードへの変更を求められます。
		</p>

		<c:if test="${not empty errors}">
			<div class="status-banner status-danger">
				<ul class="error-list">
					<c:forEach var="error" items="${errors}">
						<li><c:out value="${error}" /></li>
					</c:forEach>
				</ul>
			</div>
		</c:if>

		<form id="resetForm" action="${pageContext.request.contextPath}/passwordResetExecute.do" method="post"
			autocomplete="off" data-name="<c:out value='${shain.name}' />">
			<input type="hidden" name="_csrf" value="<c:out value='${sessionScope.csrfToken}' />">
			<input type="hidden" name="id" value="<c:out value='${shain.id.value}' />">

			<div class="form-group">
				<label class="radio-line">
					<input type="radio" name="mode" value="generate" <c:if test="${param.mode != 'specify'}">checked</c:if>>
					ランダムな仮パスワードを作る（完了画面に一度だけ表示します）
				</label>
				<label class="radio-line">
					<input type="radio" name="mode" value="specify" <c:if test="${param.mode == 'specify'}">checked</c:if>>
					パスワードを指定する
				</label>
			</div>

			<div id="specifyFields">
				<div class="form-group">
					<label for="newPassword" class="form-label">新しいパスワード（8文字以上）</label>
					<input type="password" id="newPassword" name="newPassword" class="form-input"
						autocomplete="new-password" minlength="8">
				</div>
				<div class="form-group">
					<label for="confirmPassword" class="form-label">新しいパスワード（確認）</label>
					<input type="password" id="confirmPassword" name="confirmPassword" class="form-input"
						autocomplete="new-password" minlength="8">
				</div>
			</div>

			<div class="form-buttons">
				<button type="submit" class="btn btn-danger">初期化する</button>
				<a href="${pageContext.request.contextPath}/shainList.do" class="btn">社員一覧に戻る</a>
			</div>
		</form>
	</div>

<script>
(function () {
    const form = document.getElementById('resetForm');
    const fields = document.getElementById('specifyFields');
    function update() {
        const specify = form.querySelector('input[name="mode"]:checked').value === 'specify';
        fields.style.display = specify ? '' : 'none';
        fields.querySelectorAll('input').forEach(function (input) { input.required = specify; });
    }
    form.querySelectorAll('input[name="mode"]').forEach(function (radio) { radio.addEventListener('change', update); });
    update();
    form.addEventListener('submit', function (event) {
        if (!confirm(form.dataset.name + 'さんのパスワードを初期化しますか？')) {
            event.preventDefault();
        }
    });
})();
</script>
</body>
</html>

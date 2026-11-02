<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<title>社員登録画面</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
	<div class="card form-card">
		<div class="text-center page-title-container">
			<h1>新しい仲間を登録</h1>
			<p>新しい社員の情報を入力してください。</p>
		</div>

		<c:if test="${not empty errors}">
			<div class="status-banner status-danger">
				<ul class="error-list">
					<c:forEach var="error" items="${errors}">
						<li><c:out value="${error}" /></li>
					</c:forEach>
				</ul>
			</div>
		</c:if>

		<%-- 入力エラーで戻ったときは、パスワード以外の入力値を param から戻す --%>
		<form id="shainInsertForm" action="${pageContext.request.contextPath}/shainInsertExecute.do" method="post">
			<input type="hidden" name="_csrf" value="<c:out value='${sessionScope.csrfToken}' />">
			<div class="form-group">
				<label for="id" class="form-label">社員ID</label> <input type="text"
					name="id" value="<c:out value='${nextId}'/>" class="form-input"
					readonly>
			</div>
			<div class="form-group">
				<label for="name" class="form-label">名前</label> <input type="text"
					id="name" name="name" class="form-input" value="<c:out value='${param.name}'/>" required>
			</div>
			<div class="form-group">
				<label for="namekana" class="form-label">カナ氏名</label> <input
					type="text" id="namekana" name="namekana" class="form-input"
					value="<c:out value='${param.namekana}'/>" required>
			</div>
			<div class="form-group">
				<label for="entryyear" class="form-label">入社年</label> <select
					id="entryyear" name="entryyear" class="form-input" required>
					<option value="">選択してください</option>
					<c:forEach var="entryyear" items="${yearList}">
						<option value="<c:out value="${entryyear}"/>" <c:if test="${param.entryyear == entryyear}">selected</c:if>>
							<c:out value="${entryyear}" />
						</option>
					</c:forEach>
				</select>
			</div>
			<div class="form-group">
				<label for="gender" class="form-label">性別</label> <select id="gender"
					name="gender" class="form-input" required>
					<option value="">選択してください</option>
					<option value="男" <c:if test="${param.gender == '男'}">selected</c:if>>男</option>
					<option value="女" <c:if test="${param.gender == '女'}">selected</c:if>>女</option>
				</select>
			</div>
			<div class="form-group">
				<label for="jobclass" class="form-label">役職</label> <select
					id="jobclass" name="jobclass" class="form-input" required>
					<option value="">選択してください</option>
					<c:forEach var="jobclass" items="${jobList}">
						<option value="<c:out value="${jobclass.name}"/>" <c:if test="${param.jobclass == jobclass.name}">selected</c:if>>
							<c:out value="${jobclass.name}" />
						</option>
					</c:forEach>
				</select>
			</div>

			<div class="form-group">
				<label for="departmentId" class="form-label">所属部署</label> <select
					id="departmentId" name="departmentId" class="form-input">
					<option value="">（未所属）</option>
					<c:forEach var="d" items="${departments}">
						<option value="${d.id}" <c:if test="${param.departmentId == d.id}">selected</c:if>><c:out value="${d.indent}" /><c:out value="${d.name}" /></option>
					</c:forEach>
				</select>
			</div>

			<h2>ログイン用アカウント</h2>
			<p>ユーザー名は社員IDになります。初回ログイン時に、本人にパスワードを変更してもらいます。</p>
			<div class="form-group">
				<label class="radio-line">
					<input type="checkbox" id="generatePassword" name="generatePassword" value="1"
						<c:if test="${empty param.name or not empty param.generatePassword}">checked</c:if>>
					初期パスワードを自動で作る（登録後の画面に一度だけ表示します）
				</label>
			</div>
			<div id="initialPasswordFields">
				<div class="form-group">
					<label for="initialPassword" class="form-label">初期パスワード（8文字以上）</label>
					<input type="password" id="initialPassword" name="initialPassword" class="form-input"
						autocomplete="new-password" minlength="8">
				</div>
				<div class="form-group">
					<label for="initialPasswordConfirm" class="form-label">初期パスワード（確認）</label>
					<input type="password" id="initialPasswordConfirm" name="initialPasswordConfirm" class="form-input"
						autocomplete="new-password" minlength="8">
				</div>
			</div>

			<div class="form-actions">
				<a href="${pageContext.request.contextPath}/shainList.do"
					class="btn btn-secondary">‹ 戻る</a>
				<button type="submit" class="btn btn-primary">✓ 登録</button>
			</div>
		</form>
	</div>

<script>
(function () {
    const checkbox = document.getElementById('generatePassword');
    const fields = document.getElementById('initialPasswordFields');
    function update() {
        const specify = !checkbox.checked;
        fields.style.display = specify ? '' : 'none';
        fields.querySelectorAll('input').forEach(function (input) { input.required = specify; });
    }
    checkbox.addEventListener('change', update);
    update();
})();
</script>
</body>
</html>

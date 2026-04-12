<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
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
		<div class="text-center page-title-container">
			<h1>社員一覧</h1>
			<p>登録されている社員の一覧です。ここから情報の編集や削除ができます。</p>
		</div>

		<div class="page-actions">
			<div>
				<a href="${pageContext.request.contextPath}/menu.do" class="btn btn-secondary">‹ メニューに戻る</a>
			</div>
			<div class="action-group">
				<a href="${pageContext.request.contextPath}/paidLeaveAdmin.do" class="btn btn-secondary">有給管理</a>
				<a href="${pageContext.request.contextPath}/shainInsertForm.do" class="btn btn-primary">+ 新規社員を登録</a>
			</div>
		</div>

		<div class="search-container">
			<input type="text" id="searchInput" class="form-control" placeholder="名前で社員を検索...">
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
					<th class="text-center">操作</th>
				</tr>
			</thead>
			<tbody id="shainTableBody">

				<c:forEach var="shain" items="${shainList}">
					<tr>
						<td><c:out value="${shain.id}" /></td>
						<td><c:out value="${shain.name}" /></td>
						<td><c:out value="${shain.namekana}" /></td>
						<td><c:out value="${shain.entryyear}" /></td>
						<td><c:out value="${shain.gender}" /></td>
						<td><c:out value="${shain.jobclass}" /></td>
						<td class="action-cell">
							<c:url value='/shainUpdateForm.do' var='updateUrl'>
								<c:param name='id' value='${shain.id}'/>
							</c:url>
							<a href="${updateUrl}" class="btn btn-secondary">変更</a>

							<c:url value='/shainDeleteForm.do' var='deleteUrl'>
								<c:param name='id' value='${shain.id}'/>
							</c:url>
							<a href="${deleteUrl}" class="btn btn-danger">削除</a>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>

<script>
document.addEventListener('DOMContentLoaded', () => {
    const searchInput = document.getElementById('searchInput');
    const tableBody = document.getElementById('shainTableBody');
    const contextPath = '${pageContext.request.contextPath}';
    let debounceTimer;

    searchInput.addEventListener('input', (e) => {
        clearTimeout(debounceTimer);
        const keyword = e.target.value;

        debounceTimer = setTimeout(() => {
            fetch(contextPath + '/searchShainApi.do?keyword=' + encodeURIComponent(keyword))
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Network response was not ok');
                    }
                    return response.json();
                })
                .then(data => {
                    updateTable(data);
                })
                .catch(error => {
                    console.error('Error fetching search results:', error);
                    tableBody.innerHTML = '<tr><td colspan="7" class="text-center">検索中にエラーが発生しました。</td></tr>';
                });
        }, 300); // 300msのデバウンス
    });

    function updateTable(shainList) {
        tableBody.innerHTML = ''; // テーブルをクリア

        if (shainList.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="7" class="text-center">該当する社員が見つかりません。</td></tr>';
            return;
        }

        shainList.forEach(shain => {
            const row = document.createElement('tr');
            
            const updateUrl = new URL(contextPath + '/shainUpdateForm.do', window.location.origin);
            updateUrl.searchParams.append('id', shain.id);

            const deleteUrl = new URL(contextPath + '/shainDeleteForm.do', window.location.origin);
            deleteUrl.searchParams.append('id', shain.id);

            row.innerHTML = `
                <td>\${escapeHTML(shain.id)}</td>
                <td>\${escapeHTML(shain.name)}</td>
                <td>\${escapeHTML(shain.namekana)}</td>
                <td>\${escapeHTML(shain.entryyear)}</td>
                <td>\${escapeHTML(shain.gender)}</td>
                <td>\${escapeHTML(shain.jobclass)}</td>
                <td class="action-cell">
                    <a href="\${updateUrl.pathname}\${updateUrl.search}" class="btn btn-secondary">変更</a>
                    <a href="\${deleteUrl.pathname}\${deleteUrl.search}" class="btn btn-danger">削除</a>
                </td>
            `;
            tableBody.appendChild(row);
        });
    }

    function escapeHTML(str) {
        if (str === null || str === undefined) return '';
        return str.toString()
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }
});
</script>
</body>
</html>

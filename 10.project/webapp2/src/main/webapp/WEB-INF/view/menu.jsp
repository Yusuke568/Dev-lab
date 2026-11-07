<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8" />
<title>機能選択画面</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
<style>
/* Additional styles for menu page */
.menu-grid {
	display: grid;
	/* 画面幅が280pxより狭いときは、カードを画面幅いっぱいにしてはみ出さないようにする */
	grid-template-columns: repeat(auto-fit, minmax(min(280px, 100%), 1fr));
	gap: 24px;
	margin-top: 2rem;
}

@media (max-width: 600px) {
	.menu-grid {
		gap: 16px;
		margin-top: 1rem;
	}

	.menu-card {
		padding: 16px;
	}

	.menu-card .icon {
		font-size: 2.25rem;
		margin-bottom: 0.5rem;
	}

	.menu-card h3 {
		margin-top: 0;
		margin-bottom: 0.75rem;
	}
}

.menu-card {
	background: var(--card-background);
	border-radius: var(--border-radius);
	box-shadow: var(--card-shadow);
	padding: 24px;
	text-align: center;
	transition: transform 0.2s, box-shadow 0.2s;
	border: 1px solid #e9ecef;
}

.menu-card:hover {
	transform: translateY(-5px);
	box-shadow: 0 15px 35px rgba(0,0,0,0.1);
}

.menu-card .icon {
	font-size: 3rem;
	margin-bottom: 1rem;
}

.menu-card h3 {
	margin-bottom: 1.5rem;
}

/* 画面右上のアカウント操作（パスワード変更・ログアウト） */
.account-bar {
	display: flex;
	justify-content: flex-end;
	gap: 8px;
	margin-bottom: 0.5rem;
}

.account-bar a {
	display: inline-flex;
	align-items: center;
	gap: 4px;
	padding: 4px 12px;
	font-size: 0.85rem;
	border: 1px solid #cbd5e0;
	border-radius: 999px;
	color: #4a5568;
	background: #fff;
	text-decoration: none;
	white-space: nowrap;
}

.account-bar a:hover {
	border-color: #90cdf4;
	color: #1a3d6d;
}
</style>
<%@ include file="_icons.jspf" %>
</head>
<body>
<div class="card">
	<div class="account-bar">
		<a href="${pageContext.request.contextPath}/passwordChange.do" title="ログインに使うパスワードを変更します">🔑 パスワード変更</a>
		<a href="${pageContext.request.contextPath}/logout.do" title="ログアウトしてログイン画面に戻ります">↩️ ログアウト</a>
	</div>
	<div class="text-center">
		<h1>メインメニュー</h1>
		<p>実行したい操作を選んでください</p>
	</div>

	<c:if test="${not empty alertMessage}">
		<div style="background-color: #fff3cd; color: #856404; padding: 1rem; border-radius: 4px; margin-top: 1rem; border: 1px solid #ffeeba;">
			<strong>お知らせ: </strong> <c:out value="${alertMessage}" />
		</div>
	</c:if>

	<div class="menu-grid">
		<!-- 勤怠管理カード -->
		<c:if test="${sessionScope.can.ATTENDANCE}">
		<div class="menu-card">
			<div class="icon">📅</div>
			<h3>勤怠管理</h3>
			<form action="${pageContext.request.contextPath}/kintaiDisplay.do"
				method="post">
				<input type="hidden" name="id" value="${sessionScope.loginUser}" />
				<div class="form-group" style="text-align: left; padding: 0.5rem 0;">
					<label class="form-label" style="display: inline;">社員ID: </label>
					<span style="font-weight: bold;"><c:out value="${sessionScope.loginUser}" /></span>
				</div>
				<div class="form-group">
					<label for="ym-select" class="form-label">年月</label>
					<select id="ym-select" class="form-input" onchange="document.getElementById('hidden-year').value = this.value.split('-')[0]; document.getElementById('hidden-month').value = this.value.split('-')[1];">
						<c:forEach var="ym" items="${availableMonths}">
							<option value="${ym.year}-${ym.monthValue}" <c:if test="${ym == currentMonth}">selected</c:if>>${ym.year}年${ym.monthValue}月</option>
						</c:forEach>
					</select>
				</div>
				<input type="hidden" name="year" id="hidden-year" value="${currentMonth.year}">
				<input type="hidden" name="month" id="hidden-month" value="${currentMonth.monthValue}">
				<button type="submit" class="btn btn-primary" style="width: 100%;">勤怠情報を表示</button>
			</form>
		</div>
		</c:if>

		<!-- 残業事前申請カード -->
		<c:if test="${sessionScope.can.OVERTIME_REQUEST}">
		<div class="menu-card-wrapper">
			<div class="menu-card" style="cursor: pointer; height: 100%;" onclick="location.href='${pageContext.request.contextPath}/overtimeRequestMenu.do'">
				<div class="icon">🕐</div>
				<h3>残業事前申請</h3>
				<p>時間外労働の事前申請・承認結果の確認・再申請を行います。</p>
			</div>
		</div>
		</c:if>

		<!-- 休暇申請カード -->
		<c:if test="${sessionScope.can.LEAVE_REQUEST}">
		<div class="menu-card-wrapper">
			<div class="menu-card" style="cursor: pointer; height: 100%;" onclick="location.href='${pageContext.request.contextPath}/leaveRequestMenu.do'">
				<div class="icon">🌴</div>
				<h3>休暇申請</h3>
				<p>有給・代休などの休暇を申請し、承認状況を確認します。</p>
			</div>
		</div>
		</c:if>

		<c:if test="${sessionScope.can.SHAIN_ADMIN}">
			<!-- 社員一覧カード -->
			<div class="menu-card-wrapper">
				<div class="menu-card" style="cursor: pointer; height: 100%;" onclick="location.href='${pageContext.request.contextPath}/shainList.do'">
					<div class="icon">👥</div>
					<h3>社員一覧</h3>
					<p>社員情報の確認や編集を行います。</p>
				</div>
			</div>
		</c:if>

		<c:if test="${sessionScope.can.PAID_LEAVE_ADMIN}">
			<!-- 有給休暇管理カード -->
			<div class="menu-card-wrapper">
				<div class="menu-card" style="cursor: pointer; height: 100%;" onclick="location.href='${pageContext.request.contextPath}/paidLeaveAdmin.do'">
					<div class="icon">🏖️</div>
					<h3>有給休暇管理</h3>
					<p>有給休暇の付与や日数の管理を行います。</p>
				</div>
			</div>
		</c:if>

		<c:if test="${sessionScope.can.APPROVAL}">
			<!-- 勤怠承認カード -->
			<div class="menu-card-wrapper">
				<div class="menu-card" style="cursor: pointer; height: 100%;" onclick="location.href='${pageContext.request.contextPath}/approvalList.do'">
					<div class="icon">✅</div>
					<h3>勤怠承認</h3>
					<p>自分宛ての残業・休暇申請と、部下の勤怠を承認・却下します。</p>
				</div>
			</div>

			<!-- 部下へのお知らせカード -->
			<div class="menu-card-wrapper">
				<div class="menu-card" style="cursor: pointer; height: 100%;" onclick="location.href='${pageContext.request.contextPath}/noticeManage.do'">
					<div class="icon">📢</div>
					<h3>部下へのお知らせ</h3>
					<p>組織図で自分の部下の勤怠画面に、お知らせを出します。</p>
				</div>
			</div>

			<!-- 部下の勤怠表カード -->
			<div class="menu-card-wrapper">
				<div class="menu-card" style="cursor: pointer; height: 100%;" onclick="location.href='${pageContext.request.contextPath}/subordinateAttendance.do'">
					<div class="icon">👥</div>
					<h3>部下の勤怠表</h3>
					<p>組織図で自分の部下の勤怠表を参照・編集します。</p>
				</div>
			</div>
		</c:if>

		<c:if test="${sessionScope.can.ANY_MASTER}">
			<!-- マスタ管理カード -->
			<div class="menu-card-wrapper">
				<div class="menu-card" style="cursor: pointer; height: 100%;" onclick="location.href='${pageContext.request.contextPath}/masterMenu.do'">
					<div class="icon">⚙️</div>
					<h3>マスタ管理</h3>
					<p>権限グループ・役職・組織図・承認ルートなどのマスタを設定します。</p>
				</div>
			</div>
		</c:if>
			</div>
</div>
</body>
</html>

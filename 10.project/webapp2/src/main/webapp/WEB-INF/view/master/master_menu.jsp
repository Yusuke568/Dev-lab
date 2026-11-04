<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>マスタ管理</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
<style>
.master-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 1rem; margin-top: 1.5rem; }
.master-link { display: block; padding: 1.25rem; border-radius: 8px; background: var(--card-background); box-shadow: var(--card-shadow); color: inherit; text-decoration: none; }
.master-link:hover { transform: translateY(-2px); }
.master-link h3 { margin: 0 0 0.5rem; }
.master-link p { margin: 0; font-size: 0.9rem; color: #666; }
</style>
</head>
<body>
<div class="card">
	<div class="text-center page-title-container">
		<h1>マスタ管理</h1>
		<p>権限・組織図・承認ルートと、勤怠で使うマスタを設定します。表示されるのは、使う権限があるマスタだけです。</p>
	</div>

	<div class="page-header-actions">
		<a href="${pageContext.request.contextPath}/menu.do" class="btn btn-secondary">‹ メニューへ戻る</a>
	</div>

	<div class="master-grid">
		<c:if test="${sessionScope.can.MASTER_PERMISSION_GROUP}">
		<a class="master-link" href="${pageContext.request.contextPath}/masterPermissionGroup.do">
			<h3>🔐 権限グループ</h3>
			<p>グループごとに使える機能（承認・社員管理・マスタ管理など）を設定します。</p>
		</a>
		</c:if>
		<c:if test="${sessionScope.can.MASTER_JOB_CLASS}">
		<a class="master-link" href="${pageContext.request.contextPath}/masterJobClass.do">
			<h3>🎖️ 役職</h3>
			<p>役職を追加し、どの権限グループに置くかを設定します。</p>
		</a>
		</c:if>
		<c:if test="${sessionScope.can.MASTER_DEPARTMENT}">
		<a class="master-link" href="${pageContext.request.contextPath}/masterDepartment.do">
			<h3>🏢 組織図</h3>
			<p>部署の親子関係と承認者、社員の所属部署を設定します。承認ルートはここから作られます。</p>
		</a>
		</c:if>
		<c:if test="${sessionScope.can.MASTER_APPROVAL_ROUTE}">
		<a class="master-link" href="${pageContext.request.contextPath}/masterApprovalRoute.do">
			<h3>🔀 承認ルート設定</h3>
			<p>残業事前申請・休暇申請を、組織図の何段上まで承認するかを設定します。</p>
		</a>
		</c:if>
		<c:if test="${sessionScope.can.MASTER_ABSTRACT}">
		<a class="master-link" href="${pageContext.request.contextPath}/masterAbstract.do">
			<h3>📋 適用マスタ</h3>
			<p>出勤・有給・休日などの勤務区分を設定します。</p>
		</a>
		</c:if>
		<c:if test="${sessionScope.can.MASTER_LEAVE_RULE}">
		<a class="master-link" href="${pageContext.request.contextPath}/masterLeaveRule.do">
			<h3>🏖️ 有給付与ルール</h3>
			<p>勤続年数ごとの有給付与日数を設定します。</p>
		</a>
		</c:if>
		<c:if test="${sessionScope.can.MASTER_COMPANY_HOLIDAY}">
		<a class="master-link" href="${pageContext.request.contextPath}/masterCompanyHoliday.do">
			<h3>📅 会社休日</h3>
			<p>年末年始・創立記念日など、祝日以外の会社の休日を登録します。</p>
		</a>
		</c:if>
		<c:if test="${sessionScope.can.MASTER_CORRECTION}">
		<a class="master-link" href="${pageContext.request.contextPath}/correctionMasterList.do">
			<h3>⏱️ 補正CD</h3>
			<p>派遣先ごとの勤務時間補正ルールを設定します。</p>
		</a>
		</c:if>
	</div>
</div>
</body>
</html>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    // トップページへのアクセス時に SES勤怠管理画面へ自動リダイレクト
    response.sendRedirect(request.getContextPath() + "/sesAttendance.do");
%>

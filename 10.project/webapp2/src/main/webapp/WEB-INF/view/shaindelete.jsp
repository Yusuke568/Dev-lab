<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="domain.shain.Shain" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>退職・社員削除 - SES勤怠マネージャー</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tailwind.min.css">
  <style>
    body {
      background-color: #0b0f19;
      color: #f1f5f9;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Hiragino Sans", sans-serif;
    }
    .panel {
      background-color: #131b2e;
      border: 1px solid #1e293b;
      border-radius: 0.75rem;
    }
  </style>
</head>
<body class="min-h-screen flex items-center justify-center p-4">
  <%
    Shain shain = (Shain) request.getAttribute("shain");
  %>

  <div class="panel w-full max-w-md p-6 md:p-8 space-y-6 shadow-2xl border-rose-900/50">
    <div class="text-center space-y-2">
      <div class="inline-flex p-3 rounded-full bg-rose-500/10 text-rose-400 border border-rose-500/20">
        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
        </svg>
      </div>
      <h1 class="text-lg font-bold text-white">退職処理・社員削除</h1>
      <p class="text-xs text-rose-400 font-semibold">
        本当にこの社員の退職処理（アカウント無効化・削除）を実行しますか？
      </p>
    </div>

    <form action="shainDeleteExecute.do" method="post" class="space-y-4 text-xs">
      <input type="hidden" name="id" value="<%= shain != null ? shain.getId() : "" %>" />

      <div class="bg-slate-950 p-4 rounded-xl border border-slate-800 space-y-2 font-mono">
        <div class="flex justify-between">
          <span class="text-slate-500">社員ID:</span>
          <span class="text-white"><%= shain != null ? shain.getId() : "" %></span>
        </div>
        <div class="flex justify-between">
          <span class="text-slate-500">氏名:</span>
          <span class="text-white font-bold"><%= shain != null ? shain.getName() : "" %> (<%= shain != null ? shain.getNamekana() : "" %>)</span>
        </div>
        <div class="flex justify-between">
          <span class="text-slate-500">役職:</span>
          <span class="text-sky-400"><%= shain != null ? shain.getJobclass() : "" %></span>
        </div>
        <div class="flex justify-between">
          <span class="text-slate-500">入社年:</span>
          <span class="text-white"><%= shain != null ? shain.getEntryyear() : "" %>年</span>
        </div>
      </div>

      <div class="flex items-center justify-between gap-3 pt-2">
        <a href="shainList.do" class="w-full text-center py-2.5 rounded-lg bg-slate-800 text-slate-300 font-semibold hover:bg-slate-700">
          キャンセル
        </a>
        <button type="submit" class="w-full py-2.5 rounded-lg bg-rose-600 hover:bg-rose-500 font-bold text-white shadow shadow-rose-600/30">
          退職・削除を実行
        </button>
      </div>
    </form>
  </div>
</body>
</html>
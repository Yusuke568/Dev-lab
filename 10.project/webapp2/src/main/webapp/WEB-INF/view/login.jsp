<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>ログイン - SES勤怠管理システム</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tailwind.min.css">
  <style>
    body {
      background-color: #0b0f19;
      color: #f1f5f9;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Hiragino Sans", sans-serif;
    }
  </style>
</head>
<body class="flex items-center justify-center min-h-screen p-4">
  <div class="w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl p-6 md:p-8 shadow-2xl space-y-6">
    <div class="text-center space-y-2">
      <div class="inline-flex p-3 rounded-xl bg-sky-500/10 text-sky-400 border border-sky-500/20 mb-1">
        <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      </div>
      <h1 class="text-2xl font-bold tracking-tight text-white">SES勤怠・精算マネージャー</h1>
      <p class="text-xs text-slate-400">社員IDとパスワードを入力してログインしてください</p>
    </div>

    <% String error = (String) request.getAttribute("errorMessage"); %>
    <% if (error != null) { %>
      <div class="p-3 rounded-lg bg-rose-500/10 border border-rose-500/30 text-rose-400 text-xs text-center">
        <%= error %>
      </div>
    <% } %>

    <form action="loginExecute.do" method="post" class="space-y-4 text-xs">
      <div>
        <label for="username" class="block text-slate-300 font-semibold mb-1.5">社員番号 (ID)</label>
        <input type="text" id="username" name="username" placeholder="例: 1 または 668" required
               class="w-full bg-slate-950 border border-slate-700 rounded-lg px-3.5 py-2.5 text-sm text-white focus:outline-none focus:border-sky-500" />
      </div>

      <div>
        <label for="password" class="block text-slate-300 font-semibold mb-1.5">パスワード</label>
        <input type="password" id="password" name="password" placeholder="••••••••" required
               class="w-full bg-slate-950 border border-slate-700 rounded-lg px-3.5 py-2.5 text-sm text-white focus:outline-none focus:border-sky-500" />
      </div>

      <button type="submit" class="w-full bg-sky-600 hover:bg-sky-500 text-white font-bold py-3 rounded-lg text-sm shadow-lg shadow-sky-600/30 transition">
        ログイン
      </button>
    </form>

    <div class="border-t border-slate-800 pt-4 text-[11px] text-slate-500 space-y-1">
      <p class="font-semibold text-slate-400">💡 ログインアカウント例（初期パスワード: password）</p>
      <p>・一般メンバー: <span class="font-mono text-slate-300">1</span> (中島 祐介) / <span class="font-mono text-slate-300">7</span> (中村 晃)</p>
      <p>・管理者/部長: <span class="font-mono text-slate-300">668</span> (ファイル)</p>
    </div>
  </div>
</body>
</html>

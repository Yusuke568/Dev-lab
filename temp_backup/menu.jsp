<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ja">
  <head>
    <meta charset="UTF-8" />
    <title>機能選択画面</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <style>
      body {
        font-family: sans-serif;
        background-color: #f0f2f5;
        margin: 0;
        padding: 20px;
      }

      h1 {
        text-align: center;
        margin-bottom: 30px;
        font-size: 1.5rem;
      }

      .menu {
        display: flex;
        flex-wrap: wrap;
        gap: 20px;
        justify-content: center;
      }

      .card {
        background-color: white;
        padding: 20px;
        border-radius: 10px;
        width: 100%;
        max-width: 300px;
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
        text-align: center;
        cursor: pointer;
        transition: transform 0.2s;
      }

      .card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.2);
      }

      @media screen and (max-width: 600px) {
        .card {
          width: 100%;
        }
      }

    </style>
  </head>
  <body>
    <h1>機能を選択してください</h1>
    <div class="menu">
    
<div class="card" onclick="if(confirm('送信してもよろしいですか？')) document.getElementById('form1').submit();">
  📅 勤怠管理
  <form id="form1" action="ShainKintai" method="post" style="display:none;"></form>
</div>

<div class="card" onclick="if(confirm('送信してもよろしいですか？')) document.getElementById('form2').submit();">
  👥 社員一覧
  <form id="form2" action="ShainList" method="post" style="display:none;"></form>
</div>

<!--       <div class="card" onclick="location.href='レポート作成.html'"> -->
<!--         📊 レポート作成 -->
<!--       </div> -->
<!--       <div class="card" onclick="location.href='設定.html'">⚙️ 設定</div> -->
      
      <form action="ShainIndex">
      <button type="submit">ログイン画面へ</button>      
      </form>
    </div>
  </body>
</html>

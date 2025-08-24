<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
	<%@page import="java.util.ArrayList"%>
<%@page import="beans.*"%>
<!DOCTYPE html>
<html lang="ja">
  <head>
    <meta charset="UTF-8" />
    <title>ログイン画面</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <style>
      body {
        font-family: sans-serif;
        background-color: #eef2f3;
        display: flex;
        justify-content: center;
        align-items: center;
        height: 100vh;
        margin: 0;
      }

      .login-container {
        background-color: white;
        padding: 30px;
        border-radius: 10px;
        box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
        width: 100%;
        max-width: 400px;
      }

      h2 {
        text-align: center;
        margin-bottom: 20px;
      }

      input[type='text'],
      input[type='password'] {
        width: 100%;
        padding: 12px;
        margin-bottom: 15px;
        border: 1px solid #ccc;
        border-radius: 5px;
        box-sizing: border-box;
      }

      button {
        width: 100%;
        padding: 12px;
        background-color: #3498db;
        color: white;
        border: none;
        border-radius: 5px;
        font-size: 1rem;
        cursor: pointer;
      }

      button:hover {
        background-color: #2980b9;
      }
    </style>
  </head>
  <body>
    <div class="login-container">
      <h2>勤怠管理System</h2>
      <form action="ShainMenu">
        <input type="text" name="username" placeholder="ユーザー名" required />
        <input
          type="password"
          name="password"
          placeholder="パスワード"
          required
        />
          <button type="submit">ログイン</button>      
      </form>
    </div>
  </body>
</html>

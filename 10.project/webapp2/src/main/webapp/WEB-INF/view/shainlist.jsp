<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@page import="java.util.ArrayList"%>
<%@page import="beans.*"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="ja">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>社員一覧</title>
    <style>
      body {
        margin: 0;
        padding: 0;
        font-family: 'Helvetica Neue', sans-serif;
        background: linear-gradient(to right, #e0eafc, #cfdef3);
        color: #333;
      }

      .container {
        max-width: 900px;
        margin: 40px auto;
        padding: 20px;
        background-color: #fff;
        border-radius: 12px;
        box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
      }

      h2 {
        text-align: center;
        margin-bottom: 20px;
        font-size: 2rem;
        color: #2c3e50;
      }

      .top-buttons {
        display: flex;
        justify-content: flex-end;
        margin-bottom: 20px;
      }

      .bottom-button {
        text-align: center;
        margin-top: 30px;
      }

      .btn {
        padding: 10px 18px;
        border: none;
        border-radius: 6px;
        font-size: 0.9rem;
        color: #fff;
        cursor: pointer;
        transition: background-color 0.3s ease;
        margin-left: 10px;
      }

      .register {
        background-color: #2980b9;
      }

      .register:hover {
        background-color: #2471a3;
      }

      .menu {
        background-color: #7f8c8d;
      }

      .menu:hover {
        background-color: #707b7c;
      }

      table {
        width: 100%;
        border-collapse: collapse;
      }

      th,
      td {
        padding: 16px;
        text-align: center;
        border-bottom: 1px solid #eee;
      }

      th {
        background-color: #34495e;
        color: #fff;
        font-weight: 500;
        font-size: 1rem;
      }

      tr:hover {
        background-color: #f9f9f9;
      }

      .edit {
        background-color: #27ae60;
      }

      .edit:hover {
        background-color: #219150;
      }

      .delete {
        background-color: #e74c3c;
      }

      .delete:hover {
        background-color: #c0392b;
      }

      @media screen and (max-width: 600px) {
        .container {
          margin: 20px;
          padding: 15px;
        }

        h2 {
          font-size: 1.5rem;
        }

        th,
        td {
          padding: 10px;
          font-size: 0.85rem;
        }

        .btn {
          padding: 8px 12px;
          font-size: 0.8rem;
        }

        .top-buttons {
          flex-direction: column;
          align-items: flex-end;
        }

        .top-buttons .btn {
          margin: 5px 0;
        }
      }
    </style>
  </head>
  <body>
    <div class="container">
      <h2>社員一覧</h2>

      <div class="top-buttons">
		<form action="ShainInsert" method="get" style="margin-left: 10px;">
			<button type="submit" class="btn register">社員を登録する</button>
		</form>
		<form action="ShainMenu" method="get" style="margin-left: 10px;">
			<button type="submit" class="btn menu">メニューに戻る</button>
		</form>
      </div>

      <table>
        <thead>
          <tr>
            <th>社員ID</th>
            <th>名前</th>
            <th>カナ氏名</th>
            <th>入社年</th>
            <th>性別</th>
            <th>役職</th>
            <th>変更</th>
            <th>削除</th>
          </tr>
        </thead>
        <tbody>

		<c:forEach var="shainBean" items="${shainList}">
		  <tr>
		    <td><c:out value="${shainBean.id}"/></td>
		    <td><c:out value="${shainBean.name}"/></td>
		    <td><c:out value="${shainBean.namekana}"/></td>
		    <td><c:out value="${shainBean.entryyear}"/></td> 
		    <td><c:out value="${shainBean.gender}"/></td> 
		    <td><c:out value="${shainBean.jobclass}"/></td> 
		    <td>
		    	<a href="<c:url value='ShainUpdate'>
						<c:param name='id' value='${shainBean.id}'/>
					</c:url>">変更</a>
		    </td>
		    <td>
		    	<a href="<c:url value='ShainDelete'>
						<c:param name='id' value='${shainBean.id}'/>
					</c:url>">削除</a>
		    </td>
		  </tr>
		</c:forEach>
        </tbody>
      </table>
    </div>
  </body>
</html>

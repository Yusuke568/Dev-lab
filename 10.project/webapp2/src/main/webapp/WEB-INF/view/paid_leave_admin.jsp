<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>有給休暇管理</title>
<style>
    body { font-family: 'Segoe UI', 'Noto Sans JP', sans-serif; background-color: #f3f6fb; padding: 20px; }
    h1, h2 { color: #2c3e50; }
    .container { background-color: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 6px rgba(0,0,0,0.1); }
    table { width: 100%; border-collapse: collapse; margin-top: 20px; font-size: 14px; }
    th, td { border: 1px solid #ccc; padding: 10px; text-align: left; }
    th { background-color: #f2f2f2; }
    .action-section { margin-top: 20px; padding: 15px; border: 1px solid #ddd; border-radius: 5px; background-color: #f9f9f9; }
    .action-section h2 { margin-top: 0; }
    button, input[type="submit"] {
        padding: 8px 15px;
        font-size: 14px;
        border: none;
        background-color: #3498db;
        color: white;
        border-radius: 4px;
        cursor: pointer;
        margin-right: 10px;
    }
    button:hover, input[type="submit"]:hover { background-color: #2980b9; }
    input[type="number"], input[type="text"] {
        padding: 8px;
        border-radius: 4px;
        border: 1px solid #ccc;
        margin-right: 5px;
    }
    .individual-actions { display: flex; align-items: center; }
</style>
</head>
<body>
    <div class="container">
        <h1>有給休暇管理</h1>

        <!-- Action Section 1: Grant by Years of Service -->
        <div class="action-section">
            <h2>勤続年数に応じた一括付与</h2>
            <p>勤続年数テーブル（leave_grant_rules）の定義に基づいて、全社員の有給休暇を更新します。</p>
            <form action="GrantLeaveByYearServlet" method="post">
                <button type="submit">一括付与を実行</button>
            </form>
        </div>

        <!-- Action Section 2: Grant to Selected Employees -->
        <div class="action-section">
            <h2>選択した社員への一括付与</h2>
            <form action="GrantLeaveSelectedServlet" method="post">
                <input type="number" name="days" min="1" required placeholder="付与日数">
                <button type="submit">選択した社員に付与</button>
            
        </div>

        <!-- Employee List Table -->
        <h2>社員一覧</h2>
        <table id="employee-table">
            <thead>
                <tr>
                    <th><input type="checkbox" id="select-all"></th>
                    <th>社員ID</th>
                    <th>氏名</th>
                    <th>勤続年数</th>
                    <th>有給休暇残日数</th>
                    <th>個別操作</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="shain" items="${shainList}">
                    <tr>
                        <td><input type="checkbox" name="selected_ids" value="${shain.id}"></td>
                        <td><c:out value="${shain.id}"/></td>
                        <td><c:out value="${shain.name}"/></td>
                        <td><c:out value="${shain.yearsOfService}"/> 年</td>
                        <td><c:out value="${shain.paidLeaveDays}"/> 日</td>
                        <td>
                            <div class="individual-actions">
                                <input type="number" name="individual_days_${shain.id}" min="0" value="${shain.paidLeaveDays}" style="width: 60px;">
                                <button onclick="updateIndividual(${shain.id})">更新</button>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
        </form> <!-- Close form for batch selection -->
    </div>

<script>
    // Check/uncheck all checkboxes
    document.getElementById('select-all').addEventListener('change', function(e) {
        const checkboxes = document.querySelectorAll('input[name="selected_ids"]');
        checkboxes.forEach(cb => {
            cb.checked = e.target.checked;
        });
    });

    // Function for individual update
    function updateIndividual(id) {
        const days = document.querySelector(`input[name="individual_days_${id}"]`).value;
        
        // Create a form dynamically to submit the data
        const form = document.createElement('form');
        form.method = 'post';
        form.action = 'UpdateLeaveServlet'; // This servlet needs to be created

        const idInput = document.createElement('input');
        idInput.type = 'hidden';
        idInput.name = 'id';
        idInput.value = id;
        form.appendChild(idInput);

        const daysInput = document.createElement('input');
        daysInput.type = 'hidden';
        daysInput.name = 'days';
        daysInput.value = days;
        form.appendChild(daysInput);

        document.body.appendChild(form);
        form.submit();
    }
</script>
</body>
</html>

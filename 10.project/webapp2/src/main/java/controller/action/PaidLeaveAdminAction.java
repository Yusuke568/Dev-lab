package controller.action;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import controller.Action;
import controller.View;
import domain.shain.Shain;
import usecase.shain.ShainService;

/**
 * 有給管理画面表示のアクションクラス。
 */
public class PaidLeaveAdminAction implements Action {

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            ShainService shainService = new ShainService();
            List<Shain> shainList = shainService.getAllShainWithLeaveInfo();
            
            request.setAttribute("shainList", shainList);
            return new View("/WEB-INF/view/paid_leave_admin.jsp");

        } catch (SQLException | NamingException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "社員情報の取得中にエラーが発生しました。");
            return new View("/WEB-INF/view/error.jsp");
        }
    }
}

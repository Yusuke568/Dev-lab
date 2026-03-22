package com.example.controller.action;

import java.io.IOException;

import com.example.controller.Action;
import com.example.controller.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * メニュー画面表示のアクションクラス。
 */
public class MenuAction implements Action {

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // menu.jspへフォワードするためのViewオブジェクトを返す
        return new View("/WEB-INF/view/menu.jsp");
    }
}

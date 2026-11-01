package com.example.master.adapter.in.web;

import com.example.shared.web.Action;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * マスタ管理のトップ画面（各マスタへのリンク）を表示するアクション。
 */
public class MasterMenuAction implements Action {

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        return new View("/WEB-INF/view/master/master_menu.jsp");
    }
}

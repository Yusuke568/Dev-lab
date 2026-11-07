package com.example.master.adapter.in.web;

import com.example.master.application.port.in.MasterMaintenanceUseCase;
import com.example.shared.web.Action;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * マスタ管理画面の共通処理。
 * GET では一覧を表示し、POST では保存してから同じ画面へリダイレクトする（結果はセッション経由で1回だけ表示）。
 */
public abstract class MasterActionSupport implements Action {

    private static final String MESSAGE = "masterMessage";
    private static final String ERROR = "masterErrorMessage";

    protected final MasterMaintenanceUseCase masterUseCase;

    protected MasterActionSupport(MasterMaintenanceUseCase masterUseCase) {
        this.masterUseCase = masterUseCase;
    }

    /** 画面のパス（例: /masterDepartment.do）。保存後のリダイレクト先。 */
    protected abstract String selfPath();

    /** 保存後に戻る先。既定は selfPath()。表示中の条件（年など）を引き継ぎたい画面で上書きする。 */
    protected String redirectPath(HttpServletRequest request) {
        return selfPath();
    }

    /** 一覧に必要な属性を request に入れ、表示する JSP を返す。 */
    protected abstract String show(HttpServletRequest request);

    /** フォームの内容を保存し、完了メッセージを返す。 */
    protected abstract String save(HttpServletRequest request);

    /**
     * 行を削除し、完了メッセージを返す（フォームに op=delete を付けて送ったとき）。
     * 削除できる画面だけ上書きする。
     */
    protected String delete(HttpServletRequest request) {
        throw new IllegalArgumentException("この画面では削除できません。");
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            try {
                session.setAttribute(MESSAGE, "delete".equals(request.getParameter("op")) ? delete(request) : save(request));
            } catch (NumberFormatException e) {
                session.setAttribute(ERROR, "数値の項目に正しい値を入力してください。");
            } catch (Exception e) {
                session.setAttribute(ERROR, e.getMessage());
            }
            return new View(redirectPath(request), true);
        }

        moveFlash(session, request, MESSAGE, "message");
        moveFlash(session, request, ERROR, "errorMessage");
        return new View(show(request));
    }

    private static void moveFlash(HttpSession session, HttpServletRequest request, String key, String attr) {
        Object value = session.getAttribute(key);
        if (value != null) {
            request.setAttribute(attr, value);
            session.removeAttribute(key);
        }
    }

    /** 空欄なら null、それ以外は整数。 */
    protected static Integer optionalInt(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null || value.isBlank() ? null : Integer.valueOf(value.trim());
    }

    protected static int requiredInt(HttpServletRequest request, String name) {
        return Integer.parseInt(request.getParameter(name).trim());
    }

    protected static boolean checked(HttpServletRequest request, String name) {
        return request.getParameter(name) != null;
    }
}

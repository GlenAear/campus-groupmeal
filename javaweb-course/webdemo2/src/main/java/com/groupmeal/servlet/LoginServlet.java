package com.groupmeal.servlet;

import com.groupmeal.dao.UsersDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * 第 14 项：lg.html 的表单打到 /login，用数据库验证登录。
 * 合法 → 用户名存进 session，转发到 show.jsp（所有商品页）；不合法 → 跳回 lg.html。
 * 与第 10 项的区别：第 10 项是 lg.html+LoginServlet+show.jsp 的"数据库版雏形"，
 * 本项把判断从写死改成查表 —— 讲解就讲这一句。
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");   // 中文参数不乱码；必须在取参数之前
        String username = request.getParameter("username");
        String passwd = request.getParameter("passwd");
        System.out.println("===/login 收到=== username=" + username);
        try {
            UsersDao dao = new UsersDao();
            if (dao.isUser(username, passwd)) {
                HttpSession session = request.getSession();
                session.setAttribute("loginUser", username);
                request.getRequestDispatcher("/show.jsp").forward(request, response);
            } else {
                response.sendRedirect("lg.html");
            }
        } catch (Exception e) {
            throw new ServletException("数据库登录失败：" + e, e);
        }
    }
}

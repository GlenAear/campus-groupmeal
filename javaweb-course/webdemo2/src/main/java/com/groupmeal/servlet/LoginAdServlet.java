package com.groupmeal.servlet;

import com.groupmeal.dao.UsersDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 第 16 项（前半）：admin/lg.html 的表单打到 /admin/login，管理员数据库登录验证。
 * 注意 @WebServlet 的路径带 /admin/ 前缀，但类还是放在同一个 servlet 包里（老师要求的是"目录在 admin 下"）。
 */
@WebServlet("/admin/login")
public class LoginAdServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String username = request.getParameter("username");
        String passwd = request.getParameter("passwd");
        System.out.println("===/admin/login 收到=== username=" + username);
        try {
            if (new UsersDao().isAdmin(username, passwd)) {
                request.getSession().setAttribute("adUser", username);
                request.getRequestDispatcher("/admin/welcome.jsp").forward(request, response);
            } else {
                response.sendRedirect("lg.html");
            }
        } catch (Exception e) {
            throw new ServletException("管理员登录失败：" + e, e);
        }
    }
}

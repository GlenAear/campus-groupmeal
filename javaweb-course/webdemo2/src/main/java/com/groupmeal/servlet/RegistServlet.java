package com.groupmeal.servlet;

import com.groupmeal.bean.User;
import com.groupmeal.dao.UsersDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 第 15 项：regist.html 的表单打到 /regist，用数据库注册用户。
 * 先查重（isExists），没有才 insert；成功后跳 show.jsp 并显示当前用户数。
 */
@WebServlet("/regist")
public class RegistServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String username = request.getParameter("username");
        String passwd = request.getParameter("passwd");
        String email = request.getParameter("email");
        try {
            UsersDao dao = new UsersDao();
            if (dao.isExists(username)) {
                System.out.println("===/regist=== 用户名已存在，拒绝注册：" + username);
                response.sendRedirect("regist.html");
                return;
            }
            User u = new User();
            u.setUsername(username);
            u.setPasswd(passwd);
            u.setEmail(email);
            int n = dao.add(u);
            System.out.println("===/regist=== 插入受影响行数=" + n + "，现在共 " + dao.count() + " 个用户");
            request.getSession().setAttribute("loginUser", username);
            request.setAttribute("tip", "注册成功，已插入 " + n + " 行；users 表现有 " + dao.count() + " 行");
            request.getRequestDispatcher("/show.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException("注册失败：" + e, e);
        }
    }
}

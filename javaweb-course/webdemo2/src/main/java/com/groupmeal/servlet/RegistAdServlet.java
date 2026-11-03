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
 * 第 16 项（后半）：admin/regist.html 的表单打到 /admin/regist，管理员往数据库里添加用户。
 * 和第 15 项的区别只有两点：入口在 admin 目录下、加完不自动登录，而是回到管理页并把新名单显示出来。
 */
@WebServlet("/admin/regist")
public class RegistAdServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String username = request.getParameter("username");
        String passwd = request.getParameter("passwd");
        String email = request.getParameter("email");
        try {
            UsersDao dao = new UsersDao();
            String tip;
            if (dao.isExists(username)) {
                tip = "添加失败：用户名 " + username + " 已存在";
            } else {
                User u = new User();
                u.setUsername(username);
                u.setPasswd(passwd);
                u.setEmail(email);
                tip = "添加成功，插入 " + dao.add(u) + " 行；users 表现有 " + dao.count() + " 行";
            }
            System.out.println("===/admin/regist=== " + tip);
            request.setAttribute("tip", tip);
            request.setAttribute("users", dao.selAll());
            request.getRequestDispatcher("/admin/ok.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException("管理员添加用户失败：" + e, e);
        }
    }
}

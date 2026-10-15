<%--
  Created by IntelliJ IDEA.
  Date: 2026/10/14
  Time: 下午8:24
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%
    // 注意：本页所有逻辑都写在 HTML 之前。因为 sendRedirect / forward 必须在响应未提交时调用。
    request.setCharacterEncoding("UTF-8");

    String username = request.getParameter("username");
    String pwd = request.getParameter("pwd");
    String remember = request.getParameter("remember");
    if(username == null) username="";

    if("glenbear".equals(username) && "123456".equals(pwd)){
        // 1) session：登录状态放进会话域，后面每个页面都能取
        session.setAttribute("loginUser", username);
        session.setAttribute("loginTime", new java.util.Date());

        // 2) Cookie：值里可能有中文 → 必须 URLEncoder；勾选=7 天，未勾选=寿命设 0（=删除同名 Cookie）
        Cookie cookie = new Cookie("lastUser", java.net.URLEncoder.encode(username,"UTF-8"));
        cookie.setPath("/");
        cookie.setMaxAge(remember != null ? 7 * 24 * 60 * 60 : 0);
        response.addCookie(cookie);

        // 3) response：重定向（两次请求，地址栏会变成 success.jsp）
        response.sendRedirect("success.jsp");
        return; //重定向后必须结束本页，再输出内容会抛IllegalStateException
    }
        // 登录失败：请求域存提示 + 转发回登录页（一次请求，地址栏不变，request 数据还在）
        request.setAttribute("msg", "用户名或密码错误，请重试。当前sessionId：" + session.getId());
        request.getRequestDispatcher("login.jsp").forward(request, response);
%>
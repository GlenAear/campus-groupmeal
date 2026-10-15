<%--
  Created by IntelliJ IDEA.
  Date: 2026/10/14
  Time: 下午8:24
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
    // 越权保护：没登录直接敲这个 URL，用 response 赶回登录页
    if(session.getAttribute("loginUser") == null){
        response.sendRedirect("login.jsp");
        return;
    }
    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
%>

<html>
<head>
    <title>实验报告2 · 案例② 登录成功</title>
</head>
<body>
<h2>欢迎，<%= session.getAttribute("loginUser") %>！</h2>
<%
    out.println("session 登录时间：" + fmt.format((Date) session.getAttribute("loginTime"))+"<br/>");
    out.println("SessionId(URL重写后的值)：" + session.getId() + "<br/>");
    out.println("会话创建时间："+fmt.format(new Date(session.getCreationTime()))+"<br/>");
    out.println("会话最后访问时间："+fmt.format(new Date(session.getLastAccessedTime()))+"<br/>");
    out.println("是否为新会话 isNew()："+(session.isNew() ? "是" : "否")+"<br/>");
    out.println("会话超时时间 getMaxInactiveInterval():"+session.getMaxInactiveInterval() + "秒<br/>");
    out.println("application 中的登录总次数---"+application.getAttribute("loginCount")+"<br/>");
%>
<hr>
<a href="logout.jsp">退出登录（session.invalidate()）</a>　
<a href="login.jsp">回到登录页</a>
</body>
</html>

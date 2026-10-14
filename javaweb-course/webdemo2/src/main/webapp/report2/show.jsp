<%--
  Created by IntelliJ IDEA.
  Date: 2026/10/14
  Time: 下午8:24
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding ="UTF-8"%>
<% request.setCharacterEncoding("UTF-8"); %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>

<html>
<head>
    <title>实验报告2 · 案例① 注册结果</title>
</head>
<body>
<h2>注册信息回显（show.jsp）</h2>

<h3>一、request 获取表单参数（out 输出）</h3>

<%

    String username = request.getParameter("username");
    String pwd = request.getParameter("pwd");
    String sex = request.getParameter("sex");
    String city = request.getParameter("city");
    String intro = request.getParameter("intro");
    String[] hobbies = request.getParameterValues("hobby");

    out.println("用户名：" + (username == null ? "(未填)" : username) + "<br/>");
    out.println("密码：" + (pwd == null ? "(未填)" : pwd) + "（长度 " + (pwd == null ? 0 : pwd.length()) + "）<br/>");
    out.println("性别：" + sex + "<br/>");
    out.println("爱好：" + (hobbies == null ? "(一个都没勾)" : Arrays.toString(hobbies)) + "<br/>");
    out.println("城市：" + city + "<br/>");
    out.println("自我介绍：" + (intro == null ? "" : intro) + "<br/>");
%>

<hr>

<h3>二、request 的其他常用 API（都是"客户端发来的请求信息"）</h3>
<%
    out.println("请求方式 getMethod()：" + request.getMethod() + "<br/>");
    out.println("请求资源 getRequestURI()：" + request.getRequestURI() + "<br/>");
    out.println("项目上下文 getContextPath()：" + request.getContextPath() + "<br/>");
    out.println("客户端地址 getRemoteAddr()：" + request.getRemoteAddr() + "<br/>");
    out.println("参数编码 getCharacterEncoding()：" + request.getCharacterEncoding() + "<br/>");
    out.println("协议 getProtocol()：" + request.getProtocol() + "<br/>");
    out.print("浏览器 User-Agent：" + request.getHeader("User-Agent") + "<br/>");
%>

<hr>

<h3>三、内置对象的"真身"（用运行时类名印证 §2 的表）</h3>
<%
    out.println("out → " + out.getClass().getName() + "<br/>");
    out.println("session → " + session.getClass().getName() + "<br/>");
    out.println("application → " + application.getClass().getName() + "<br/>");
    out.println("pageContext → " + pageContext.getClass().getName() + "<br/>");
    out.println("服务器信息 application.getServerInfo()：" + application.getServerInfo() + "<br/>");
    out.println("本文件真实路径 application.getRealPath(\"/report2/show.jsp\")："
            + application.getRealPath("/report2/show.jsp") + "<br/>");
    out.println("服务器时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "<br/>");
%>
<hr>

<a href="register.jsp">返回重新注册</a>　<a href="/webdemo2/hello.jsp">报告1 的 hello.jsp</a>

</body>
</html>

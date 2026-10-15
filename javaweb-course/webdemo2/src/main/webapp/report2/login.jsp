<%--
  Created by IntelliJ IDEA.
  Date: 2026/10/14
  Time: 下午8:16
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%
    // 案例③：从 Cookie 里把上次登录的用户名读出来，预填到输入框
    String lastUser = "";
    Cookie [] cookies = request.getCookies(); //Cookie不用import , jakarta.servlet.http已自动导入
    if(cookies != null && cookies.length > 0){
        for(Cookie c : cookies){
            if("lastUser".equals(c.getName())){
                lastUser = java.net.URLDecoder.decode(c.getValue(),"UTF-8");
            }
        }
    }
%>

<html>
<head>
    <title>实验报告2 · 案例② 用户登录</title>
</head>
<body>
<h2>用户登录（login.jsp）</h2>
<p>测试账号：<b>glenbear</b>　密码：<b>123456</b></p>

<form action="check.jsp" method="post">
    <p>用户名：<input type="text" name="username" value="<%= lastUser%>"></p>
    <p>密&nbsp码&nbsp:<input type="password" name="pwd"></p>
    <p><label><input type="checkbox" name="remember" value="yes">记住我的用户名(写进Cookie，有效期7天)</label></p>
    <p><input type="submit" value="登录"></p>
</form>
<p style="color: red">${msg}</p>
<hr>
本页面SessionId：<%= session.getId()%>
</body>
</html>

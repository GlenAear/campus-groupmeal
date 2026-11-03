<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head><meta charset="UTF-8"><title>管理页</title></head>
<body>
<h1>管理员登录成功</h1>
<p>当前管理员：<b><%= session.getAttribute("adUser") %></b>（用户名和 session 里都验证过了）</p>
<p><a href="regist.html">添加用户</a>　<a href="../lg.html">普通用户登录</a></p>
</body>
</html>

<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.groupmeal.bean.User" %>
<%-- 第 8 项：JavaBean 存一个用户、取用户 —— Java 代码实现 --%>
<%
    User u = new User();
    u.setUid(8);
    u.setUsername("glen");
    u.setPasswd("bear");
    u.setEmail("1994341519@qq.com");
    u.setRegDate("2026-11-03");
    request.setAttribute("jbUser", u);
%>
<html>
<head><meta charset="UTF-8"><title>JavaBean·Java 写法（第8项）</title></head>
<body>
<h1>Java 代码存取 JavaBean</h1>
<p>存：<code>User u = new User(); u.setUsername("glen"); ...</code></p>
<p>取：用户名＝<b><%= u.getUsername() %></b>，邮箱＝<b><%= u.getEmail() %></b>，注册日期＝<b><%= u.getRegDate() %></b></p>
<p>toString()：<%= u %></p>
<p><a href="jbtag.jsp">看第 9 项：同一件事用 JSP 标签怎么写</a></p>
</body>
</html>

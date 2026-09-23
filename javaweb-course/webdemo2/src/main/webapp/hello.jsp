<%--
  Created by IntelliJ IDEA.
  User: 19943
  Date: 2026/9/19
  Time: 下午2:55
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<html>
<head>
    <title>实验报告1 · 环境验证</title>
</head>
<body>
<h2>Tomcat 10.1.5 + IntelliJ IDEA 2024.1.2 部署成功</h2>

<%-- 1. scriptlet 小脚本 --%>
<%
    String course = "网络编程技术";
    out.println("课程：" + course + "<br/>");
%>

<%-- 2. 表达式 --%>
服务器当前时间：<%= new java.util.Date() %><br/>

<%-- 3. 内置对象 application（报告2 会正式讲） --%>
JSP 容器信息：<%= application.getServerInfo() %><br/>
Servlet 规范版本：<%= application.getMajorVersion() %>.<%= application.getMinorVersion() %><br/>
项目真实路径：<%= application.getRealPath("/") %><br/>
</body>
</html>


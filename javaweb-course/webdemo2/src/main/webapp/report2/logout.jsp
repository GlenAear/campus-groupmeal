<%--
  Created by IntelliJ IDEA.
  Date: 2026/10/15
  Time: 上午11:24
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%
    session.invalidate();               // 会话域内容全清；下次访问会生成新 SessionId
    response.sendRedirect("login.jsp");
%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<html>
<head><meta charset="UTF-8"><title>添加结果</title></head>
<body>
<h1><%= request.getAttribute("tip") %></h1>
<h3>users 表名单（UsersDao.selAllNoPwd() → Conn.selResult()，<b>SQL 里没查 passwd，所以页面上不会出现密码</b>）</h3>
<table border="1" cellpadding="6">
<%
    List<Map<String, Object>> rows = (List<Map<String, Object>>) request.getAttribute("users");
    if (!rows.isEmpty()) {
        out.print("<tr>");
        for (String k : rows.get(0).keySet()) { out.print("<th>" + k + "</th>"); }
        out.print("</tr>");
    }
    for (Map<String, Object> r : rows) {
        out.print("<tr>");
        for (Object v : r.values()) { out.print("<td>" + v + "</td>"); }
        out.print("</tr>");
    }
%>
</table>
<p><a href="regist.html">再加一个</a>　<a href="lg.html">回管理员登录</a></p>
</body>
</html>

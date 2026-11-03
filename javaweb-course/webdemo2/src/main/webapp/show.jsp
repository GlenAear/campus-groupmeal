<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<html>
<head><meta charset="UTF-8"><title>所有商品页</title></head>
<body>
<h1>所有商品页</h1>
<p>欢迎，<b><%= session.getAttribute("loginUser") %></b></p>
<%
    Object tip = request.getAttribute("tip");
    if (tip != null) { out.println("<p style='color:green'>" + tip + "</p>"); }
%>
<hr/>
<ul>
    <li>红烧肉　18 元</li>
    <li>清蒸鱼　32 元</li>
    <li>番茄炒蛋　12 元</li>
</ul>
<%
    Object users = request.getAttribute("users");
    if (users != null) {
        out.println("<h3>users 表当前内容（selResult 查出来的）</h3><table border='1' cellpadding='6'>");
        List<Map<String, Object>> rows = (List<Map<String, Object>>) users;
        if (!rows.isEmpty()) {
            out.println("<tr>");
            for (String k : rows.get(0).keySet()) { out.println("<th>" + k + "</th>"); }
            out.println("</tr>");
        }
        for (Map<String, Object> r : rows) {
            out.println("<tr>");
            for (Object v : r.values()) { out.println("<td>" + v + "</td>"); }
            out.println("</tr>");
        }
        out.println("</table>");
    }
%>
<p><a href="lg.html">回登录页</a>　<a href="lt.jsp">数据库连通测试（第7项）</a></p>
</body>
</html>

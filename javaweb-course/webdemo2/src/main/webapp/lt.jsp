<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.groupmeal.util.Conn" %>
<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.Statement" %>
<%@ page import="java.sql.ResultSet" %>
<%-- 第 7 项：数据库连通测试。老师原话——连通本应写在 src 里给多个类用，这里为了测试才做成 jsp --%>
<html>
<head><meta charset="UTF-8"><title>数据库连通测试（第7项）</title></head>
<body>
<h1>数据库连通测试</h1>
<%
    try {
        Connection c = Conn.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("select count(*) cnt from users");
        rs.next();
        int cnt = rs.getInt("cnt");
        out.println("<h2 style='color:green'>连接成功：jdbc.properties 读到的库里，users 表有 " + cnt + " 行</h2>");
        rs.close(); s.close(); c.close();
    } catch (Exception e) {
        out.println("<h2 style='color:red'>连接失败：" + e.getClass().getName() + " — " + e.getMessage() + "</h2>");
    }
%>
<p>驱动类：<%= com.mysql.cj.jdbc.Driver.class.getName() %></p>
</body>
</html>

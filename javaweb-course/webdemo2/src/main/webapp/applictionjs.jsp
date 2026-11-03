<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 第 6 项：application 实现计数累计。application＝全应用共享、进程级、活到服务器关闭 --%>
<%
    Integer n = (Integer) application.getAttribute("visitCount");
    if (n == null) { n = 0; }
    n = n + 1;
    application.setAttribute("visitCount", n);
%>
<html>
<head><meta charset="UTF-8"><title>application 计数（第6项）</title></head>
<body>
<h1>本页面累计被访问 <%= n %> 次</h1>
<p>刷新一次加一。计数存在 <b>application</b> 里，所以<b>换一个浏览器、换一个人访问也照样累加</b>；
   换成 session 就只有自己看得到，换成 request 就每次都是 1。</p>
<p>（报告2 案例④ 用的就是同一个内置对象。）</p>
</body>
</html>

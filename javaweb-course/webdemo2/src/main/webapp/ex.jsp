<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 第 5 项：<%@ include %> 指令举例＝静态包含，编译时把 top.html 的内容原样拼进本 jsp，最终只有一个 servlet --%>
<html>
<head><meta charset="UTF-8"><title>include 指令（第5项）</title></head>
<body>
<%@ include file="top.html" %>
<h1>ex.jsp 正文</h1>
<p>上面那块蓝底是 <b>&lt;%@ include file="top.html" %&gt;</b> 静态包含进来的。</p>
<p><b>老师常问</b>：静态包含（指令）编译时合并成<b>一个</b> servlet，共享同一个 request；
   动态包含（<code>&lt;jsp:include page="..."/&gt;</code>）是运行时把目标页<b>输出的结果</b>塞进来，两个独立的 servlet。</p>
</body>
</html>

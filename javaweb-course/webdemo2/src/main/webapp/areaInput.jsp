<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 实验报告4 · 视图（V）之一：输入三角形与梯形的值 --%>
<html>
<head><meta charset="UTF-8"><title>输入图形参数（视图1）</title></head>
<body>
<h1>请输入三角形与梯形的边长</h1>
<p>表单提交到 <code>lookArea</code>（＝ <code>@WebServlet(name="computerArea", urlPatterns="/lookArea")</code> 的 HandleArea）。</p>
<form action="lookArea" method="post">
    <h3>三角形</h3>
    <p>底　　　<input name="triangleBase" value="4"></p>
    <p>高　　　<input name="triangleHeight" value="5"></p>
    <h3>梯形</h3>
    <p>上底　　　<input name="trapezoidTop" value="3"></p>
    <p>下底　　　<input name="trapezoidBottom" value="5"></p>
    <p>高　　　<input name="trapezoidHeight" value="4"></p>
    <p><input type="submit" value="计算面积"></p>
</form>
<p>提示：直接访问 <a href="lookArea">lookArea</a> 也行（GET 也能处理，用的是默认值）。</p>
</body>
</html>

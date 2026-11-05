<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 实验报告4 · 视图（V）之二：显示三角形和梯形的面积。
     值由控制器放进 request（request.setAttribute("area", bean)），这里用 EL 从 JavaBean 里取属性。
     ${area.triangleArea} 等价于 ((AreaBean)request.getAttribute("area")).getTriangleArea() --%>
<html>
<head><meta charset="UTF-8"><title>面积结果（视图2）</title></head>
<body>
<h1>面积计算结果</h1>

<h3>三角形</h3>
<p>底 = ${area.triangleBase}，高 = ${area.triangleHeight}</p>
<p>面积 = <b style="color:darkred">${area.triangleArea}</b>　（公式：底 × 高 ÷ 2）</p>

<h3>梯形</h3>
<p>上底 = ${area.trapezoidTop}，下底 = ${area.trapezoidBottom}，高 = ${area.trapezoidHeight}</p>
<p>面积 = <b style="color:darkred">${area.trapezoidArea}</b>　（公式：(上底 ＋ 下底) × 高 ÷ 2）</p>

<hr/>
<p>地址栏停在 <code>/lookArea</code> 而不是变成 <code>areaShow.jsp</code> —— 因为控制器用的是
   <b>forward 转发</b>（一次请求，request 里的数据还在，所以 EL 取得到）。</p>
<p><a href="areaInput.jsp">再算一组</a></p>
</body>
</html>

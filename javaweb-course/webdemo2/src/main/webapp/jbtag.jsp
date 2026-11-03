<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 第 9 项：JavaBean 存一个用户、取用户 —— JSP 标签实现（和上一项成对，老师最爱抽这两个） --%>
<jsp:useBean id="u2" class="com.groupmeal.bean.User" scope="page"/>
<jsp:setProperty name="u2" property="username" value="lisi"/>
<jsp:setProperty name="u2" property="passwd" value="123"/>
<jsp:setProperty name="u2" property="email" value="li@qq.com"/>
<html>
<head><meta charset="UTF-8"><title>JavaBean·标签写法（第9项）</title></head>
<body>
<h1>JSP 标签存取 JavaBean</h1>
<p>存：<code>&lt;jsp:useBean&gt;</code> ＋ <code>&lt;jsp:setProperty&gt;</code>　取：<code>&lt;jsp:getProperty&gt;</code></p>
<p>用户名＝<b><jsp:getProperty name="u2" property="username"/></b>，
   密码＝<b><jsp:getProperty name="u2" property="passwd"/></b>，
   邮箱＝<b><jsp:getProperty name="u2" property="email"/></b>，
   注册日期＝<b><jsp:getProperty name="u2" property="regDate"/></b>（没 set 过，所以是 null）</p>
<p>JavaBean 三要素：属性私有 ＋ 有 get/set ＋ 有无参构造。<b>少了无参构造，<code>&lt;jsp:useBean&gt;</code> 直接报错</b>。</p>
</body>
</html>

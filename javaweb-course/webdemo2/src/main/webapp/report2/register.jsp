<%--
  Created by IntelliJ IDEA.
  Date: 2026/10/14
  Time: 下午8:23
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding = "UTF-8"%>

<html>
<head>
    <title>实验报告2· 案例① 用户注册</title>
</head>
<body>
<h2>用户注册（register.jsp）</h2>
<form action="show.jsp" method="post">
    <p>用户名：<input type="text" name="username" value="glenbear"></p>
    <p>密码：<input type="password" name="pwd" value="123456"></p>
    <p>性别：
    <input type="radio" name="sex" value="男" checked>男
    <input type="radio" name="sex" value="女">女
    </p>
    <p>爱好：
    <input type="checkbox" name="hobby" value="篮球">篮球
    <input type="checkbox" name="hobby" value="足球">足球
    <input type="checkbox" name="hobby" value="滑板">滑板
    </p>
    <p>城市：
        <select name="city">
            <option value="西安">西安</option>
            <option value="其他">其他</option>
        </select>
    </p>
    <p>自我介绍：<textarea name="intro" rows="3" cols="30">网络编程技术实验</textarea></p>
    <p><input type="submit" value="提交注册"> <input type="reset" value="重置"></p>
</form>
</body>
</html>

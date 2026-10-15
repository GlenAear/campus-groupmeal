<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%!
    /**
     * HTML 转义：昵称和消息都要过一遍，防止 " < > & 打乱页面结构（XSS）。
     * 写在 JSP「声明」标签里（就是百分号加叹号那种）＝ 翻译成 Servlet 的成员方法，
     * 本页任何位置都能调用。
     * ⚠ 顺带一个 JSP 的坑：声明块内部不能出现「百分号紧跟右尖括号」这个结束符，
     *   连注释里也不行 —— 一旦写了，JSP 会认为声明在那里就提前结束了，后面全变成普通文本。
     * 原来只转义了消息里的 < >，昵称没转义 —— 昵称里带一个英文双引号就能把
     * value="..." 这个属性截断，属于同一个坑，这次一起补掉。
     */
    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
%>
<%
    request.setCharacterEncoding("UTF-8");

    String name = request.getParameter("name");
    String msg  = request.getParameter("msg");
    if (name == null || name.trim().isEmpty()) name = "游客";
    name = name.trim();

    // ① 有消息 → 写进 application 域，然后重定向（Post/Redirect/Get，防止刷新重复提交）
    if (msg != null && !msg.trim().isEmpty()) {
        synchronized (application) {                     // application 全局共享，必须加锁
            List<String> list = (List<String>) application.getAttribute("chatList");
            if (list == null) {
                list = new ArrayList<String>();
                application.setAttribute("chatList", list);
            }
            String time = new SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
            // 写入前就转义：存进 application 的已经是安全文本，读取端不必再处理
            list.add("[" + time + "] " + esc(name) + "：" + esc(msg.trim()));
            while (list.size() > 30) list.remove(0);     // 只保留最近 30 条
        }
        response.sendRedirect("chat.jsp?name=" + java.net.URLEncoder.encode(name, "UTF-8"));
        return;
    }
%>
<html>
<head>
    <title>实验报告2 · 案例④ Application 多人聊天室</title>
    <%-- ★BUG 修复（2026-10-15）：这里原来写的是 <meta http-equiv="refresh" content="2">。
         meta refresh 的语义是"每 2 秒把整个文档重新请求一次"，于是：
           · 昵称框刚打两个字就被整页重载清空 → 永远输不完用户名；
           · 焦点、光标位置、滚动条全部重置；
           · 浏览器每 2 秒重新加载一次页面，切到别的标签页时也能感觉到它在跳。
         现在改为：整页只加载一次，由下面的 JS 每 2 秒 fetch chatData.jsp，
         只替换 <div id="chatArea"> 里的内容 —— 消息照样 2 秒内自己冒出来，输入框完全不受影响。 --%>
</head>
<body>
<h2>多人在线聊天室（消息存 application 域，全项目共享）</h2>

<p>
    <label><input type="checkbox" id="autoRefresh" checked
                  onclick="this.checked ? startAuto() : stopAuto()">
        自动刷新（每 2 秒，只刷新消息区，不影响输入）</label>
    　<input type="button" value="立即刷新" onclick="loadMessages()">
    　<span id="tip" style="color:gray">上次刷新：—</span>
</p>

<div id="chatArea">
    <jsp:include page="chatData.jsp"/>
</div>

<form action="chat.jsp" method="post">
    <p>昵称：<input type="text" name="name" value="<%= esc(name) %>" size="12"></p>
    <p>消息：<input type="text" name="msg" size="55"></p>
    <p><input type="submit" value="发送"></p>
</form>
<hr>
我的 SessionId：<%= session.getId() %>
<span style="color:gray">（换浏览器这个值会变；但聊天记录在 application 域，所以两个浏览器看到的是同一份）</span>

<script>
    var timer = null;

    // 只换"消息区"这一块 DOM：表单、输入框、焦点、光标位置全部不动
    function loadMessages() {
        fetch('chatData.jsp?_=' + new Date().getTime(), {cache: 'no-store'})
            .then(function (r) { return r.text(); })
            .then(function (html) {
                // 换内容之前先记住：我原来是贴在底部的，还是正在往上翻历史？
                var old = document.getElementById('msgBox');
                var atBottom = !old || (old.scrollHeight - old.scrollTop - old.clientHeight < 12);
                var keepTop = old ? old.scrollTop : 0;

                document.getElementById('chatArea').innerHTML = html;

                var box = document.getElementById('msgBox');
                // 贴底 → 自动跟到最新一条；正在翻历史 → 保持原位，不被硬拽回去
                if (box) box.scrollTop = atBottom ? box.scrollHeight : keepTop;

                document.getElementById('tip').innerText =
                    '上次刷新：' + new Date().toLocaleTimeString();
            })
            .catch(function () { /* 偶发失败不管它，2 秒后还会再试 */ });
    }

    function startAuto() { if (timer === null) timer = setInterval(loadMessages, 2000); }
    function stopAuto()  { if (timer !== null) { clearInterval(timer); timer = null; } }

    window.addEventListener('load', function () {
        // ★坑：不能用 document.forms[0].name 去取"昵称"这个输入框 ——
        // form.name 是表单自己的 name 属性（HTML 的老坑），拿到的是字符串不是元素。
        // 取表单里的控件必须用 form.elements['name']。
        var f = document.forms[0];
        var nameInput = f.elements['name'];
        var msgInput  = f.elements['msg'];
        // 昵称还是默认的"游客"就先把光标放昵称上，否则直接放消息框，发完一句能接着打
        (nameInput.value === '游客' || nameInput.value === '' ? nameInput : msgInput).focus();

        loadMessages();   // 打开页面先把消息滚到底部
        startAuto();      // 再开启 2 秒轮询
    });
</script>
</body>
</html>

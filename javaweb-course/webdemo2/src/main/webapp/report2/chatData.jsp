<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%--
  chatData.jsp —— 只输出"消息区"这一小块 HTML 片段，不输出 <html>/<body>。
  它被两个地方调用：
    ① chat.jsp 首次打开时用 <jsp:include page="chatData.jsp"/> 动态包含
       （保证"首屏渲染"和"轮询渲染"用的是同一段代码，不会出现两处不一致）；
    ② chat.jsp 里的 JS 每 2 秒 fetch 一次本页，只替换消息区的 innerHTML
       —— 输入框不动、焦点不丢、光标不跳。
  这就是替代 <meta http-equiv="refresh" content="2"> 的做法：
  meta refresh 是把整个文档重新请求一遍，正在输入的昵称会被清掉。
--%>
<%
    // 轮询接口必须禁缓存，否则浏览器可能一直拿旧的消息列表（看起来像"收不到消息"）
    response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);

    // ★关键：读 application 里的共享 List 也要加锁，并且先拷一份快照再遍历。
    // 否则另一个请求正在 list.add() / list.remove(0) 时，这里的 for-each 会抛
    // ConcurrentModificationException（页面 500）。两个浏览器同时开着 2 秒轮询时特别容易撞上。
    List<String> snapshot;
    synchronized (application) {
        List<String> list = (List<String>) application.getAttribute("chatList");
        snapshot = (list == null) ? new ArrayList<String>() : new ArrayList<String>(list);
    }
%>
<div id="msgBox" style="border:1px solid #999;width:520px;height:200px;overflow:auto;padding:8px">
<%
    if (snapshot.isEmpty()) {
        out.println("<i>还没有人发言。</i>");
    } else {
        for (String line : snapshot) {
            out.println(line + "<br/>");   // line 在写入 application 时已做过 HTML 转义，这里直接输出
        }
    }
%>
</div>
<p>消息条数：<%= snapshot.size() %>
    　服务器：<%= application.getServerInfo() %>
    　应用真实路径：<%= application.getRealPath("/") %></p>

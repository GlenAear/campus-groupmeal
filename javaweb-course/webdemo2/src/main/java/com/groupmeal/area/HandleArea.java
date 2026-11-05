package com.groupmeal.area;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 实验报告4 · 控制器（C）：指导书要求的三点全在这里——
 *   ① Servlet 类名叫 HandleArea；
 *   ② Servlet 的名字（servlet-name）＝ computerArea；
 *   ③ 访问它的 url-pattern ＝ lookArea。
 * Servlet 3.0 用注解一行搞定：@WebServlet(name = "computerArea", urlPatterns = "/lookArea")
 * 等价的 Servlet 2.5 写法（web.xml）贴在报告正文里，见执行文档 §四。
 *
 * 指导书还要求"必须继承 HttpServlet、重写 doGet() 或 doPost()"。
 * 这里两个都重写、共同调用 service()：这样浏览器地址栏直接访问（GET）和表单提交（POST）都能演示。
 * 顺便把"Servlet 生命周期 5 阶段"里的 加载→初始化→服务→销毁 打日志打出来，控制台截图就是证据。
 */
@WebServlet(name = "computerArea", urlPatterns = "/lookArea", loadOnStartup = 1)
public class HandleArea extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** 初始化：容器创建实例后调用一次 */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        System.out.println("[生命周期] ②初始化 init()　servlet-name=" + config.getServletName()
                + "，url-pattern=/lookArea");
    }

    /** 真正处理请求（GET 和 POST 都走到这里） */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("[生命周期] ③服务 service() 被调用，收到请求：" + request.getRequestURI());
        request.setCharacterEncoding("UTF-8");

        // 1）接收：把表单里的五个数读出来，装进"封装数据的 JavaBean"
        AreaBean bean = new AreaBean();
        bean.setTriangleBase(parse(request, "triangleBase", 4));
        bean.setTriangleHeight(parse(request, "triangleHeight", 5));
        bean.setTrapezoidTop(parse(request, "trapezoidTop", 3));
        bean.setTrapezoidBottom(parse(request, "trapezoidBottom", 5));
        bean.setTrapezoidHeight(parse(request, "trapezoidHeight", 4));

        // 2）交给模型（业务逻辑 JavaBean）算面积 —— 控制器不算业务
        new AreaCalc().calc(bean);
        System.out.println("[模型算完] " + bean);

        // 3）把结果放进 request（M→V 的传递），再转发给显示页
        request.setAttribute("area", bean);
        request.getRequestDispatcher("/areaShow.jsp").forward(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 销毁：Servlet 被系统回收前执行 */
    @Override
    public void destroy() {
        System.out.println("[生命周期] ④销毁 destroy()　HandleArea 实例被回收");
    }

    private double parse(HttpServletRequest request, String name, double defaultValue) {
        String v = request.getParameter(name);
        if (v == null || v.trim().isEmpty()) return defaultValue;
        try {
            return Double.parseDouble(v.trim());
        } catch (NumberFormatException e) {
            return defaultValue;      // 输入不是数字就用默认值，报告里说明这是"容错"
        }
    }
}

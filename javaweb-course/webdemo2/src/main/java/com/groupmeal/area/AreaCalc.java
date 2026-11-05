package com.groupmeal.area;

/**
 * 实验报告4 · 模型（M）之二：封装业务逻辑的 JavaBean。
 * 指导书说 JavaBean 分两大类："封装数据的"（实体类，对应表）和"封装业务逻辑的"（它举的例子是 LoginDao 封装登录）。
 * 这里就把面积公式单独装进一个类，Servlet 只负责接收和跳转，公式改动只用动这一个文件 ——
 * 这就是指导书讲的 JavaBean 两个作用：减轻 JSP 复杂度 ＋ 提高代码复用。
 */
public class AreaCalc {

    /** 三角形面积 = 底 × 高 ÷ 2 */
    public double triangleArea(double base, double height) {
        return base * height / 2;
    }

    /** 梯形面积 = (上底 + 下底) × 高 ÷ 2 */
    public double trapezoidArea(double top, double bottom, double height) {
        return (top + bottom) * height / 2;
    }

    /** 把算好的结果写回那个封装数据的 JavaBean —— 两个 JavaBean 在这里接上 */
    public void calc(AreaBean bean) {
        bean.setTriangleArea(triangleArea(bean.getTriangleBase(), bean.getTriangleHeight()));
        bean.setTrapezoidArea(trapezoidArea(bean.getTrapezoidTop(), bean.getTrapezoidBottom(), bean.getTrapezoidHeight()));
    }
}

package com.groupmeal.area;

import java.io.Serializable;

/**
 * 实验报告4 · 模型（M）之一：封装数据的 JavaBean —— 负责存"输入的边长"和"算出来的面积"。
 * 指导书里 JavaBean 的定义（两点）在这里逐条对应：
 *   a. public 修饰的类 ＋ public 无参构造　→　class AreaBean 是 public，构造函数 public AreaBean() 显式写出来了
 *   b. 所有属性 private ＋ 提供 set/get　　→　下面七个属性全 private，每个都有 setXxx/getXxx
 * 再加 implements Serializable，才能放进 session（JSP 报告2 讲过）。
 * 注意 int 类型的面积字段我也用 double，因为三角形面积会出现 0.5 这样的小数。
 */
public class AreaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /* 输入：三角形的底和高 */
    private double triangleBase;
    private double triangleHeight;
    /* 输入：梯形的上底、下底、高 */
    private double trapezoidTop;
    private double trapezoidBottom;
    private double trapezoidHeight;

    /* 输出：两个面积（由 HandleArea 算好之后存进来） */
    private double triangleArea;
    private double trapezoidArea;

    public AreaBean() {                     // public 无参构造：缺了它 <jsp:useBean> 直接报错
    }

    public double getTriangleBase() { return triangleBase; }
    public void setTriangleBase(double triangleBase) { this.triangleBase = triangleBase; }

    public double getTriangleHeight() { return triangleHeight; }
    public void setTriangleHeight(double triangleHeight) { this.triangleHeight = triangleHeight; }

    public double getTrapezoidTop() { return trapezoidTop; }
    public void setTrapezoidTop(double trapezoidTop) { this.trapezoidTop = trapezoidTop; }

    public double getTrapezoidBottom() { return trapezoidBottom; }
    public void setTrapezoidBottom(double trapezoidBottom) { this.trapezoidBottom = trapezoidBottom; }

    public double getTrapezoidHeight() { return trapezoidHeight; }
    public void setTrapezoidHeight(double trapezoidHeight) { this.trapezoidHeight = trapezoidHeight; }

    public double getTriangleArea() { return triangleArea; }
    public void setTriangleArea(double triangleArea) { this.triangleArea = triangleArea; }

    public double getTrapezoidArea() { return trapezoidArea; }
    public void setTrapezoidArea(double trapezoidArea) { this.trapezoidArea = trapezoidArea; }

    @Override
    public String toString() {
        return "AreaBean[三角形 底=" + triangleBase + " 高=" + triangleHeight + " → 面积=" + triangleArea
                + "；梯形 上底=" + trapezoidTop + " 下底=" + trapezoidBottom + " 高=" + trapezoidHeight
                + " → 面积=" + trapezoidArea + "]";
    }
}

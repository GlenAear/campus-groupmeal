package com.groupmeal.jdbc3;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

/**
 * 实验报告3 · 步骤 d：处理结果集。
 * 演示指导书点到的三个方法：next()、previous()、getXxx(字段名或位置)。
 * previous() 要往回移光标，所以必须建"可滚动"的结果集（默认那种只能一直往前）。
 */
public class Demo4_ResultSet {

    public static void main(String[] args) throws Exception {
        Connection conn = DbParam.open();
        Statement st = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = st.executeQuery("select sid, sname, score, regdate from stu3");

        // ① 用元数据把列名列出来（换成任何一张表这段都能打印表头 —— 上机课 Conn.selResult 用的就是它）
        ResultSetMetaData md = rs.getMetaData();
        int cols = md.getColumnCount();
        System.out.print("表头：");
        for (int i = 1; i <= cols; i++) {
            System.out.print(md.getColumnLabel(i) + "(" + md.getColumnTypeName(i) + ") ");
        }
        System.out.println();

        // ② 正向遍历：next() 光标下移，返回 true 表示还有数据
        System.out.println("next() 正向：");
        while (rs.next()) {
            System.out.println("    按列名取 = " + rs.getInt("sid") + " / " + rs.getString("sname")
                    + "    按位置取 = " + rs.getInt(1) + " / " + rs.getString(2)
                    + "    日期 = " + rs.getDate("regdate")
                    + "    光标所在行 = " + rs.getRow());
        }
        System.out.println("遍历完再 next() = " + rs.next() + "（false，表示没数据了）");

        // ③ 往回走：previous()
        System.out.println("previous() 反向：");
        rs.beforeFirst();               // 光标放回第一行之前
        rs.next(); rs.next();           // 走到第 2 行
        while (rs.previous()) {
            System.out.println("    回退到：" + rs.getString("sname") + "，光标所在行 = " + rs.getRow());
        }

        rs.close(); st.close(); conn.close();
    }
}

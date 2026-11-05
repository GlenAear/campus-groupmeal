package com.groupmeal.jdbc3;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * 实验报告3 · 步骤 c（写法二）：PreparedStatement 参数化预编译，并用登录语句和 Statement 做对照。
 * 这是 JDBC 最该讲清楚的一段：拼字符串会被 SQL 注入，? 占位不会。
 */
public class Demo3_PreparedAndInjection {

    /** 模拟登录：查得到记录就算成功 */
    static boolean login(String sqlTemplate, String name, String pwd) throws Exception {
        Connection conn = DbParam.open();
        PreparedStatement ps = conn.prepareStatement(sqlTemplate);   // 先把 SQL 发给数据库预编译
        ps.setString(1, name);      // 给第 1 个 ? 填值（位置从 1 开始，不是 0）
        ps.setString(2, pwd);       // 给第 2 个 ? 填值
        ResultSet rs = ps.executeQuery();
        boolean ok = rs.next();     // 有结果行 ＝ 登录成功
        rs.close(); ps.close(); conn.close();
        return ok;
    }

    public static void main(String[] args) throws Exception {
        String name = "张三";
        String pwd  = "88";

        System.out.println("== 正常输入 ==");
        System.out.println("PreparedStatement 登录结果 = " + login("select * from stu3 where sname=? and score=?", name, pwd));

        System.out.println("== 被注入的输入：用户名填 张三' or '1'='1 ==");
        String evilName = "张三' or '1'='1";
        System.out.println("PreparedStatement 登录结果 = " + login("select * from stu3 where sname=? and score=?", evilName, "88"));

        // 对照组：不用 PreparedStatement，直接把参数拼进 SQL 字符串
        Connection conn = DbParam.open();
        Statement st = conn.createStatement();
        String sql = "select * from stu3 where sname='" + evilName + "' and score='88'";
        System.out.println("拼接后的 SQL 实际长这样：" + sql);
        ResultSet rs = st.executeQuery(sql);
        System.out.println("Statement 拼接 登录结果 = " + rs.next() + "　← 这就是被注掉了");
        rs.close(); st.close(); conn.close();

        System.out.println();
        System.out.println("结论：? 占位符把参数当【值】处理，永远不当【SQL 片段】处理，所以注入不进去。");
    }
}

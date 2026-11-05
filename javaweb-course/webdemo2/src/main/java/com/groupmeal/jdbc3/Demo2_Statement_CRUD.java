package com.groupmeal.jdbc3;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * 实验报告3 · 步骤 c（写法一）：Statement 发送 SQL，执行相关操作。
 * 增删改用 executeUpdate()（返回受影响行数），查询用 executeQuery()（返回 ResultSet）。
 *
 * 本类用的是 javaweb 库里的 stu3 表（建表脚本 sql/jdbc3.sql，第一步就是它）。
 */
public class Demo2_Statement_CRUD {

    public static void main(String[] args) throws Exception {
        Connection conn = DbParam.open();
        Statement st = conn.createStatement();

        // 先清一遍，保证可以反复运行
        st.executeUpdate("delete from stu3");

        // ① 增：executeUpdate 返回"影响了几行"
        int n1 = st.executeUpdate("insert into stu3(sname,score,regdate) values('张三',88,date('2026-11-05'))");
        st.executeUpdate("insert into stu3(sname,score,regdate) values('李四',55,date('2026-11-05'))");
        st.executeUpdate("insert into stu3(sname,score,regdate) values('王五',72,date('2026-11-05'))");
        System.out.println("插入第一条，受影响行数 = " + n1);

        // ② 查
        ResultSet rs = st.executeQuery("select sid,sname,score from stu3");
        System.out.print("查询全部：");
        while (rs.next()) {                       // next()：光标下移一行，有数据返回 true
            System.out.print("[" + rs.getInt("sid") + " " + rs.getString("sname")
                    + " " + rs.getInt("score") + "] ");
        }
        System.out.println();

        // ③ 改
        int n3 = st.executeUpdate("update stu3 set score = score + 10 where sname = '王五'");
        System.out.println("把王五加 10 分，受影响行数 = " + n3);

        // ④ 删
        int n4 = st.executeUpdate("delete from stu3 where score < 60");
        System.out.println("删掉不及格的，受影响行数 = " + n4);

        // ⑤ 聚合查询：只有一行一列，用 getString/getInt 取第一列
        rs = st.executeQuery("select count(*) , avg(score) from stu3");
        rs.next();
        System.out.println("现在共 " + rs.getInt(1) + " 行，平均分 " + rs.getDouble(2));   // getXxx(位置) 位置从 1 开始

        rs.close(); st.close(); conn.close();
    }
}

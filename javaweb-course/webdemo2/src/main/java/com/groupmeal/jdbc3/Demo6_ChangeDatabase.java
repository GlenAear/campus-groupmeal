package com.groupmeal.jdbc3;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * 实验报告3 · 考点"任意数据库"：同一段业务代码，只改驱动／URL／账号／密码四样，换库照样跑。
 * 运行方式：把 DbParam.useH2 改成 true 再跑一次本类（或者跑下面这个 main，它自己切换）。
 * 前半段连 MySQL，后半段连 H2 内存库 —— SQL 语句一模一样。
 */
public class Demo6_ChangeDatabase {

    /** 只接收四个参数，其余代码完全不关心是 MySQL 还是 H2 */
    static void run(String driver, String url, String user, String pwd) throws Exception {
        Class.forName(driver);                                    // ① 驱动＋具体驱动类
        Connection conn = DriverManager_getConnection(url, user, pwd);   // ② 连接字符串＋账号密码
        DatabaseMetaData md = conn.getMetaData();
        System.out.println("── 现在连的是：" + md.getDatabaseProductName() + " " + md.getDatabaseProductVersion());

        Statement st = conn.createStatement();
        // ③ 建表＋增：MySQL 和 H2 都能吃这几句标准 SQL
        try { st.executeUpdate("drop table stu9"); } catch (Exception ignore) {}
        st.executeUpdate("create table stu9(sid int primary key"
                + (DbParam.useH2 ? "" : " auto_increment") + ", sname varchar(30), score int)");
        st.executeUpdate("insert into stu9 values(1,'赵六',91)");
        st.executeUpdate("insert into stu9 values(2,'孙七',78)");

        // ④ 查：参数化预编译，两边用同一个写法
        PreparedStatement ps = conn.prepareStatement("select sid,sname,score from stu9 where score >= ?");
        ps.setInt(1, 80);
        ResultSet rs = ps.executeQuery();
        System.out.print("   80 分以上：");
        while (rs.next()) {
            System.out.print("[" + rs.getString("sname") + " " + rs.getInt("score") + "] ");
        }
        System.out.println();
        rs.close(); ps.close(); st.close(); conn.close();
    }

    static Connection DriverManager_getConnection(String url, String user, String pwd) throws Exception {
        return java.sql.DriverManager.getConnection(url, user, pwd);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("【第一次：MySQL】只需要改的四样＝驱动、具体驱动类、连接字符串、用户名密码");
        DbParam.useH2 = false;
        run(DbParam.MYSQL_DRIVER, DbParam.MYSQL_URL, DbParam.MYSQL_USER, DbParam.MYSQL_PWD);

        System.out.println();
        System.out.println("【第二次：H2】下面四样换成 H2 的，上面的 run() 方法一行没改");
        DbParam.useH2 = true;
        run(DbParam.H2_DRIVER, DbParam.H2_URL, DbParam.H2_USER, DbParam.H2_PWD);
    }
}

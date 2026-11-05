package com.groupmeal.jdbc3;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;

/**
 * 实验报告3 · 步骤 a＋b：导入驱动、加载具体驱动类、与数据库建立连接。
 * IDEA 里右键 Run 本类的 main() 即可，下面的输出就是报告里"实验结果截屏"的第一张。
 */
public class Demo1_DriverAndConn {

    public static void main(String[] args) throws Exception {

        // a. 导入驱动（jar 已在 pom 里），加载具体驱动类
        Class.forName(DbParam.driver());
        System.out.println("驱动类加载成功：" + DbParam.driver());

        // 看看 JVM 现在注册了哪些驱动（这一步不是必须，但截图很说明问题）
        Enumeration<Driver> ds = DriverManager.getDrivers();
        System.out.println("当前已注册的驱动：");
        while (ds.hasMoreElements()) {
            Driver d = ds.nextElement();
            System.out.println("    " + d.getClass().getName() + "  版本 " + d.getMajorVersion() + "." + d.getMinorVersion());
        }

        // b. 与数据库建立连接，并读出连接背后的信息
        try (Connection conn = DbParam.open()) {
            System.out.println("连接成功：" + conn);
            DatabaseMetaData md = conn.getMetaData();
            System.out.println("  数据库产品名     = " + md.getDatabaseProductName());
            System.out.println("  数据库版本       = " + md.getDatabaseProductVersion());
            System.out.println("  连接 URL         = " + md.getURL());
            System.out.println("  登录用户         = " + md.getUserName());
            System.out.println("  驱动名/版本      = " + md.getDriverName() + " / " + md.getDriverVersion());
            System.out.println("  是否自动提交     = " + conn.getAutoCommit());
        }
    }
}

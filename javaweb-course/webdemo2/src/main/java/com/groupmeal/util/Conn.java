package com.groupmeal.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 第 11 项：两种拿连接的方式
 *   getConn()      连接参数写死在代码里（老师进度表上写的是"直连 access 数据库"，
 *                  原理一样：驱动＋jdbc:URL＋账号密码。access 那套靠 JDK 自带的 JDBC-ODBC 桥，
 *                  而 **JDK 9 起桥就被移除了**，本机是 JDK 17，所以这里换成 MySQL 版，讲解时说明这一点即可。
 *                  ⚠️ 账号密码不写在这里（见方法上的注释），本仓库是公开仓库，口令一律走 jdbc.properties）
 *   getConnection()连接参数放 jdbc.properties —— 这就是 MyBatis 的 mybatis-config.xml 里
 *                  <environments>／jdbc.properties 干同一件事，也是"配置外置"的第一步
 * 第 12 项：三个公共方法
 *   selBoolean()   查询，返回记录是否存在（true/false）
 *   selResult()    查询，返回记录（每行一个 Map，键＝列名）
 *   updateData()   维护，增删改（返回受影响行数）
 *
 * 自带 main()，不用起 Tomcat 就能先验证连接与 SQL：IDEA 右键 Run 'Conn.main()' 即可。
 */
public class Conn {

    /** 第 11 项·写法一：参数直连（演示的形态＝URL 与驱动写在调用处，不经过属性文件对象）。
     *  ⚠️ 但**真实账号密码绝不落在源码里**：本仓库是公开仓库，所以 user/password 从 jdbc.properties
     *  取（该文件已进 .gitignore）。面试就讲这句："写死参数是教学形态，写死口令是事故。" */
    public static Connection getConn() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Properties p = loadProps();
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/javaweb?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai",
                p.getProperty("user"), p.getProperty("password"));
    }

    /** 第 11 项·写法二：属性文件存连接参数（四个值全从 jdbc.properties 读） */
    public static Connection getConnection() throws Exception {
        Properties p = loadProps();
        Class.forName(p.getProperty("driver"));
        return DriverManager.getConnection(p.getProperty("url"), p.getProperty("user"), p.getProperty("password"));
    }

    /** 两个写法共用的读文件动作：从 WEB-INF/classes 根目录读 jdbc.properties */
    private static Properties loadProps() throws Exception {
        Properties p = new Properties();
        InputStream in = Conn.class.getClassLoader().getResourceAsStream("jdbc.properties");
        if (in == null) {
            throw new SQLException("找不到 jdbc.properties：它应该在 WEB-INF/classes 根目录下"
                    + "（把 src/main/resources/jdbc.properties.example 复制成 jdbc.properties 再改值）");
        }
        p.load(new InputStreamReader(in, "UTF-8"));
        in.close();
        return p;
    }

    /** 第 12 项①：查询记录是否存在。sql 里用 ? 占位，args 按顺序填 */
    public static boolean selBoolean(String sql, Object... args) throws Exception {
        Connection conn = getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        bind(ps, args);
        ResultSet rs = ps.executeQuery();
        boolean exists = rs.next();
        close(rs, ps, conn);
        return exists;
    }

    /** 第 12 项②：查询并返回记录，每行一个 Map（键＝数据库列名，保序） */
    public static List<Map<String, Object>> selResult(String sql, Object... args) throws Exception {
        Connection conn = getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        bind(ps, args);
        ResultSet rs = ps.executeQuery();
        ResultSetMetaData md = rs.getMetaData();
        int cols = md.getColumnCount();
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<String, Object>();
            for (int i = 1; i <= cols; i++) {
                row.put(md.getColumnLabel(i), rs.getObject(i));
            }
            list.add(row);
        }
        close(rs, ps, conn);
        return list;
    }

    /** 第 12 项③：维护（insert／update／delete），返回受影响行数 */
    public static int updateData(String sql, Object... args) throws Exception {
        Connection conn = getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        bind(ps, args);
        int n = ps.executeUpdate();
        close(null, ps, conn);
        return n;
    }

    private static void bind(PreparedStatement ps, Object[] args) throws SQLException {
        if (args == null) return;
        for (int i = 0; i < args.length; i++) {
            ps.setObject(i + 1, args[i]);
        }
    }

    private static void close(ResultSet rs, Statement st, Connection conn) {
        try { if (rs != null) rs.close(); } catch (Exception e) {}
        try { if (st != null) st.close(); } catch (Exception e) {}
        try { if (conn != null) conn.close(); } catch (Exception e) {}
    }

    /** 冷启动自测：不连 Tomcat，直接把四个方法各跑一次 */
    public static void main(String[] args) throws Exception {
        System.out.println("写法一 getConn()      → " + getConn());
        System.out.println("写法二 getConnection()→ " + getConnection());
        System.out.println("selBoolean 有 zhangsan 吗 → "
                + selBoolean("select uid from users where username=? and passwd=?", "zhangsan", "123"));
        System.out.println("selBoolean 有 nobody 吗   → "
                + selBoolean("select uid from users where username=?", "nobody"));
        System.out.println("selResult 全部用户 → " + selResult("select uid,username,email from users"));
        System.out.println("updateData 插入一条 → " + updateData(
                "insert into users(username,passwd,email,regDate) values(?,?,?,?)",
                "testConn", "123", "test@qq.com", new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date())));
        System.out.println("updateData 删掉它   → " + updateData("delete from users where username=?", "testConn"));
    }
}

package com.groupmeal.jdbc3;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 实验报告3 第 1 步：把"换数据库只需要改的那四样东西"单独抽出来放这儿。
 * 老师指导书原话："如果对数据库进行了更换，只需要替换：驱动、具体驱动类、连接字符串、用户名、密码。"
 * 所以后面六个演示类都不写死参数，一律从这里取 —— 换库时只动这个文件，其余代码一行不改。
 *
 * ⚠️ 但**四样里面只有"密码"不能写在这个文件里**：本仓库是 GitHub 公开仓库，明文口令一旦提交，
 *    就算之后删掉也还留在历史里，只能靠"改密码"来作废。所以密码走外置（顺序从上往下试）：
 *      ① 启动参数 -Dmysql.pwd=xxx
 *      ② 环境变量 MYSQL_PWD
 *      ③ classpath 下的 jdbc.properties 里的 password=（这份已写进 .gitignore，不进仓库）
 *    这恰好就是 MyBatis 那条链的做法：驱动/URL/账号写在配置里，真密码单独放一个不进版本库的文件。
 */
public class DbParam {

    /* ===== MySQL（本机 8.4.6，库 javaweb）===== */
    public static final String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";
    public static final String MYSQL_URL    = "jdbc:mysql://localhost:3306/javaweb"
            + "?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai";
    public static final String MYSQL_USER   = "root";
    /** 不写死：运行时从 -Dmysql.pwd ／ 环境变量 MYSQL_PWD ／ jdbc.properties 的 password= 里取 */
    public static final String MYSQL_PWD    = readExternalPassword();

    /* ===== H2（纯 Java，内存库，不用装服务）===== */
    public static final String H2_DRIVER = "org.h2.Driver";
    public static final String H2_URL    = "jdbc:h2:mem:demo3;DB_CLOSE_DELAY=-1";
    public static final String H2_USER   = "sa";
    public static final String H2_PWD    = "";

    /** 当前用哪个库：改这一行就等于"换了个数据库" */
    public static boolean useH2 = false;

    public static String driver() { return useH2 ? H2_DRIVER : MYSQL_DRIVER; }
    public static String url()    { return useH2 ? H2_URL    : MYSQL_URL; }
    public static String user()   { return useH2 ? H2_USER   : MYSQL_USER; }
    public static String pwd()    { return useH2 ? H2_PWD    : MYSQL_PWD; }

    /** 四步走的第 ① 步：加载驱动类；② 建立连接。后面所有演示类都调这个方法 */
    public static Connection open() throws Exception {
        Class.forName(driver());          // ① 加载具体驱动类（反射，写错类名会抛 ClassNotFoundException）
        Connection conn = DriverManager.getConnection(url(), user(), pwd());   // ② 与数据库建立连接
        return conn;
    }

    /** 按 ①启动参数 ②环境变量 ③jdbc.properties 的顺序找密码；全都没有就报一句能看懂的话 */
    private static String readExternalPassword() {
        String p = System.getProperty("mysql.pwd");
        if (p == null || p.isEmpty()) {
            p = System.getenv("MYSQL_PWD");
        }
        if (p != null && !p.isEmpty()) {
            return p;
        }
        // classloader 找 jdbc.properties：跑 main 时在 target\classes 根目录，打包进 war 后在 WEB-INF\classes
        try (java.io.InputStream in = DbParam.class.getClassLoader().getResourceAsStream("jdbc.properties")) {
            if (in == null) {
                throw new IllegalStateException(
                        "取不到数据库密码：classpath 根目录没有 jdbc.properties，也没设 -Dmysql.pwd 或环境变量 MYSQL_PWD。"
                      + "把 src/main/resources/jdbc.properties.example 复制成同目录的 jdbc.properties 并填上 password= 即可"
                      + "（这份文件已在 .gitignore 里，不会被提交）。");
            }
            java.util.Properties pro = new java.util.Properties();
            pro.load(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8));
            String v = pro.getProperty("password");
            if (v == null || v.trim().isEmpty()) {
                throw new IllegalStateException("jdbc.properties 找到了，但里面 password= 是空的");
            }
            return v.trim();
        } catch (java.io.IOException e) {
            throw new IllegalStateException("读 jdbc.properties 失败：" + e.getMessage(), e);
        }
    }
}

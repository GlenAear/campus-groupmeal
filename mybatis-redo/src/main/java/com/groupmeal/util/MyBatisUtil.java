package com.groupmeal.util;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

public class MyBatisUtil {

    private static SqlSessionFactory factory;

    // 类加载时执行一次：解析 xml 配置与映射文件，构建工厂
    static {
        try (InputStream in = Resources.getResourceAsStream("mybatis-config.xml")) {
            factory = new SqlSessionFactoryBuilder().build(in);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ExceptionInInitializerError("mybatis-config.xml 读取失败：" + e.getMessage());
        }
    }

    /** 读操作用：默认不自动提交（等价于 openSession(false)） */
    public static SqlSession getSession() {
        return factory.openSession();
    }

    /** 写操作（增删改）用：true = 自动提交事务 */
    public static SqlSession getSession(boolean autoCommit) {
        return factory.openSession(autoCommit);
    }

    public static void close(SqlSession session) {
        if (session != null) session.close();
    }
}

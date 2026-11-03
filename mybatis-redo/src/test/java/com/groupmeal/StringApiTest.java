package com.groupmeal;

import com.groupmeal.entity.Emp;
import com.groupmeal.util.MyBatisUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

class StringApiTest {
    private SqlSession session;

    @BeforeEach
    void setUp(){
        session = MyBatisUtil.getSession(true);
    }
    @AfterEach
    void tearDown(){
        MyBatisUtil.close(session);
    }
    @Test
    void 字符串方式查全部(){
        List<Emp> list = session.selectList("com.groupmeal.dao.EmpDao.queryAll");
        list.forEach(System.out::println);
    }
    @Test
    void 字符串方式故意写错一次() {
        // 把 queryAll 写成 queryall —— 编译器不报错，运行时才炸。
        // 这里用 assertThrows 把"预期中的异常"固化成断言：教学点保留（亲眼看到异常内容），
        // 但 mvn test 不会因为这行永远红着（红测试的意义是"发现问题"，不是"演示问题"）。
        Exception e = Assertions.assertThrows(Exception.class,
                () -> session.selectList("com.groupmeal.dao.EmpDao.queryall"));
        System.out.println("故意写错看到的异常 = " + e.getClass().getSimpleName() + " : " + e.getMessage());
        Assertions.assertTrue(e.getMessage().contains("queryall"),
                "异常信息里应该带上那个写错的 id，方便定位");
    }
    @Test
    void 调一个接口里没有的SQL(){
        List<Emp> list = session.selectList("com.groupmeal.dao.EmpDao.querySalaryGt", 5000);
        list.forEach(System.out::println);
    }
}

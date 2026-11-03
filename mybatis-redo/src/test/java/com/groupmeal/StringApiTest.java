package com.groupmeal;

import com.groupmeal.entity.Emp;
import com.groupmeal.util.MyBatisUtil;
import org.junit.jupiter.api.AfterEach;
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
        // 把 queryAll 写成 queryall —— 编译器不报错，运行时炸
        List<Emp> list = session.selectList("com.groupmeal.dao.EmpDao.queryall");
        System.out.println(list);
    }
    @Test
    void 调一个接口里没有的SQL(){
        List<Emp> list = session.selectList("com.groupmeal.dao.EmpDao.querySalaryGt", 5000);
        list.forEach(System.out::println);
    }
}

package com.groupmeal;

import com.groupmeal.dao.EmpDao;
import com.groupmeal.util.MyBatisUtil;
import com.groupmeal.vo.EmpVo;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ResultMapTest {
    private SqlSession session;
    private EmpDao dao;

    @BeforeEach
    void setUp(){
        session = MyBatisUtil.getSession();
        dao = session.getMapper(EmpDao.class);
    }

    @AfterEach
    void tearDown(){ MyBatisUtil.close(session); }

    @Test
    void 不一致字段也能映射(){
        List<EmpVo> list = dao.queryAllVo();
        list.forEach(System.out::println);
        EmpVo first = list.get(0);
        Assertions.assertNotNull(first.getEmpName(),"ename应该映射到empName");
        Assertions.assertNotNull(first.getMoney(),"salary应该映射到money");
        Assertions.assertNotNull(first.getHDate(),"hiredate应该映射到hDate");

    }
}

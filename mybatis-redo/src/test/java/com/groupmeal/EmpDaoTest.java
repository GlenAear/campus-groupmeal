package com.groupmeal;

import com.groupmeal.dao.EmpDao;
import com.groupmeal.entity.Emp;
import com.groupmeal.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.*;

import java.util.Date;
import java.util.List;

class EmpDaoTest {

    private SqlSession session;
    private EmpDao dao;

    @BeforeEach                       // 每个 @Test 之前都执行一次
    void setUp() {
        session = MyBatisUtil.getSession(true);   // ⚠️ 写操作必须 true
        dao = session.getMapper(EmpDao.class);    // ⚠️ 用代理接口，不要直接调 session.selectList
    }

    @AfterEach                        // 每个 @Test 之后都执行一次
    void tearDown() {
        MyBatisUtil.close(session);
    }

    @Test
    void 查全部() {
        List<Emp> list = dao.queryAll();
        list.forEach(System.out::println);
        Assertions.assertFalse(list.isEmpty(), "emp 表不该是空的");
    }

    @Test
    void 按主键查() {
        Emp emp = dao.queryByEmpno(1);
        System.out.println(emp);
        Assertions.assertNotNull(emp);
        Assertions.assertEquals("张三", emp.getEname());
    }

    @Test
    void 增() {
        Emp emp = new Emp();
        emp.setEname("测试员工");
        emp.setSalary(6000f);
        emp.setDeptno(2);
        emp.setHiredate(new Date());
        int rows = dao.saveEmp(emp);
        System.out.println("插入行数=" + rows + "，回填的自增主键 empno=" + emp.getEmpno());
        Assertions.assertEquals(1, rows);
        Assertions.assertNotNull(emp.getEmpno(), "useGeneratedKeys 应该把主键回填进来");
        // 收尾：自己插的自己删掉。不写这一行，每跑一次 emp 表就多一行"测试员工"（本机实测已经攒了 3 行）
        Assertions.assertEquals(1, dao.deleteByEmpno(emp.getEmpno()), "收尾删除失败");
    }

    @Test
    void 改() {
        Emp emp = dao.queryByEmpno(1);
        Float origin = emp.getSalary();                 // 先记住原值
        emp.setSalary(origin + 500f);
        int rows = dao.updateEmp(emp);
        Assertions.assertEquals(1, rows);
        System.out.println("改后：" + dao.queryByEmpno(1));
        // 收尾：把工资还原。不写这一行，每跑一次 +500，种子数据会被测试吃掉
        //（本机实测：张三从 8000 被跑到了 9500，正好是跑了 3 次的结果）
        emp.setSalary(origin);
        Assertions.assertEquals(1, dao.updateEmp(emp), "还原失败说明 updateEmp 有问题");
        System.out.println("还原后：" + dao.queryByEmpno(1));
    }

    @Test
    void 删() {
        // 先插一条再删，避免把测试数据删光
        Emp emp = new Emp();
        emp.setEname("待删除");
        emp.setSalary(1000f);
        emp.setDeptno(1);
        emp.setHiredate(new Date());
        dao.saveEmp(emp);
        int rows = dao.deleteByEmpno(emp.getEmpno());
        Assertions.assertEquals(1, rows);
        Assertions.assertNull(dao.queryByEmpno(emp.getEmpno()), "删完应该查不到了");
    }
}

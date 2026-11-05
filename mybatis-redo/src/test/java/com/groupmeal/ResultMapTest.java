package com.groupmeal;

import com.groupmeal.dao.EmpDao;
import com.groupmeal.entity.Emp;
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
    void 不一致字段也能映射ForResultMap(){
        //result map 测试
        List<EmpVo> list = dao.queryAllVo();
        list.forEach(System.out::println);
        EmpVo first = list.get(0);
        Assertions.assertNotNull(first.getEmpName(),"ename应该映射到empName");
        Assertions.assertNotNull(first.getMoney(),"salary应该映射到money");
        Assertions.assertNotNull(first.getHDate(),"hiredate应该映射到hDate");

    }

    @Test
    void 不一致字段也能映射ForResultType(){
        //result map 测试
        List<EmpVo> list = dao.queryAllVo2();
        list.forEach(System.out::println);
        EmpVo first = list.get(0);
        Assertions.assertNotNull(first.getEmpName(),"ename应该映射到empName");
        Assertions.assertNotNull(first.getMoney(),"salary应该映射到money");
        Assertions.assertNotNull(first.getHDate(),"hiredate应该映射到hDate");

    }


    /**
     * 实验 A（02 册 §5.2）——这条**不断言，只打印**，因为它看到什么取决于 mybatis-config.xml 现在的 settings：
     * <ul>
     *   <li>常态配置（只有 mapUnderscoreToCamelCase，autoMappingBehavior 默认 PARTIAL）
     *       → 打印一条完整 Emp，属性都有值。queryByEmpno 用 resultType="Emp"、列名和属性名一模一样，PARTIAL 能自动映射上。</li>
     *   <li>临时加 <code>&lt;setting name="autoMappingBehavior" value="NONE"/&gt;</code>
     *       → 打印 <b>null</b>。因为 resultType 没有显式列映射，NONE 把自动映射整个关掉 → foundValues=false，
     *       而 returnInstanceForEmptyRow 默认 false → 收尾时对象直接给成 null。</li>
     *   <li>在上面那条基础上再加 <code>&lt;setting name="returnInstanceForEmptyRow" value="true"/&gt;</code>
     *       → 对象回来了但属性全 null（文档 §5.2 说的"属性全 null"其实是这一种，光开 NONE 是整行 null）。</li>
     * </ul>
     * ⚠️ 实验看完**必须把这两行从 settings 删掉**，否则连带其它 resultType 查询一起变成 null（§5.4）。
     * 顺带注意：上面 "Preparing/Parameters/Row: Total: 1" 那几行是 STDOUT_LOGGING 的 SQL 日志，
     * 属于执行器/JDBC 层，跟"结果怎么映射成对象"是两个独立阶段 —— null 上面有一堆 SQL 输出是正常的。
     */
    @Test
    void 实验A_打开NONE后resultType就映射不出来() {
        Emp emp = dao.queryByEmpno(1);
        System.out.println("实验A结果：" + emp
                + "　← 现在 settings 是常态（PARTIAL），所以应有值；想看到 null 请按方法注释临时加 autoMappingBehavior=NONE");
        Assertions.assertNotNull(emp, "PARTIAL 下 queryByEmpno 必然映射得出，若这里就是 null，说明 settings 里的实验行没删干净");
    }

    @Test
    void 下划线加开关也能映射对() {
        List<EmpVo> list = dao.queryAllVo3();
        list.forEach(System.out::println);
        EmpVo first = list.get(0);
        Assertions.assertNotNull(first.getEmpName(), "emp_name 应被开关还原成 empName");
        Assertions.assertNotNull(first.getMoney(), "money 是直接别名（§4 的招），开关管不到它");
        Assertions.assertNotNull(first.getHDate(), "h_date 应被开关还原成 hDate");
        Assertions.assertNotNull(first.getDeptNo(), "dept_no 应被开关还原成 deptNo");
    }
}

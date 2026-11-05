package com.groupmeal.dao;

import com.groupmeal.entity.Emp;
import com.groupmeal.vo.EmpVo;
import java.util.List;

public interface EmpDao {
    List<EmpVo> queryAllVo();
    List<EmpVo> queryAllVo2();
    List<EmpVo> queryAllVo3();
    int   getCount();                       // PDF p4 的第一个示例方法
    List<Emp> queryAll();                   // 查全部
    Emp   queryByEmpno(Integer empno);      // 按主键查
    int   saveEmp(Emp emp);                 // 增
    int   updateEmp(Emp emp);               // 改
    int   deleteByEmpno(Integer empno);     // 删
}

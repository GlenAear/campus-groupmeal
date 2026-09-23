package com.groupmeal.dao;

import com.groupmeal.entity.Emp;
import java.util.List;

public interface EmpDao {
    int   getCount();                       // PDF p4 的第一个示例方法
    List<Emp> queryAll();                   // 查全部
    Emp   queryByEmpno(Integer empno);      // 按主键查
    int   saveEmp(Emp emp);                 // 增
    int   updateEmp(Emp emp);               // 改
    int   deleteByEmpno(Integer empno);     // 删
}

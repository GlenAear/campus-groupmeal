package com.groupmeal.vo;

import lombok.Data;
import java.util.Date;


/**
 * 故意把属性名取得和表字段完全不一样，用来练 resultMap。
 * 真实项目里这就是"给前端看的 VO"：字段名按前端习惯取，不跟数据库妥协。
 */
@Data
public class EmpVo {
    private Integer empNo;  //empno
    private String empName; //ename
    private Float money;    //sal
    private Date hDate;     //hiredate
    private Integer deptNo; //deptno
}

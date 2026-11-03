package com.groupmeal.vo;

import lombok.Data;
import java.util.Date;


/**
 * 故意把属性名取得和表字段完全不一样，用来练 resultMap。
 * 真实项目里这就是"给前端看的 VO"：字段名按前端习惯取，不跟数据库妥协。
 *
 * 属性 ← 对应 emp 表的列（箭头右边才是数据库里的真名）：
 *   empNo   ← empno      （只差大小写，自动映射能认出来）
 *   empName ← ename      （列名少了个 p，自动映射认不出，必须写 resultMap 或用 AS 别名）
 *   money   ← salary     （完全不同的词，同上）
 *   hDate   ← hiredate   （同上）
 *   deptNo  ← deptno     （只差大小写，能自动映射）
 *
 * 注意本类用的是 Lombok 的 @Data：getter/setter/toString/equals/hashCode 都在编译期生成，
 * 源码里看不见但 .class 里有。MyBatis 填值靠的就是这些 setter。详见
 * markdown/MyBatis从头重现_附录A_Lombok与面试要点.md
 */
@Data
public class EmpVo {
    private Integer empNo;      // 对应列 empno
    private String  empName;    // 对应列 ename
    private Float   money;      // 对应列 salary
    private Date    hDate;      // 对应列 hiredate
    private Integer deptNo;     // 对应列 deptno
}

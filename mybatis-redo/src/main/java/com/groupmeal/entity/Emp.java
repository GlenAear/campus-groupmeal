package com.groupmeal.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data                 // Lombok：自动生成 getter/setter/toString/equals/hashCode（纪要「引入 Lombok 注解以省略 Getter 和 Setter」）
@NoArgsConstructor    // 无参构造：MyBatis 反射创建对象时需要
@AllArgsConstructor   // 全参构造：测试里 new Emp(...) 方便
public class Emp {
    private Integer empno;     // ⚠️ 必须包装类，不能写 int
    private String  ename;
    private Float   salary;    // ⚠️ 必须包装类，不能写 float
    private Integer deptno;    // 第 04 册做关联映射时，这里会换成 Dept 对象
    private Date    hiredate;

}

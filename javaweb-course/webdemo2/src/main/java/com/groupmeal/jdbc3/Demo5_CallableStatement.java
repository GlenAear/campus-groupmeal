package com.groupmeal.jdbc3;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Types;

/**
 * 实验报告3 · 指导书里点名的第三个 API：CallableStatement —— 调用数据库中的存储过程/存储函数。
 * 过程和函数先在 sql/jdbc3.sql 里建好（MySQL 8 支持）。
 *   存储过程：call p_count_stu(输入参数, 输出参数)
 *   存储函数：? = call f_avg_score()
 * 共同点：都用 registerOutParameter(位置, 类型) 告诉 JDBC 结果是什么类型。
 */
public class Demo5_CallableStatement {

    public static void main(String[] args) throws Exception {
        Connection conn = DbParam.open();

        // ① 调存储过程：传入"及格线 60"，OUT 参数拿回人数
        CallableStatement cs = conn.prepareCall("{call p_count_stu(?, ?)}");
        cs.setInt(1, 60);                                   // IN  参数
        cs.registerOutParameter(2, Types.INTEGER);          // OUT 参数：先声明类型
        cs.execute();
        System.out.println("存储过程 p_count_stu(60) → 60 分以上的有 " + cs.getInt(2) + " 人");
        cs.close();

        // ② 调存储函数：返回值写在最前面的 ? 上
        CallableStatement cs2 = conn.prepareCall("{? = call f_avg_score()}");
        cs2.registerOutParameter(1, Types.DOUBLE);
        cs2.execute();
        System.out.println("存储函数 f_avg_score() → 平均分 = " + cs2.getDouble(1));
        cs2.close();

        conn.close();
    }
}

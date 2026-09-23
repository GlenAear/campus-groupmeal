package com.groupmeal;

import com.groupmeal.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;

public class TestMyBatis {
    public static void main(String[] args) {
        SqlSession session = MyBatisUtil.getSession();
        try {
            // 方式一：直接调 SqlSession 的 API（PDF 的写法，能跑但不推荐）
            Integer count = session.selectOne("com.groupmeal.dao.EmpDao.getCount");
            System.out.println("emp 表记录数 = " + count);
        } finally {
            MyBatisUtil.close(session);
        }
    }
}

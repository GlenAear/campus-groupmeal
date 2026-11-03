package com.groupmeal.dao;

import com.groupmeal.bean.User;
import com.groupmeal.util.Conn;

import java.util.List;
import java.util.Map;

/**
 * 第 13 项：UsersDao 类访问 users 表。
 * 所有 SQL 都走 Conn 的三个公共方法，本类只负责"业务语义＋SQL 语句"，
 * 这就是三层架构里 DAO 层的位置——以后换成 MyBatis，被代理掉的就是这一层。
 */
public class UsersDao {

    /** 登录验证：用户名密码都对才算存在 */
    public boolean isUser(String username, String passwd) throws Exception {
        return Conn.selBoolean("select uid from users where username=? and passwd=?", username, passwd);
    }

    /** 管理员验证：既要在表里，用户名还必须是 admin（不额外建表，讲解时说明这是简化） */
    public boolean isAdmin(String username, String passwd) throws Exception {
        return "admin".equals(username) && isUser(username, passwd);
    }

    /** 注册前查重 */
    public boolean isExists(String username) throws Exception {
        return Conn.selBoolean("select uid from users where username=?", username);
    }

    /** 注册／管理员添加用户，返回受影响行数 */
    public int add(User u) throws Exception {
        return Conn.updateData("insert into users(username,passwd,email,regDate) values(?,?,?,?)",
                u.getUsername(), u.getPasswd(), u.getEmail(),
                new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
    }

    /** 查全部（第 19 项的主菜，今天先用它做验收） */
    public List<Map<String, Object>> selAll() throws Exception {
        return Conn.selResult("select uid,username,passwd,email,regDate from users order by uid");
    }

    /** 数一数有几行 */
    public int count() throws Exception {
        List<Map<String, Object>> rows = Conn.selResult("select count(*) cnt from users");
        return Integer.parseInt(rows.get(0).get("cnt").toString());
    }

    /** 按用户名查单个记录（第 18 项 selUser 的原型） */
    public User selUser(String username) throws Exception {
        List<Map<String, Object>> rows = Conn.selResult(
                "select uid,username,passwd,email,regDate from users where username=?", username);
        if (rows.isEmpty()) return null;
        Map<String, Object> r = rows.get(0);
        User u = new User();
        u.setUid(Integer.parseInt(r.get("uid").toString()));
        u.setUsername(String.valueOf(r.get("username")));
        u.setPasswd(String.valueOf(r.get("passwd")));
        u.setEmail(String.valueOf(r.get("email")));
        u.setRegDate(String.valueOf(r.get("regDate")));
        return u;
    }
}

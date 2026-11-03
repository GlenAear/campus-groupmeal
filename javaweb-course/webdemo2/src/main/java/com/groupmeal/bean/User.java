package com.groupmeal.bean;

import java.io.Serializable;

/**
 * JavaBean 三要素：①属性私有 ②有 get/set ③有无参构造（写 get/set 后编译器就不给默认的，所以显式写出来）
 * 还要 implements Serializable 才能放进 session（报告2 讲过）。
 * regDate 这里用 String 存，省掉 java.util.Date 与数据库 DATE 的转换，JSP 里也好直接打印。
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private int uid;
    private String username;
    private String passwd;
    private String email;
    private String regDate;

    public User() {
    }

    public int getUid() { return uid; }
    public void setUid(int uid) { this.uid = uid; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswd() { return passwd; }
    public void setPasswd(String passwd) { this.passwd = passwd; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRegDate() { return regDate; }
    public void setRegDate(String regDate) { this.regDate = regDate; }

    @Override
    public String toString() {
        return "User[uid=" + uid + ", username=" + username + ", email=" + email + ", regDate=" + regDate + "]";
    }
}

-- 11/4 上机课：第 13 项要用的 users 表（放在 javaweb 库里，列名与已有的 javaweb.usertb1 一致）
-- 机房机器上没有这张表就整段跑一次；本机 2026-11-03 已跑过，重复执行不会出错
CREATE DATABASE IF NOT EXISTS javaweb DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE javaweb;
CREATE TABLE IF NOT EXISTS users (
  uid INT NOT NULL AUTO_INCREMENT,
  username VARCHAR(30) DEFAULT NULL,
  passwd VARCHAR(30) DEFAULT NULL,
  email VARCHAR(50) DEFAULT NULL,
  regDate DATE DEFAULT NULL,
  PRIMARY KEY (uid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 三个测试账号（已存在就不重复插）
INSERT INTO users(username, passwd, email, regDate)
SELECT 'admin','123','admin@qq.com',CURDATE() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username='admin');
INSERT INTO users(username, passwd, email, regDate)
SELECT 'zhangsan','123','zhang@qq.com',CURDATE() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username='zhangsan');
INSERT INTO users(username, passwd, email, regDate)
SELECT 'lisi','123','li@qq.com',CURDATE() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username='lisi');

SELECT uid, username, passwd, email, regDate FROM users ORDER BY uid;

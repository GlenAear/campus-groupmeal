-- 实验报告3（JDBC）用的表 ＋ 存储过程/函数
-- 跑法（命令行；-p 后面**别接密码**，回车后会提示你输入，这样口令不落任何文件）：
--   "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" -uroot -p --default-character-set=utf8mb4 -e "source D:/Java/Projects/campus-groupmeal/javaweb-course/webdemo2/sql/jdbc3.sql"
--   （用 SQLyog 更省事：连上后打开本文件 → 全部执行）
-- 重复执行不会报错。
--
-- ★ 2026-11-05 实测踩坑：本机 MySQL 8.4 跑到 CREATE PROCEDURE 时报
--   ERROR 1598 (HY000) Binary logging not possible ... 'binlog_error_action' is set to 'ABORT_SERVER'. Server is being stopped.
--   ——服务被自己停掉了。用 --skip-log-bin 起一份 MySQL 再跑同一个脚本，全部成功，所以确认是 binlog 写不进去。
--   第一句把"本次会话"的二进制日志关掉就能绕开（root 有 SUPER 权限才生效；代价是这些改动不写 binlog，
--   你没有主从复制，所以没有影响）。要恢复全局 binlog：删掉这一行即可。
SET SESSION sql_log_bin = 0;

CREATE DATABASE IF NOT EXISTS javaweb DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE javaweb;

CREATE TABLE IF NOT EXISTS stu3 (
  sid     INT NOT NULL AUTO_INCREMENT,
  sname   VARCHAR(30) DEFAULT NULL,
  score   INT DEFAULT NULL,
  regdate DATE DEFAULT NULL,
  PRIMARY KEY (sid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP PROCEDURE IF EXISTS p_count_stu;
DROP FUNCTION  IF EXISTS f_avg_score;

DELIMITER //
-- 带 IN / OUT 参数的存储过程：统计"分数线及以上"的人数
CREATE PROCEDURE p_count_stu(IN line INT, OUT total INT)
BEGIN
  SELECT COUNT(*) INTO total FROM stu3 WHERE score >= line;
END //

-- 存储函数：返回平均分
CREATE FUNCTION f_avg_score() RETURNS DOUBLE
BEGIN
  DECLARE r DOUBLE;
  SELECT AVG(score) INTO r FROM stu3;
  RETURN r;
END //
DELIMITER ;

SELECT sid, sname, score, regdate FROM stu3 ORDER BY sid;

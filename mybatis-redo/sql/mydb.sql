-- ============================================================================
-- MyBatis 从头重现工程（mybatis-redo）用的建库脚本 —— 幂等版，重复执行不报错
--
-- 用法：MySQL 8.4 服务起着之后，整段贴进 IDEA 的 Database Query Console（或命令行
--       `mysql -uroot -p < sql/mydb.sql`），全选执行一次即可。
--
-- 为什么仓库里需要这个文件：本工程所有测试都要求"本机有 mydb、emp 表有 4 行种子数据"，
-- 换台机器（机房 / 面试官 clone）没有这份脚本就跑不起来。
--
-- 与 01 册 §2.2 那段 SQL 的关系（2026-11-04 用 JDBC 直连本机 mydb 核对过）：
--   种子数据完全一致（dept 3 行、emp 4 行 = 张三/李四/王五/赵六，empno=1 是张三 —— EmpDaoTest 断言的就是它），
--   区别只在**幂等**：01 册那两条 insert 没给主键，重复执行会插出第二份；
--   本脚本显式写主键 ＋ `insert ignore`，撞了主键就跳过，所以可以反复执行。
--
-- 本机 2026-11-04 01:10 实测状态（供你对照，脚本不会改动这些已有行）：
--   emp 有 7 行：empno 1/2/3/4 ＋ 三行历史测试残留的"测试员工"（empno 6/8/10）
--   张三的 salary 显示 9500（种子是 8000，被 `改()` 测试每次 +500 推上去的；本册已把 `改()` 改成跑完自动还原）
--   dept 3 行，干净
--
-- 幂等做法：显式给主键 + `insert ignore`（主键撞了就跳过，不报错），
-- 这样重复执行既不会重复插数据，也不会覆盖你已经改过的行。
-- ============================================================================

CREATE DATABASE IF NOT EXISTS mydb DEFAULT CHARACTER SET utf8mb4;
USE mydb;

CREATE TABLE IF NOT EXISTS dept (
    deptno INT PRIMARY KEY AUTO_INCREMENT,
    dname  VARCHAR(20)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS emp (
    empno    INT PRIMARY KEY AUTO_INCREMENT,
    salary   FLOAT,
    ename    VARCHAR(20),
    deptno   INT,
    hiredate DATE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
-- 注意：emp.deptno 只是普通 int，**没有建外键**。04 册讲关联映射时会用到这一点，
-- 讲解时能说一句"这里故意不建外键，方便造脏数据验证 LEFT JOIN 的行为"就够了。

-- 种子数据（4 行，和 EmpDaoTest 的断言对齐；重复执行不会插第二份）
INSERT IGNORE INTO dept(deptno, dname) VALUES
    (1, '研发部'),
    (2, '市场部'),
    (3, '财务部');

INSERT IGNORE INTO emp(empno, ename, salary, deptno, hiredate) VALUES
    (1, '张三', 8000.0, 1, '2025-03-01'),
    (2, '李四', 9500.5, 1, '2025-06-15'),
    (3, '王五', 7200.0, 2, '2026-01-10'),
    (4, '赵六', 6800.0, 3, '2026-05-20');

-- ----------------------------------------------------------------------------
-- 可选清理（**默认不执行**，需要就跑一次；2026-11-04 本机实测状态写在旁边）
-- 测试跑多了会留下这些垃圾行，01/02 册的自测脚本已改成"自己插的自己删"，以后不会再攒：
--   ① 历次 `增()` 测试残留的"测试员工"（本机实测有 3 行：empno 6 / 8 / 10）
--      DELETE FROM emp WHERE ename = '测试员工';
--   ② `改()` 测试把张三的工资从 8000 一路推到了 9500（跑了 3 次 × 500）
--      UPDATE emp SET salary = 8000.0 WHERE empno = 1;
-- 注：这两条我（Qoder）没有替你执行 —— 动你库里已有数据的事，你自己决定。
-- ----------------------------------------------------------------------------

-- 验收：下面两个 select 应该分别返回 3 行和 4 行
SELECT * FROM dept;
SELECT * FROM emp;

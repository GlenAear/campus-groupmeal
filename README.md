# campus-groupmeal · 校园拼单订餐 ＋ 智能取餐柜

面向高校宿舍/教学楼场景的拼单订餐系统：同楼栋用户凑单达到起送门槛后合并下单，取餐柜按格口分配，减少配送成本与等待时间。

**这个仓库目前分成两块**：一块是还没开始写的**主体项目**，一块是支撑它的**练习与课程代码**。两者刻意放在一起提交，是因为练习内容会直接长成主体项目的血肉（DAO 层、连接配置、分页/模糊查询都是同一件事的框架版）。读代码的人只要记住一句：**`groupmeal-server/` 才是项目，其余是练手与作业。**

## 一、主体项目（预留，尚未开工）

| 目录 | 内容 | 状态 |
|---|---|---|
| `groupmeal-server/` | Spring Boot 3 主工程：拼单成团 ＋ 取餐柜格口分配 | **占位中**，随培训进度（MyBatis → Spring → SpringMVC → Boot）逐步落地 |

规划中的 V1 范围、模块划分与选型理由见 `docs/项目技术选型分析.md`。

## 二、练习与课程（当前所有代码都在这里）

| 目录 | 内容 | 跑起来需要什么 |
|---|---|---|
| `mybatis-redo/` | MyBatis 从头重现练习工程（对照课程 PDF 逐册手写，`markdown/` 里就是配套的复现讲义） | MySQL 8.4 ＋ `sql/mydb.sql` ＋ `src/main/resources/jdbc.properties`（不外传） |
| `javaweb-course/webdemo2/` | JavaWeb / JSP / Servlet 课程作业与上机课代码（老师进度表 1–24 项，进度记在同目录 `JSP课程项目.txt`） | Tomcat 10.1.5 ＋ MySQL 8.4 ＋ `sql/users.sql` ＋ `src/main/resources/jdbc.properties` |
| `docs/` | 项目技术选型分析 | — |

> 实验报告（报告 1 环境搭建、报告 2 JSP 内置对象）按学校要求单独提交，不放在这个仓库里；仓库里只留有证据价值的代码本身。

## 三、技术栈

- **语言/JDK**：Java 17（本机 JDK 17.0.20）
- **持久层**：JDBC 手写 → MyBatis 3.5.19 → （计划）MyBatis-Plus
- **Web**：Servlet 6.0 / JSP（Jakarta EE 10）→（计划）Spring MVC → Spring Boot 3
- **数据库**：MySQL 8.4（服务名 `MySQL84`，端口 3306）
- **容器**：Apache Tomcat 10.1.5
- **构建**：Maven 3.9

## 四、本地运行（四个工程共用这四步）

1. **起数据库**：Windows 服务里启动 `MySQL84`（或用管理员权限 `net start MySQL84`）。
2. **建库建表**：整段执行 `mybatis-redo/sql/mydb.sql` 和 `javaweb-course/webdemo2/sql/users.sql`（两份都是幂等版，重复执行不报错）。
3. **补连接参数**（这一步是刻意的，**真实账号密码不进仓库**）：把各自的 `src/main/resources/jdbc.properties.example` 复制成同目录下的 `jdbc.properties`，改 `user` / `password` 两行。`.gitignore` 里已用 `**/jdbc.properties` 排除。
4. **构建**：
   - `mybatis-redo` → `mvn test`（9 个测试，全部通过需要第 1–3 步就位）
   - `webdemo2` → `mvn package` 出 war，丢进 Tomcat `webapps/` 或让 IDEA 的 Tomcat 运行配置部署

## 五、当前进度（截至 2026-11-04）

- [x] 项目方向定案与选型分析（2026-09-17）
- [x] Tomcat 10.1.5 ＋ IDEA 2024.1 环境搭建（实验报告 1）
- [x] JSP 内置对象四个案例（实验报告 2，代码在 `webdemo2/src/main/webapp/report2/`）
- [x] MyBatis 重现 01 册：环境 ＋ 第一个 CRUD（2026-09-22 跑通）
- [x] MyBatis 重现 02 册 Step 1–3：Mapper 代理、namespace/id 边界、resultMap（2026-11-03）
- [x] JavaWeb 上机课第 5–16 项：include 指令、application 计数、JavaBean、数据库登录/注册/管理员（2026-11-03）
- [ ] MyBatis 重现 02 册 Step 4–6.5（AS 别名、autoMapping 实验、下划线转驼峰、@Param 多参数）
- [ ] MyBatis 重现 03–09 册（动态 SQL、关联映射、缓存、分页）
- [ ] JavaWeb 上机课第 17–24 项（查询所有/删除/修改/模糊查询/**分页**）
- [ ] `groupmeal-server/` Spring Boot 骨架
- [ ] V1：拼单成团 ＋ 取餐柜格口分配

## 作者

侯奕聪 ｜ 陕西学前师范学院 · 24 级物联网工程

# groupmeal-server（占位 · 尚未开工）

主体项目工程的位置：**Spring Boot 3 ＋ MyBatis ＋ MySQL**，实现"同楼栋拼单成团 ＋ 智能取餐柜格口分配"。

这里目前只有一个说明文件，是因为**目录先占住，代码按培训进度长出来**：

| 阶段 | 会先落地什么 | 从哪个练习工程搬过来 |
|---|---|---|
| 1 | dao / mapper / entity（拼单、订单、柜格三张核心表） | `../mybatis-redo/`（Mapper 代理、resultMap、动态 SQL、分页） |
| 2 | 连接与配置外置（`application.yml` ＋ 多环境） | `../mybatis-redo/` 与 `../javaweb-course/webdemo2/` 的 `jdbc.properties` 同一套思路 |
| 3 | service ＋ 事务（成团判定、超时未成团退款） | MyBatis 06 册（缓存）／Spring 事务 |
| 4 | 接口层（RESTful，替代 JSP 页面） | 培训 SpringMVC 部分 ＋ `../javaweb-course/webdemo2/` 的登录/注册/管理员三个 Servlet 的业务语义 |
| 5 | V1 验收：拼单成团 ＋ 取餐柜格口分配 | — |

约定（现在就已经生效）：

- **连接参数不进仓库**：只提交 `application.yml` 的非敏感部分，账号密码走本地配置／环境变量。
- 表字段一律**下划线命名**，Java 侧驼峰，靠 `mapUnderscoreToCamelCase` 桥接（见 MyBatis 02 册 Step 6）。
- 选型理由与模块划分见 `../docs/项目技术选型分析.md`。

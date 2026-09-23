# MyBatis 从头重现 · 01 环境与第一个 CRUD

> ⚠️ **菜单名怎么对上**：本文件里出现的 IDEA 菜单路径，若与你中文界面上的词不一致，看 **`D:\我的\复盘\相关信息\IDEA菜单中英对照表.md`**；或者更省事——**双击 `Shift` → 切 `Actions` → 直接打英文名**，那一行会显示它的中文位置和快捷键。（2026-09-21 新增：我的所有文档从今往后一律写成「English（中文）」双标。）

> **PDF 主线**：§1 MyBatis 是什么(p1)、§2 搭建项目(p1)、§3 Maven(p1–2)、§4 开发步骤 0–7(p2–8)、§14 JUnit(p19)
> **折入纪要**：`9.12_1mybatis流程回顾.txt` 全部要点（已在正文对应位置标出「纪要」）
> **时间盒**：120 分钟 ｜ **落点**：9/19（六）自主时段首选；做不完按 §6 降级，不欠账
> **前置**：先读同目录《00 索引与总览》的 §4 通用约定（包名、版本、连接串口径）

---

## 0. 本册目标与验收（3 行）

1. 从零建出一个 Maven 工程 `mybatis-redo`，连上本机 MySQL 8.4 的 `mydb` 库。
2. 用 **JUnit5 ＋ `getMapper` 代理接口** 跑通 `emp` 表的 **查全部／按 id 查／增／改／删** 五条。
3. 控制台能看到 MyBatis 打印出的**真实 SQL 与参数**（证明日志配置生效，这是后面所有册的调试基础）。

**做完的判据**：`EmpDaoTest` 五个测试方法全绿，且数据库里能看到你插入/修改/删除的痕迹。

---

## 1. 前置检查（3 min，我 9/18 已核对过的部分你只需确认）

| # | 检查 | 怎么做 | 期望 |
|---|---|---|---|
| 1 | MySQL 服务在跑 | `Win+R` → `services.msc` → 找 `MySQL84` | 状态 **正在运行**（我 9/18 查过是 Running，端口 3306 已监听） |
| 2 | 你知道 root 密码 | 一会儿 IDEA 连库要用 | **如果忘了 → 立刻告诉我**，重置密码要改 `my.ini`，别自己瞎试，会耗掉整晚 |
| 3 | IDEA 的 Maven 指向你自己的 Maven | `Settings → Build, Execution, Deployment → Build Tools → Maven` | Maven home path＝`D:\Java\Maven\apache-maven-3.9.0`；User settings file 勾 **Override** 指到 `…\conf\settings.xml`；下方 Local repository 自动变成 `…\repo` |
| 4 | 新建项目设置也要改 | 同页面 → **Maven → Importing / 或 `File → New Projects Setup → Settings for New Projects`** 里同样改一遍 | PDF p1 特别提醒过：不改这里，**每次新建工程都会退回 C 盘默认仓库**，依赖要重下一遍 |

> 「标注」**第 3、4 步是本册最容易浪费时间的地方**。你本地仓库里已经缓存了 mybatis 3.5.19、mysql-connector-j 8.0.33、lombok 1.18.42、junit-jupiter 5.9.2 —— 只要 IDEA 用对了 Maven，这四个依赖**一秒命中，不用联网**。用错了就要重下几百 MB。
> 「纪要」PDF p1 的原话：*"在 idea 开发工具中配置自定义的 Maven 目录：File → Settings → 搜索 Maven 进行配置……为了防止每次新建工程时又恢复到默认的 C 盘目录，建议配置'新建项目设置'"*。

---

## 2. Step 1 · 建库建表（PDF p2 §4.0，15 min）

### 2.1 用 IDEA 自带的 Database 工具连库（你是 Ultimate，不用装 Navicat）

1. IDEA 右侧边栏点 **Database**（或 `View → Tool Windows → Database`）。
2. 左上 **`+`** → **Data Source → MySQL**。
3. 第一次会提示 **Download missing driver files** → 点它，等几秒（IDEA 自己下 MySQL 驱动，与 pom 里那个是两回事，别混）。
4. 填：Host `localhost`、Port `3306`、User `root`、Password 你的密码、Database 先留空。
5. **Test Connection** → 出现 `Succeeded` → OK。

### 2.2 建库建表（PDF p2 原样 SQL，直接执行）

在 Database 窗口选中刚建的数据源 → 上方 **Open Console**（或右键 → New → Query Console）→ 粘贴 → 全选 → `Ctrl+Enter` 执行：

```sql
create database if not exists mydb default character set utf8mb4;
use mydb;

create table dept(
    deptno int primary key auto_increment,
    dname  varchar(20)
);

create table emp(
    empno   int primary key auto_increment,
    salary  float,
    ename   varchar(20),
    deptno  int,
    hiredate date
);

-- 塞几条测试数据（PDF 没给，我补的，否则查出来是空的没法验证）
insert into dept(dname) values ('研发部'), ('市场部'), ('财务部');
insert into emp(ename, salary, deptno, hiredate) values
 ('张三', 8000.0, 1, '2025-03-01'),
 ('李四', 9500.5, 1, '2025-06-15'),
 ('王五', 7200.0, 2, '2026-01-10'),
 ('赵六', 6800.0, 3, '2026-05-20');

select * from emp;
select * from dept;
```

**验收**：最后两条 select 分别返回 4 行和 3 行；左侧 Database 树里 `mydb → tables` 出现 `dept`、`emp`。

> 「标注」三条：
> ① PDF 的建库语句是 `create database mydb;`，我加了 `default character set utf8mb4` —— 不指定的话 MySQL 8.4 默认也是 utf8mb4，但显式写出来更稳（后面存中文菜品名不会出乱码）。
> ② `emp.deptno` 只是普通 int，**没有建外键**。第 04 册做关联映射时这样反而方便（不会因为外键约束卡住增删改）。纪要里老师提过"基本类型默认值 0 会引发主外键约束异常"，那是指 Java 侧用 `int` 的坑，不是数据库侧。
> ③ **MySQL 8.4 的认证插件是 `caching_sha2_password`**（`mysql_native_password` 已被移除）。IDEA 连没问题；后面 Java 代码连的时候，连接串必须带 `allowPublicKeyRetrieval=true`，否则报 `Public Key Retrieval is not allowed`。

### 2.3 顺手记一条面试口径（PDF p1 §1）

**MyBatis 是什么**：一款优秀的 **ORM 持久化框架**。
- **ORM** ＝ Object-Relation-Mapping：Java 对象（类）↔ 关系型数据库（表）↔ 映射配置。映射的是**类名↔表名、属性↔字段、Java 类型↔数据库类型**。
- **持久化** ＝ 瞬时状态（内存）与持久状态（磁盘）相互转化的过程。**Insert/Update/Delete 是瞬时→持久，Select 是持久→瞬时**（「纪要」明确说这是面试常考基础概念）。
- **框架** ＝ 一个半成品。
- **半自动化 vs 自动化**（PDF p2）：Hibernate 全自动（开发快、代码少、不用写 SQL，但缺灵活性）；MyBatis 半自动（灵活、运行效率较高、省略自动生成 SQL 的过程，但要自己写 SQL、代码量相对大）。

---

## 3. Step 2 · 建 Maven 工程（PDF p1–2 §2/§3，10 min）

### 3.1 主线（PDF 的做法）

`File → New → Project` → 左侧 **Empty Project** → 名字随意 → Create；然后在工程上右键 → **New → Module** → 选 **Maven** → 名字 `mybatis-redo` → Create。

### 3.2 更快的做法（推荐，省一步）

`File → New → Project` → 左侧 **Java** → **Build system 选 Maven** → 勾 **Add sample code** → Name `mybatis-redo` → Location `D:\Java\Projects`（注意：填到 `Projects` 这一层，IDEA 会自己建子目录）→ JDK 选 **17** → Create。

**验收**：工程根目录有 `pom.xml`、`src\main\java`、`src\main\resources`、`src\test\java`（`src\test\resources` 没有就自己建）。

> 「标注」**"约定大于配置"**（PDF p2 原话）：Maven 规定了目录用途 —— 源代码 `src/main/java`、资源文件 `src/main/resources`、测试代码 `src/test/java`。**MyBatis 的 xml 必须放 `src/main/resources`**，因为只有这个目录下的文件才会被打进 classpath；放到 `src/main/java` 下面，运行时就会报 `Invalid bound statement (not found)`（这是新手第一大坑，第 07 册的注解方式就是为了绕开它）。
> 「标注」PDF p1 还讲了 Maven 是什么：一款 Java **自动构建工具**（源码→编译→测试→部署→安装到服务器），同类有 Ant、Gradle；传统 Web 项目要手动把 jar 拷进 `WEB-INF/lib`（jar 多、版本容易不一致），Maven Web 项目只需配 `pom.xml` 就自动下载、导入、管理 jar。**这段话可以直接抄进你未来项目的技术选型说明里。**

---

## 4. Step 3 · pom.xml 依赖（PDF p3 §4.1，5 min）

把 `pom.xml` 整个替换成下面这份（**版本按你本地已缓存的来，见 00 索引 §4.2**）：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.groupmeal</groupId>
    <artifactId>mybatis-redo</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- MyBatis 核心 -->
        <dependency>
            <groupId>org.mybatis</groupId>
            <artifactId>mybatis</artifactId>
            <version>3.5.19</version>
        </dependency>
        <!-- MySQL 8 驱动（新坐标 com.mysql:mysql-connector-j） -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <version>8.0.33</version>
        </dependency>
        <!-- Lombok：@Data 省 getter/setter（纪要 9.12_1） -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.42</version>
            <scope>provided</scope>
        </dependency>
        <!-- JUnit5 单元测试（PDF §14） -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.9.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

保存后 IDEA 右下角会转圈下载/索引；**或**右键 pom → **Maven → Reload Project**。

**验收**：`External Libraries` 里能看到 `Maven: org.mybatis:mybatis:3.5.19`、`Maven: com.mysql:mysql-connector-j:8.0.33`、`lombok`、`junit-jupiter`。

> 「标注」**PDF 写的是 `mysql:mysql-connector-java:8.0.29`，我改成了 `com.mysql:mysql-connector-j:8.0.33`**，两个原因：①旧坐标 Oracle 已弃用（Maven 中央仓库里那个 artifact 停更了）；②你的 MySQL 是 **8.4**，配 8.0.29 的老驱动容易在认证插件上出问题。**若老师课上要求跟 PDF 一致，改回版本号即可，代码一行都不用动。**

---

## 5. Step 4 · mybatis-config.xml（PDF p3 §4.2 ＋ p4 ＋ p9 §7，15 min）

在 `src\main\resources` 下新建文件 `mybatis-config.xml`（PDF 里写的是 `mybatis-coinfig.xml`，那是老师的拼写错误，**名字可以自定义**，但后面代码里要一致）：

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
        PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "https://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>

    <!-- ① 全局设置：打开 SQL 日志（PDF p5「日志配置」）＋ 下划线转驼峰 -->
    <settings>
        <setting name="logImpl" value="STDOUT_LOGGING"/>
        <setting name="mapUnderscoreToCamelCase" value="true"/>
    </settings>

    <!-- ② 类型别名：扫整个包，之后映射文件里可直接写 Emp 而不必写全类名（纪要「配置别名简化」） -->
    <typeAliases>
        <package name="com.groupmeal.entity"/>
    </typeAliases>

    <!-- ③ 环境：数据库四要素（驱动、URL、用户名、密码）＝所有 JDBC 封装框架的通用要求（纪要原话） -->
    <environments default="devA">
        <environment id="devA">
            <transactionManager type="JDBC"/>
            <dataSource type="POOLED">
                <property name="driver"   value="com.mysql.cj.jdbc.Driver"/>
                <property name="url"      value="jdbc:mysql://127.0.0.1:3306/mydb?useSSL=false&amp;serverTimezone=Asia/Shanghai&amp;characterEncoding=utf8&amp;allowPublicKeyRetrieval=true"/>
                <property name="username" value="root"/>
                <property name="password" value="改成你的root密码"/>
            </dataSource>
        </environment>
    </environments>

    <!-- ④ 注册映射文件（纪要：新增 mapper 必须在这里注册，否则容器不认识它） -->
    <mappers>
        <mapper resource="mapper/EmpMapper.xml"/>
    </mappers>

</configuration>
```

### 5.1 ⚠️ 四个必踩的坑（先看完再动手）

1. **`&` 必须写成 `&amp;`**。XML 里裸写 `&` 会直接报解析错误（`Error parsing SQL Mapper Configuration`）。上面连接串里四个 `&amp;` 就是这么来的。
2. **标签顺序不能乱**。DTD 规定的顺序是：`properties → settings → typeAliases → typeHandlers → objectFactory → objectWrapperFactory → reflectorFactory → plugins → environments → databaseIdProvider → mappers`。**「纪要」里老师特别强调：分页插件的 `plugins` 要放在 `environments` 前面**（第 05 册会用到），写反了 IDEA 会直接飘红。
3. **`<mapper resource="mapper/EmpMapper.xml"/>` 现在会飘红**，因为文件还没建 —— **正常，Step 6 建完就消失**，别在这卡住。
4. **密码不要提交到 Git**。W4 建仓时这个文件要进 `.gitignore`，或改成从 `jdbc.properties` 读（第 05 册做外部化配置时正好解决）。我在 00 索引里已经把它记为建仓前必办事项。

### 5.2 三个属性的取值口径（PDF p3–4 ＋ p10，面试会问，读一遍）

**`<transactionManager type>`**：
- **JDBC** —— 直接用 JDBC 的事务机制，调用 `Connection` 的 `commit()`/`rollback()`；适用于简单事务管理（**我们用这个**）。
- **MANAGED** —— 由容器管理事务生命周期，MyBatis 不主动提交或回滚；适用于部署在 Tomcat/WebLogic 等应用服务器上时。
- **自定义** —— 实现 `TransactionFactory` 接口，配置时写全限定类名。

**`<dataSource type>`**：
- **UNPOOLED** —— 每次请求都新建连接、用完立即关闭；适用于简单应用或测试环境。
- **POOLED** —— 连接池管理，提高性能；适用于生产环境（**我们用这个**）。
- **JNDI** —— 从 Java 容器的命名服务里取数据源；适用于部署在应用服务器上的企业应用。

> 「标注」**PDF §7 配置文件详解（p9）把 `<configuration>` 下 9 个子标签的作用逐条列了**，那张清单值得抄进你的笔记（第 02 册我会再引用一次）：`configuration` 根元素 / `properties` 加载外部属性文件（可用 `${property}` 引用）/ `settings` 全局参数（自动映射模式、延迟加载策略、日志工厂）/ `typeAliases`、`typeHandlers` / `plugins` 注册拦截器 / `environments` 多环境（每个环境唯一 id，可指定默认）/ `transactionManager` / `dataSource` / `mappers` 注册映射器接口或 XML（可用类路径或绝对路径）。

---

## 6. Step 5 · 实体类 Emp（PDF p4 §4.3，5 min）

`src\main\java` 下建包 `com.groupmeal.entity`，新建 `Emp.java`：

```java
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
```

**验收**：`Emp` 类不飘红；随便写一行 `new Emp().setEname("x")` 能补全出来（说明 Lombok 生效）。

> 「标注」**为什么必须用包装类**（「纪要」9.12_3 原话，第 03 册动态 SQL 会真正吃到这个亏）：基本类型在 JVM 里默认值是 `0`／`false`，MyBatis 动态 SQL 用 `<if test="empno != null">` 判断时，`int empno` 永远不为 null（它是 0），于是**没赋值的字段也会被当成"有值"拼进 SQL**，轻则改错数据，重则触发主外键约束异常。
> 「标注」**Lombok 不生效的排查顺序**：① IDEA 2024 已内置 Lombok 插件，若没有则 `Settings → Plugins` 搜 Lombok 装上；② `Settings → Build, Execution, Deployment → Compiler → Annotation Processors` → 勾 **Enable annotation processing**；③ Reload Maven。三步做完还不行就发我。

---

## 7. Step 6 · Dao 接口 ＋ 映射文件（PDF p4 §4.4，15 min）

### 7.1 Dao 接口（只声明，不实现 —— 实现由 MyBatis 动态代理生成）

`com.groupmeal.dao.EmpDao`：

```java
package com.groupmeal.dao;

import java.util.List;

public interface EmpDao {
   int getCount();                       // PDF p4 的第一个示例方法

   List<Emp> queryAll();                   // 查全部

   Emp queryByEmpno(Integer empno);      // 按主键查

   int saveEmp(Emp emp);                 // 增

   int updateEmp(Emp emp);               // 改

   int deleteByEmpno(Integer empno);     // 删
}
```

> 「纪要」*"创建 UserDao 接口并定义查询方法，接口中仅声明方法而无具体实现"* —— 这就是 MyBatis 的核心机制：**你写接口，框架用 JDK 动态代理生成实现类**，SQL 写在 XML（或注解）里，靠 `namespace + id` 与接口方法绑定。

### 7.2 映射文件

`src\main\resources` 下建目录 `mapper`，新建 `EmpMapper.xml`：

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "https://mybatis.org/dtd/mybatis-3-mapper.dtd">
<!-- ⚠️ namespace 必须是 Dao 接口的完整类名（纪要原话：以实现接口方法的绑定与实现） -->
<mapper namespace="com.groupmeal.dao.EmpDao">

    <select id="getCount" resultType="int">
        select count(*) from emp
    </select>

    <!-- resultType="Emp" 能这么写，是因为 Step 4 配了 typeAliases 包扫描 -->
    <select id="queryAll" resultType="Emp">
        select empno, ename, salary, deptno, hiredate from emp
    </select>

    <select id="queryByEmpno" parameterType="int" resultType="Emp">
        select empno, ename, salary, deptno, hiredate
        from emp where empno = #{empno}
    </select>

    <!-- useGeneratedKeys/keyProperty：插入后把自增主键回填到 emp.empno（PDF 没写，项目里必用） -->
    <insert id="saveEmp" parameterType="Emp" useGeneratedKeys="true" keyProperty="empno">
        insert into emp(ename, salary, deptno, hiredate)
        values(#{ename}, #{salary}, #{deptno}, #{hiredate})
    </insert>

    <update id="updateEmp" parameterType="Emp">
        update emp
        set ename = #{ename}, salary = #{salary}, deptno = #{deptno}, hiredate = #{hiredate}
        where empno = #{empno}
    </update>

    <delete id="deleteByEmpno" parameterType="int">
        delete from emp where empno = #{empno}
    </delete>

</mapper>
```

**验收**：①`mybatis-config.xml` 里那行飘红消失；②每个 `id` 与 `EmpDao` 的方法名**一字不差**；③`namespace` 与接口全类名**一字不差**。

> 「标注」**`id` 与 `namespace` 写错是本册 90% 的报错来源**，症状统一是 `org.apache.ibatis.binding.BindingException: Invalid bound statement (not found)`。排查顺序：namespace → id → xml 是否在 `src/main/resources` 下 → `mybatis-config.xml` 的 `<mappers>` 有没有注册它 → 路径大小写。
> 「标注」**上面这条 `update` 是"全字段更新"，正是第 03 册动态 SQL 要解决的问题**：如果只想改工资，其它字段没传，就会被置成 null。现在先照抄，第 03 册会把它改成 `<set>` ＋ `<if>` 的版本，你会立刻明白动态 SQL 的价值。

---

## 8. Step 7 · 工具类 ＋ 第一个 main（PDF p5 §5，10 min）

### 8.1 三大 API 的关系（先记住这条链，「纪要」重点）

```
SqlSessionFactoryBuilder  --读配置文件输入流-->  SqlSessionFactory  --openSession()-->  SqlSession
      （用完即弃）                                  （全局唯一，静态持有）              （一次会话，用完 close）
```

`com.groupmeal.util.MyBatisUtil`：

```java
package com.groupmeal.util;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

public class MyBatisUtil {

    private static SqlSessionFactory factory;

    // 类加载时执行一次：解析 xml 配置与映射文件，构建工厂
    static {
        try (InputStream in = Resources.getResourceAsStream("mybatis-config.xml")) {
            factory = new SqlSessionFactoryBuilder().build(in);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ExceptionInInitializerError("mybatis-config.xml 读取失败：" + e.getMessage());
        }
    }

    /** 读操作用：默认不自动提交（等价于 openSession(false)） */
    public static SqlSession getSession() {
        return factory.openSession();
    }

    /** 写操作（增删改）用：true = 自动提交事务 */
    public static SqlSession getSession(boolean autoCommit) {
        return factory.openSession(autoCommit);
    }

    public static void close(SqlSession session) {
        if (session != null) session.close();
    }
}
```

> 「纪要」**本册最重要的一条坑**：*"若未显式传入参数，系统默认事务自动提交为 false，导致增删改操作无法持久化入库；执行保存、删除或修改等写操作时，必须将事务设置为 true"*。PDF p5 也写了：`sqlSessionFactory.openSession()` **默认等价于 `openSession(false)`**。
> → 症状：代码跑完没报错、`System.out` 也打印了"更新 1 行"，但**数据库里数据没变**。99% 是这个原因。两种解法：`openSession(true)`，或写完手动 `session.commit()`（异常时 `session.rollback()`）。

### 8.2 先用 main 打通一次（PDF p5 的 `TestMyBatis`）

`com.groupmeal.TestMyBatis`（**放在 src/main/java**，只为最快看到结果）：

```java
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
```

右键 → Run。**验收**：控制台输出 `emp 表记录数 = 4`，并且**上面还打印了一段 MyBatis 日志**，形如：

```
==>  Preparing: select count(*) from emp
==> Parameters:
<==      Total: 1
```

看到 `==> Preparing:` 就说明 **Step 4 的 `logImpl=STDOUT_LOGGING` 生效了** —— 这行日志是你后面所有调试的命根子。

> 「纪要」*"当前不推荐使用直接调用 SqlSession API 的方式，而是应通过 SqlSession 的 getMapper 方法获取代理接口进行查询等操作"* → 所以下一步（Step 8）改用 `getMapper`，`TestMyBatis` 这个类跑通后可以删掉。

---

## 9. Step 8 · JUnit5 测试类（PDF p19 §14 ＋ 纪要，20 min）

`src\test\java` 下建 `com.groupmeal.EmpDaoTest`（**类名不能叫 `Test`，也不要叫 `Emp`／`EmpDao`**）：

```java
package com.groupmeal;

import com.groupmeal.dao.EmpDao;
import com.groupmeal.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.*;

import java.util.Date;
import java.util.List;

class EmpDaoTest {

   private SqlSession session;
   private EmpDao dao;

   @BeforeEach
      // 每个 @Test 之前都执行一次
   void setUp() {
      session = MyBatisUtil.getSession(true);   // ⚠️ 写操作必须 true
      dao = session.getMapper(EmpDao.class);    // ⚠️ 用代理接口，不要直接调 session.selectList
   }

   @AfterEach
      // 每个 @Test 之后都执行一次
   void tearDown() {
      MyBatisUtil.close(session);
   }

   @Test
   void 查全部() {
      List<Emp> list = dao.queryAll();
      list.forEach(System.out::println);
      Assertions.assertFalse(list.isEmpty(), "emp 表不该是空的");
   }

   @Test
   void 按主键查() {
      Emp emp = dao.queryByEmpno(1);
      System.out.println(emp);
      Assertions.assertNotNull(emp);
      Assertions.assertEquals("张三", emp.getEname());
   }

   @Test
   void 增() {
      Emp emp = new Emp();
      emp.setEname("测试员工");
      emp.setSalary(6000f);
      emp.setDeptno(2);
      emp.setHiredate(new Date());
      int rows = dao.saveEmp(emp);
      System.out.println("插入行数=" + rows + "，回填的自增主键 empno=" + emp.getEmpno());
      Assertions.assertEquals(1, rows);
      Assertions.assertNotNull(emp.getEmpno(), "useGeneratedKeys 应该把主键回填进来");
   }

   @Test
   void 改() {
      Emp emp = dao.queryByEmpno(1);
      emp.setSalary(emp.getSalary() + 500f);
      int rows = dao.updateEmp(emp);
      Assertions.assertEquals(1, rows);
      System.out.println("改后：" + dao.queryByEmpno(1));
   }

   @Test
   void 删() {
      // 先插一条再删，避免把测试数据删光
      Emp emp = new Emp();
      emp.setEname("待删除");
      emp.setSalary(1000f);
      emp.setDeptno(1);
      emp.setHiredate(new Date());
      dao.saveEmp(emp);
      int rows = dao.deleteByEmpno(emp.getEmpno());
      Assertions.assertEquals(1, rows);
      Assertions.assertNull(dao.queryByEmpno(emp.getEmpno()), "删完应该查不到了");
   }
}
```

**运行**：点类名左边的绿色三角 → Run 'EmpDaoTest'（五个方法一起跑）。**验收**：全绿 ＋ 控制台能看到每条 SQL 的 `==> Preparing:` / `==> Parameters:` / `<== Total:`。

> 「纪要」三条关于 JUnit 的原话，都值得记住：
> ① **`@BeforeEach` vs `@BeforeAll`**：`@BeforeEach` 在**每个**测试用例前都执行；`@BeforeAll` 只在**整个测试类加载时执行一次**，且必须是 **static 方法**。→ 我们要每个用例都有独立的 SqlSession（否则一级缓存会让你看不到真实 SQL，第 06 册会演示这个现象），所以用 `@BeforeEach`。
> ② **命名冲突**：*"若自定义类名或方法名为 Test，会导致 JUnit 的 @Test 注解被就近原则误认为是自定义类而报错"* → 所以类名叫 `EmpDaoTest`，且**不要在你的包里建任何叫 `Test` 的类**。
> ③ *"严禁使用与实体或接口相同的名称，建议右键重命名并添加后缀"* → 同上，`Emp`/`EmpDao`/`EmpDaoTest` 三个名字必须不同。
>
> 「标注」我把测试方法名写成了中文（`查全部()`），Java 允许，且一眼能看出在测什么。**如果 IDEA 或你的 JDK 编码设置有问题导致中文方法名报错，就改成 `testQueryAll()` 这种英文名**，不影响任何逻辑。

---

## 10. Step 9 · `#{}` 与 `${}`（PDF p8 §6，5 min，只读不写）

| | `#{}` | `${}` |
|---|---|---|
| 本质 | **预编译占位符**，生成 `?`，用 `PreparedStatement` 设值 | **字符串直接拼接**，值原样替换进 SQL |
| 安全 | 防 SQL 注入 | **有注入风险** |
| 用法 | 传值一律用它 | 只用于**无法用占位符的场景**：动态表名、动态列名、`order by` 的字段名 |
| 例 | `where empno = #{empno}` → `where empno = ?` | `order by ${col}` → `order by salary` |

**面试口径一句话**：*"`#{}` 是预编译占位符，能防 SQL 注入；`${}` 是字符串拼接，只在动态表名/排序字段这类占位符无法覆盖的场景使用，且必须自己校验入参。"*

> 「标注」一个常踩的细节：`#{}` 传字符串时会**自动加引号**（`where ename = '张三'`），`${}` 不会（`where ename = 张三` → 语法错误）。所以 `${}` 传字符串必须自己写成 `'${name}'`，而这就是注入的入口。

---

## 11. 合卷自测（做完本册才看，能答上就说明真的会了）

1. 从零搭一个 MyBatis 工程，**7 个步骤的顺序**是什么？（建库建表 → Maven 工程 → pom 依赖 → 核心配置文件 → 实体类 → Dao 接口＋映射文件 → 工具类/测试）
2. `mybatis-config.xml` 里**本册用到的 4 个块**，以及 DTD 规定的**先后顺序**？
3. `SqlSessionFactoryBuilder` / `SqlSessionFactory` / `SqlSession` 三者的**生命周期**分别是什么？为什么工厂要 static 持有？
4. 为什么写操作要 `openSession(true)`？不传参数默认是什么？症状是什么？
5. `namespace` 必须写什么？写错的报错信息是什么？
6. 为什么映射文件必须放在 `src/main/resources`？
7. `#{}` 与 `${}` 的区别，各举一个只能用它的那个场景。
8. 实体类属性为什么必须用包装类？（第 03 册会真正吃到这个亏）

**答不上来的那条，就是你下次要重做的那一步。**

---

## 12. 报错急救表（本册高频 12 条）

| 现象 | 根因 | 解法 |
|---|---|---|
| `Invalid bound statement (not found)` | namespace/id 不匹配、xml 不在 resources 下、`<mappers>` 没注册 | 按 §7.2 标注的四步顺序查 |
| `Error parsing SQL Mapper Configuration` | xml 语法错（多半是裸 `&`）或标签顺序错 | `&`→`&amp;`；顺序按 §5.1 第 2 条 |
| `Access denied for user 'root'@'localhost'` | 密码错 | 改 `mybatis-config.xml` 里的 password |
| `Public Key Retrieval is not allowed` | MySQL 8.4 的 caching_sha2_password | 连接串加 `allowPublicKeyRetrieval=true` |
| `The server time zone value 'xxx' is unrecognized` | MySQL 8 驱动必填时区 | 连接串加 `serverTimezone=Asia/Shanghai` |
| `Communications link failure` | MySQL 服务没起／端口不对 | `services.msc` 启动 `MySQL84`；确认 3306 |
| `ClassNotFoundException: com.mysql.jdbc.Driver` | 用了旧驱动类名 | 改成 `com.mysql.cj.jdbc.Driver` |
| 增删改跑完数据库没变 | `openSession()` 默认 false | 用 `openSession(true)` 或手动 `commit()` |
| `Cannot resolve symbol 'Data'`／getter 补全不出来 | Lombok 未生效 | §6 标注的三步排查 |
| 依赖一直下不动／下到 C 盘 | IDEA 用的不是你的 Maven | §1 的第 3、4 项（含"新建项目设置"） |
| 测试类报 `initializationError`／`@Test` 不识别 | 类名叫 Test，或 junit 依赖 scope 错 | 改名 `XxxTest`；确认 `junit-jupiter` 的 `<scope>test</scope>` |
| 控制台 SQL 日志没打出来 | 没配 `logImpl` | `<settings>` 里加 `STDOUT_LOGGING`（§5 的 ①） |

---

## 13. 时间盒与降级阶梯（120 min）

| 时段 | 内容 | 累计 |
|---|---|---|
| 0:00–0:05 | §1 前置检查（重点看 IDEA 的 Maven 指向） | 5 |
| 0:05–0:20 | Step 1 建库建表＋塞测试数据 | 20 |
| 0:20–0:30 | Step 2 建 Maven 工程 | 30 |
| 0:30–0:35 | Step 3 pom 依赖＋Reload | 35 |
| 0:35–0:50 | Step 4 mybatis-config.xml（含四个坑） | 50 |
| 0:50–0:55 | Step 5 实体类 | 55 |
| 0:55–1:10 | Step 6 Dao＋映射文件 | 70 |
| 1:10–1:20 | Step 7 工具类＋main 打通（**看到 `==> Preparing:` 就成功一半了**） | 80 |
| 1:20–1:40 | Step 8 JUnit 五个用例全绿 | 100 |
| 1:40–1:45 | Step 9 `#{}`/`${}` 读一遍 | 105 |
| 1:45–2:00 | §11 合卷自测＋§14 结束清单 | 120 |

**降级阶梯**（砍到哪都不欠账）：
- **满配**：Step 1–9 ＋ 合卷自测，五个测试全绿。
- **标准（＝W3 产出物③达成）**：Step 1–7 ＋ Step 8 只做 `查全部()` 和 `增()` 两个用例 —— **"工程能跑＋CRUD 走通一条"** 就是这个标准。
- **保底（＝L0 达成）**：Step 1–4（库建好、工程建好、配置文件写完）＋ 把 `main` 里的 `getCount()` 跑出数字。**这一步做到，W4 接着往下就是纯增量。**
- **最低**：只做 Step 1（建库建表＋塞数据）。数据库这步一旦完成，后面任何时候接着做都不用重来。

---

## 14. 结束清单（5 min）

1. ☐ 工程复制一份到 `D:\Java\Projects\backup_20260919\mybatis-redo\`（Git 未建仓，这是唯一版本保护）：
   ```
   xcopy "D:\Java\Projects\mybatis-redo" "D:\Java\Projects\backup_20260919\mybatis-redo\" /E /I /H /Y
   ```
2. ☐ 日志 3 行（`日志模板_简化版.md`），"做了"那行写清**跑到第几 Step、几个测试绿了**。
3. ☐ **不 commit**（commit 线等 W4 管线③建仓后才生效）。
4. ☐ `mybatis-config.xml` 里的**数据库密码**记一笔"W4 建仓前要外部化/加 .gitignore"（第 05 册会做 `jdbc.properties`，正好解决）。
5. ☐ 合卷自测答不上来的条目，写进日志的"卡点"行 —— 我下次开新册前会先补那一块。

---

## 15. 卡点上报格式（卡住 15 分钟就发我）

```
卡在第 01 册 Step __。
报错原文（整段复制，别翻译）：

控制台最后的 ==> Preparing / ==> Parameters 那几行：
我已经试过：
截图：（Win+Shift+S）
```

---

*01 册 v1.0 ｜ 2026-09-18 ｜ 下一册：`02_Mapper接口方式与结果映射`（PDF §8–§9）*

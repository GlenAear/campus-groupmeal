# MyBatis 从头重现 · 02 ｜ Mapper 接口方式 ＋ 结果映射

> ⚠️ **菜单名怎么对上**：本文件里出现的 IDEA 菜单路径，若与你中文界面上的词不一致，看 **`D:\我的\复盘\相关信息\IDEA菜单中英对照表.md`**；或者更省事——**双击 `Shift` → 切 `Actions` → 直接打英文名**，那一行会显示它的中文位置和快捷键。（2026-09-21 新增：我的所有文档从今往后一律写成「English（中文）」双标。）

> **主线**：培训 PDF《Mybatis 框架.pdf》§8 Mapper 接口方式（p10–11）、§9 结果映射（p11–12）
> **时间盒**：90 min ｜ **前置**：第 01 册全部完成（工程能跑、5 条 CRUD 在 JUnit 里绿了、`mydb.emp` 有数据）
> **本册定位**：01 册已经让你用上了 `getMapper()`，本册回答三个"为什么"和两个"怎么办"——为什么不用字符串调用、namespace/id 的边界到底在哪、resultMap 和 resultType 怎么分工；字段名和属性名不一致怎么办、自动映射失控怎么办。
> **完成判据（做到这句就算过）**：能用 `resultMap` 把 `salary→money`、`hiredate→hDate` 这种不一致映射对；能解释 `autoMappingBehavior=NONE` 为什么会让一个原本正常的 `resultType` 查询**返回 null**；知道真实项目里下划线字段（`create_time`）靠什么映射成驼峰属性。

> **★ 2026-10-15 夜修订说明（4 处，读之前先看）**：
> ① §1.1 的 `setUp` 原来写 `getSession(true)` 拿去做**查询**——这正是你 9/22 提的"读写都传 true"坏习惯的残留，已改为 `getSession()`（读不传参），口径与《01回顾》§6 一致。
> ② 新增 **§6.5 多参数与 @Param**——原册全篇 0 处 `@Param`，而"方法有两个参数"是 Mapper 接口方式最常踩的坑，就业必会。
> ③ §10 自检 5 问**补了参考答案**（原册只有题没有答案）。
> ④ 开头加"接续路径"：你 10/15 夜已做完 §1.1，**从 §1.2 接着做**。
>
> **接续路径（你现在的位置）**：§1.1 字符串方式两个测试 ✅ 已做 → 下一步 **§1.2 换代理方式** → §1.3 看三个缺点 → 然后按 §0 那张表建 `EmpVo` 进 §3。今晚若只剩 30min，就只做 §1.2＋§1.3（10min）＋读 §3 开头（20min），**不欠账**。

---

## 0. 本册要新建/改动的文件（先看全景，30 秒）

| 动作 | 文件 | 干什么 |
|---|---|---|
| 新建 | `src/main/java/com/groupmeal/vo/EmpVo.java` | 故意让属性名**和表字段全不一致**，用来练 resultMap |
| 改 | `src/main/resources/mapper/EmpMapper.xml` | 加 `<resultMap>` ＋ 3 个新 `<select>` |
| 改 | `src/main/java/com/groupmeal/dao/EmpDao.java` | 加 3 个方法声明 |
| 新建 | `src/test/java/com/groupmeal/StringApiTest.java` | Step 1 的对比实验（用完可删） |
| 新建 | `src/test/java/com/groupmeal/ResultMapTest.java` | Step 3–6 的验收 |
| 改（临时） | `src/main/resources/mybatis-config.xml` | Step 5 加 `<setting name="autoMappingBehavior" value="NONE"/>` 做实验，**做完必须改回来** |

> 「标注」`EmpVo` 放在新包 `com.groupmeal.vo` 而不是 `entity`，是有意的：01 册的 `typeAliases` 只扫了 `com.groupmeal.entity`，所以 VO **拿不到别名**，你会被迫写全类名 —— 这正是 Step 3 要顺手讲清的一件事。

---

## 1. Step 1 · 两种调用方式对比（PDF p11 第三步，15 min）

### 1.1 先做"原始"那种：字符串调用

MyBatis 的 `SqlSession` 本身就有一批按 **`namespace.id` 字符串** 调用的方法。写一个测试类感受一下：

`src/test/java/com/groupmeal/StringApiTest.java`：

```java
package com.groupmeal;

import com.groupmeal.entity.Emp;
import com.groupmeal.util.MyBatisUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

class StringApiTest {

    private SqlSession session;

    @BeforeEach
    // 2026-10-15 修订：读操作不传参（getSession()）。原稿写 getSession(true) 是"读写都传 true"的坏习惯残留，
    // 口径见《01回顾》§6：读不需要提交，写才用 getSession(true)。
    void setUp() { session = MyBatisUtil.getSession(); }

    @AfterEach
    void tearDown() { MyBatisUtil.close(session); }

    @Test
    void 字符串方式查全部() {
        // ⚠️ 参数是"全类名.方法名"这个字符串，编译器不检查
        List<Emp> list = session.selectList("com.groupmeal.dao.EmpDao.queryAll");
        list.forEach(System.out::println);
    }

    @Test
    void 字符串方式故意写错一次() {
        // 把 queryAll 写成 queryall —— 编译器不报错，运行时炸
        List<Emp> list = session.selectList("com.groupmeal.dao.EmpDao.queryall");
        System.out.println(list);
    }
}
```

**验收**：第一个方法打印数据；**第二个方法抛异常**，控制台红字：

```
org.apache.ibatis.binding.BindingException:
Invalid bound statement (not found): com.groupmeal.dao.EmpDao.queryall
```

### 1.2 换成代理方式（01 册的写法）

```java
EmpDao dao = session.getMapper(EmpDao.class);
List<Emp> list = dao.queryAll();     // 写错方法名 → IDEA 直接飘红，编译都过不去
```

### 1.3 三个缺点，一句话各一条（这就是「纪要」9.12_1 说"代理接口优于直接调 SqlSession API"的原因）

| | 字符串方式 | Mapper 代理方式 |
|---|---|---|
| 出错时机 | **运行时**才炸（上面那个异常） | **编译期**就飘红 |
| 可维护性 | `namespace.id` 散落在 Service 各处，改方法名要全文搜字符串 | 改接口方法名，IDEA 一键重构，调用点自动跟着改 |
| 分层 | 业务代码里出现 SQL 标识符，DAO 抽象被穿透 | 业务只依赖接口，和 `new EmpDaoImpl()` 一样自然 |

> 「标注」PDF p11 的 `EmpService` 示例里那句注释 *"动态代理 动态根据接口实现生成对应的实现类的代理对象"* 是全套 MyBatis 最关键的一句话。**没有实现类**：`getMapper()` 用 JDK 的 `Proxy` 现造一个实现，方法被调用时，代理拿"接口全类名 + 方法名"去配置里找对应的 `<select>/<insert>` 执行。你想验证它真的没有实现类：`Ctrl+点击 getMapper` 进到 `DefaultSqlSession`，一路看下去只会看到 `MapperProxy`。
> 「标注」PDF 里的接口叫 `IEmpDao`（加 `I` 前缀是老派写法）。**本系列沿用 01 册的 `EmpDao`**，Java/Spring 社区主流不加 `I`；跟老师演示时不必纠结，口头能说出"接口 + 动态代理"即可。

---

## 2. Step 2 · namespace 与 id 的边界（PDF p11「注意」三条，10 min）

PDF 原文三条，逐条动手验，验完才记得住：

1. **`namespace` 必须是接口的完整限定名** → 01 册已定：`com.groupmeal.dao.EmpDao`。写错就是 Step 1 那个 `Invalid bound statement`。
2. **SQL 标记元素的 `id` 必须和接口中要实现的方法名一致** → 绑定靠的是"namespace + id"两个字符串拼起来等于"接口全类名 + 方法名"。
3. **⚠️ 标记元素的 id 不需要和接口全部一致**：
   - 接口里可以有方法，**XML 里不写对应 SQL**（此时调用它才炸，不调用完全没事）；
   - XML 里可以有**额外的、接口没有对应方法的 SQL 标记**（这条最容易被忽略）。

动手验第 3 条的第二种情况 —— 在 `EmpMapper.xml` 里加一段"孤儿 SQL"：

```xml
<!-- 接口里没有叫 querySalaryGt 的方法，但 MyBatis 照样注册它 -->
<select id="querySalaryGt" resultType="Emp">
    select empno, ename, salary, deptno, hiredate from emp where salary &gt; #{minSal}
</select>
```

然后在测试里用**字符串方式**调它：

```java
@Test
void 调一个接口里没有的SQL() {
    List<Emp> list = session.selectList("com.groupmeal.dao.EmpDao.querySalaryGt", 5000);
    list.forEach(System.out::println);
}
```

**验收**：能查出来。→ 结论：**XML 是 SQL 的注册表，接口只是它的一个"类型安全的入口"**；两者靠字符串对齐，不是一对一强绑定。
（注意 `&gt;` —— XML 里裸写 `>` 在部分位置会被解析器吃掉，SQL 里的比较运算符推荐写 `&gt;` / `&lt;`，或用 `<![CDATA[ ... ]]>`，第 03 册动态 SQL 会大量用到。）

> 「标注」"孤儿 SQL"在生产里有个正当用法：**同一条 SQL 被多个不同签名的方法复用**，或者给报表/临时查询留口子。但更多时候它是历史垃圾，评审时应清掉。

---

## 3. Step 3 · resultMap 普通属性映射（PDF p11–12 主线，20 min）

### 3.1 什么时候**必须**用 resultMap

PDF 原话：**"当查询的 SQL 中，出现了表字段名和实体类属性名不一致情况时，需要指定映射关系。"**

01 册的表字段和属性名恰好一致（`empno/ename/salary/deptno/hiredate`），所以 `resultType="Emp"` 一直够用 —— 那是运气好。真实项目里字段名一定不一致（数据库叫 `sal`、Java 叫 `salary`；数据库叫 `create_time`、Java 叫 `createTime`）。**所以本册故意造一个全不一致的实体。**

新建 `com.groupmeal.vo.EmpVo`：

```java
package com.groupmeal.vo;

import lombok.Data;
import java.util.Date;

/**
 * 故意把属性名取得和表字段完全不一样，用来练 resultMap。
 * 真实项目里这就是"给前端看的 VO"：字段名按前端习惯取，不跟数据库妥协。
 */
@Data
public class EmpVo {
    private Integer empNo;      // ← empno
    private String  empName;    // ← ename
    private Float   money;      // ← salary
    private Date    hDate;      // ← hiredate
    private Integer deptNo;     // ← deptno
}
```

### 3.2 三步走（PDF p12 主线：「第一步编写结果映射 → 第二步 select 中引用 → 第三步代码查询验证」）

**第一步**：在 `EmpMapper.xml` 里写映射（放在 `<mapper>` 开头、`<select>` 之前都可以，DTD 对 `resultMap` 的位置宽松）

```xml
<!-- type 这里必须写全类名：com.groupmeal.vo 不在 typeAliases 的扫描包里，拿不到别名 -->
<resultMap id="empVoMap" type="com.groupmeal.vo.EmpVo">
    <!-- 主键用 id，普通列用 result；property=Java属性，column=SQL查出来的列 -->
    <id     property="empNo"   column="empno"/>
    <result property="empName" column="ename"/>
    <result property="money"   column="salary"/>
    <result property="hDate"   column="hiredate"/>
    <result property="deptNo"  column="deptno"/>
</resultMap>
```

**第二步**：查询里引用 —— **`resultType` 和 `resultMap` 二者只能选一个**（PDF p12 注释原话："使用结果映射，就是把 resultType 换为 resultMap，注意两者不能同时使用"）

```xml
<select id="queryAllVo" resultMap="empVoMap">
    select empno, ename, salary, hiredate, deptno from emp
</select>
```

**第三步**：接口加方法声明 ＋ 测试验证

```java
// EmpDao.java 里加
List<EmpVo> queryAllVo();
```

```java
// src/test/java/com/groupmeal/ResultMapTest.java
package com.groupmeal;

import com.groupmeal.dao.EmpDao;
import com.groupmeal.util.MyBatisUtil;
import com.groupmeal.vo.EmpVo;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.*;

import java.util.List;

class ResultMapTest {

    private SqlSession session;
    private EmpDao dao;

    @BeforeEach
    void setUp() {
        // 2026-10-15 修订：本测试类全是查询，读不传参。写操作才用 getSession(true)。
        session = MyBatisUtil.getSession();
        dao = session.getMapper(EmpDao.class);
    }

    @AfterEach
    void tearDown() { MyBatisUtil.close(session); }

    @Test
    void 不一致字段也能映射对() {
        List<EmpVo> list = dao.queryAllVo();
        list.forEach(System.out::println);
        EmpVo first = list.get(0);
        Assertions.assertNotNull(first.getEmpName(), "ename 应该映射到 empName");
        Assertions.assertNotNull(first.getMoney(),   "salary 应该映射到 money");
        Assertions.assertNotNull(first.getHDate(),   "hiredate 应该映射到 hDate");
    }
}
```

**验收**：`ResultMapTest` 绿；控制台打印出的 `EmpVo` 五个属性**全有值**（toString 里能看到 `empNo=..., empName=..., money=...`）。

> 「标注」PDF p12 那句注释 *"如果属性名和字段名一致时，可以省略的"* 是对的：`<result property="deptNo" column="deptno"/>` 严格说不能省（它也不一致），但**真正一致的列不用出现在 resultMap 里**，MyBatis 会自动补 —— 前提是自动映射没被关掉，这正是 Step 5 的实验。

---

## 4. Step 4 · 另一种解法：SQL 里用 AS 别名（PDF p12「常见方式」第一条，10 min）

不写 resultMap，改在 SQL 里把列名"变成"属性名：

```xml
<select id="queryAllVo2" resultType="com.groupmeal.vo.EmpVo">
    select empno   as empNo,
           ename   as empName,
           salary  as money,
           hiredate as hDate,
           deptno  as deptNo
    from emp
</select>
```

接口加 `List<EmpVo> queryAllVo2();`，测试里照抄 `不一致字段也能映射对()` 改调 `queryAllVo2()`，**应该同样绿**。

**两种解法怎么选**（面试/答辩都问得到）：

| | AS 别名 | resultMap |
|---|---|---|
| 写起来 | 快，改一行 SQL 就行 | 要维护一段 XML |
| 复用 | **每个 select 都要重抄一遍别名**，5 个查询 = 抄 5 遍 | 定义一次，N 个 select `resultMap="empVoMap"` 引用 |
| 复杂场景 | 做不了嵌套对象、做不了延迟加载 | 关联映射（04 册）、`<collection>`/`<association>` 只有它能干 |
| 适用 | 临时查询、单表、字段少 | 项目正式用法 |

→ **结论：字段不一致 ≤2 个用 AS，多的或要复用的用 resultMap。**

---

## 5. Step 5 · autoMapping / autoMappingBehavior：把坑亲手踩一遍（PDF p12，15 min）

PDF p12 用了一整页讲这件事，说明它是**真实项目高频事故点**。本册最重要的一步，别跳过实验。

### 5.1 两个概念先分清（都照 PDF 原话）

| 名字 | 属于哪 | 作用 | 默认 |
|---|---|---|---|
| `autoMapping` | **映射文件**里的 `<resultMap>` 属性 | 只对**这一个** resultMap 生效；开了它，resultMap 里没写的列也会被自动映射 | `true` |
| `autoMappingBehavior` | **配置文件** `<settings>` 里的全局项 | 对**所有**查询生效；`NONE` 关 / `PARTIAL` 只自动映射非嵌套字段 / `FULL` 连嵌套也自动映射 | `PARTIAL` |

> 「标注」PDF 那句 **"此属性会覆盖全局的属性 autoMappingBehavior"** 是关键：resultMap 上的 `autoMapping="true"` 优先级高于全局的 `NONE`。

### 5.2 实验 A：全局关掉自动映射，看 resultType 查询变 null

在 `mybatis-config.xml` 的 `<settings>` 里加一行（**加完记得 Step 5.4 删掉**）：

```xml
<settings>
    <setting name="logImpl" value="STDOUT_LOGGING"/>
    <setting name="autoMappingBehavior" value="NONE"/>   <!-- ← 实验用 -->
</settings>
```

再写一个测试，故意用**没有 resultMap** 的查询去查不一致字段之外的东西：

```java
@Test
void NONE下resultType查询的属性会是null() {
    // queryByEmpno 用的是 resultType="Emp"，没有 resultMap
    Emp emp = dao.queryByEmpno(1);
    System.out.println("实验A结果：" + emp);
}
```

**你应该看到**（这一步是本册的核心认知）：

- 对象**不是** null，但**属性全是 null**（`Emp(empno=null, ename=null, ...)`）。

> 「标注」PDF p12 的原话是"对应没有使用结果映射，而使用 resultType 的查询，**会查询结果直接为 null**"。这句话在 3.5.19 上表现得更精确：**行对象会被 new 出来，但因为没有任何列被映射，所有属性都是 null**（`select count(*)` 那种单值结果同理取不到）。所以别背"结果为 null"这句话，要背**"NONE = 只有你在 resultMap 里显式写过的列才会被赋值"**。
> 「标注」更要紧的一条：PDF 提醒 *"会对其他没有使用结果映射的查询产生影响"* —— 你只是想给某一个查询做精确映射，结果把**全工程所有 `resultType` 查询**都打成了空对象。这就是为什么没人把 `autoMappingBehavior` 设成 `NONE`。

### 5.3 实验 B：单个 resultMap 上开回来

把 `<resultMap id="empVoMap" ...>` 改成带 `autoMapping="true"`，其余不动，`不一致字段也能映射对()` 仍然绿 —— 说明 resultMap 级别的开关能覆盖全局（把 5.2 的 `NONE` 留着再验一次更明显：全局 NONE ＋ 局部 `autoMapping="true"` → 这个 resultMap 的查询正常，其他 resultType 查询照样一片 null）。

### 5.4 **实验做完，把 `autoMappingBehavior` 那一行删掉**

删完重跑 `EmpDaoTest`（01 册那 5 个）＋ 本册 `ResultMapTest`，**全绿才算本步结束**。不删的话第 03、04 册会全线莫名其妙查不出数据，你会误以为是动态 SQL 或关联映射的问题。

---

## 6. Step 6 · 真实项目的主流做法：下划线 → 驼峰（PDF 没讲，就业必会，15 min）

前面全是"故意造的不一致"。现实中的不一致只有一种主流形态：**数据库下划线 ＋ Java 驼峰**（`create_time` / `order_no` / `user_id`）。业界不会为此写 resultMap，而是打开一个全局开关：

```xml
<settings>
    <setting name="logImpl" value="STDOUT_LOGGING"/>
    <setting name="mapUnderscoreToCamelCase" value="true"/>   <!-- 下划线自动转驼峰 -->
</settings>
```

打开后，`select empno, ename, salary, hiredate, deptno from emp` 查出的列能自动落到 `empNo / ename / salary / hireDate / deptNo` 上。

**动手验**：把 3.2 的 `empVoMap` 那段 resultMap **整段注释掉**（或新写一个 `queryAllVo3` 用 `resultType="com.groupmeal.vo.EmpVo"` ＋ SQL 里 `select empno as emp_no, ename as emp_name, salary as money, hiredate as h_date ...`），打开开关后属性照样有值 —— 你会立刻明白"自动映射"到底在自动什么。

> 「标注」为什么这一节 PDF 没讲但你必须会：① Spring Boot 里 `mybatis.configuration.map-underscore-to-camel-case=true` 是**几乎所有教程和脚手架的默认配置**；② 《JSP 网站开发项目化教程》第 5 个项目往后做订餐表（订单号、下单时间）时，你的字段一定是下划线的；③ 你未来 `campus-groupmeal` 的建表规范就按下划线来，Java 侧全驼峰，靠这个开关桥接。
> 「标注」三种解法的优先级（我给你的定例）：**能用同名最好 → 下划线/驼峰差异用 `mapUnderscoreToCamelCase` → 剩下的真不一致才写 resultMap**。别一上来就给每个查询配 resultMap，那是维护负担。

---

## 6.5 多参数传参：@Param 与 Map（2026-10-15 补，就业必会，15 min）

前面所有方法都是**0 个或 1 个参数**。真实项目里最常见的是**两个以上参数**，例如"按部门＋最低工资查人"：

```java
List<Emp> queryByDeptAndSalary(Integer deptno, Double minSalary);
```

**直接这么写会炸**。报错长这样（背下来，一眼认出）：

```
org.apache.ibatis.binding.BindingException:
Parameter 'deptno' not found.
Available parameters are [arg1, arg0, param1, param2]
```

**为什么**：单个参数时 MyBatis 直接把那一个值给你用；**多个参数时它不知道哪个名字对应哪个值**，只能按位置给 `arg0/arg1`（或 `param1/param2`）。XML 里写 `#{deptno}` 它找不到这个名字 → 炸。

**解法一（主流，必会）：`@Param` 起名字**

```java
List<Emp> queryByDeptAndSalary(@Param("deptno") Integer deptno,
                               @Param("minSalary") Double minSalary);
```

```xml
<select id="queryByDeptAndSalary" resultType="Emp">
    select empno, ename, salary, deptno, hiredate
    from emp
    where deptno = #{deptno} and salary >= #{minSalary}
</select>
```

`@Param("deptno")` 就是告诉 MyBatis"第一个参数叫 deptno"，XML 里的 `#{deptno}` 就对上了。**规则：参数 ≥2 个，每个都加 @Param。**

**解法二：传一个 Map**

```java
List<Emp> queryByMap(Map<String, Object> map);
```

XML 里 `#{deptno}` 会去 Map 里取 key="deptno" 的值。适合"参数个数不固定"的查询（动态条件多的搜索页）。缺点：键是字符串、编译器不检查，写错运行时才炸。

**解法三：传一个对象（参数本来就是一个实体时）**

如果几个参数其实属于同一个东西（就是 Emp 的 deptno 和 salary），直接传 `Emp`，XML 里 `#{deptno}` 会调 `getDeptno()`。**01 册的 saveEmp/updateEmp 就是这种。**

**三种怎么选（定例）**：参数是"一个实体的字段"→ 传对象；参数是"几个独立的查询条件"→ @Param；参数"个数不定/来自前端表单"→ Map。

**动手验（10min）**：在 `EmpDao` 加 `queryByDeptAndSalary`（带 @Param）＋对应 XML，测试里调一次 `queryByDeptAndSalary(10, 5000.0)` 打印结果。**再故意把 @Param 删掉跑一次**，亲眼看到上面那个 `Available parameters are [arg1, arg0...]` 报错——看过一次就永远记得。

---

## 7. 本册小结（背这 5 句，答辩/面试都够用）

1. Mapper 接口没有实现类，`getMapper()` 用 JDK 动态代理现场生成，靠 **namespace（接口全类名）＋ id（方法名）** 找到 SQL。
2. XML 里的 `id` 不必和接口一一对应：多出来的 SQL 标记能被字符串方式调用，接口里没 SQL 的方法一调就炸。
3. `resultType` 和 `resultMap` 二选一；字段不一致时用 AS（轻）或 resultMap（可复用、能做嵌套）。
4. `autoMappingBehavior` 是全局开关（默认 PARTIAL），设 `NONE` 会把所有 `resultType` 查询打成空属性对象 —— **不要动它**；单个 resultMap 可用 `autoMapping` 覆盖。
5. 真实项目靠 `mapUnderscoreToCamelCase=true` 桥接"数据库下划线 / Java 驼峰"，而不是给每个查询写 resultMap。

---

## 8. 常见报错急救表

| 现象 | 真正原因 | 怎么办 |
|---|---|---|
| `Invalid bound statement (not found)` | namespace 写错 / id 和方法名不一致 / XML 没被加载 | 三查：①namespace 是否等于接口全类名 ②id ③`mybatis-config.xml` 里 `<mapper resource="mapper/EmpMapper.xml"/>` 有没有这一行 |
| 接口有方法、XML 没 SQL，调用就炸 | 同上（少注册了 id） | 补 `<select id="方法名">`，或把方法删掉 |
| resultMap 生效不了、属性还是 null | 用了 `resultType` 又写 resultMap / 两者同时写 | 只能留一个；引用名要和 `id` 完全一致 |
| `<setting name="autoMappingBehavior" value="NONE"/>` 之后**全项目**查不出东西 | 就是这一行 | 删掉它（Step 5.4），或给受影响的查询都配 resultMap |
| `Type ... not found` （typeAliases 里的别名找不到） | 类不在扫描包内 | 写全类名，或在 `<typeAliases>` 里加 `<package name="com.groupmeal.vo"/>` |
| XML 里 `>` 让文件飘红/解析报错 | XML 把 `<` 当标签起始 | 改 `&gt;`/`&lt;`，或整段包 `<![CDATA[ ... ]]>`（03 册细讲） |
| VO 打印出来属性是 null 但 Emp 正常 | 那个查询没走 resultMap | 给 VO 的查询补 `resultMap="empVoMap"` |
| 开了 `mapUnderscoreToCamelCase` 后个别列仍映射不上 | 列名转驼峰的结果与属性名不完全相同（如列 `dept_no2` vs 属性 `deptNo02`），或 `Boolean is_xxx` 这类特殊命名 | 单独给这几列写 `as` 别名，或在 resultMap 里补 `<result>`；**不要为个别列关掉全局开关** |

---

## 9. 迁移到 `campus-groupmeal` 时，本册的东西怎么落地（a 方案的伏笔）

01 册的 `Emp`＝**数据库实体（DO）**，本册的 `EmpVo`＝**给上层/前端看的视图对象（VO）**。将来订餐项目里这条链是固定的：

```
表 group_order  →  GroupOrderDO（字段跟数据库，一一对应）
                →  GroupOrderVO（字段跟前端的 json，名字按前端习惯）
```

**为什么就业项目要这么分**（也是你现在就把 `vo` 包建出来的理由）：数据库字段会改（加 `is_deleted`、金额换 `decimal` 变 `BigDecimal`），但不该逼着前端跟着改 json 字段名；反过来前端要 `orderStatusText` 这种展示字段，也不该往表里塞。中间这层映射在纯 MyBatis 里就是 **resultMap**，在 MyBatis-Plus/MapStruct 项目里是转换器 —— **本质完全一样**，你现在练的是原理。

---

## 10. 自检 5 问（能答上来才进 03 册；答不上来就回本册对应 Step）

1. `getMapper()` 拿到的对象，它的方法体在哪里？（→ Step 1.3 标注：动态代理，`MapperProxy`）
2. 为什么 `Invalid bound statement` 大多不是"SQL 写错"而是"没绑上"？（→ Step 2）
3. resultMap 和 AS 别名，什么场景必须用前者？（→ Step 4 表格：要复用、要嵌套对象/关联映射）
4. 全局 `autoMappingBehavior=NONE` 会发生什么现象？（→ Step 5.2：对象 new 出来了但属性全 null）
5. 表字段 `order_time`、Java 属性 `orderTime`，最省事的解法是什么？（→ Step 6：`mapUnderscoreToCamelCase=true`）

**参考答案（2026-10-15 补）**：
1. 方法体**不存在**。`getMapper()` 返回 JDK 动态代理对象（`MapperProxy`），调用时代理按 namespace+id 去 Configuration 取 SQL 执行。
2. 因为报错发生在"找 SQL"那一步（绑定），还没轮到执行 SQL。namespace/id/注册任一不对就报这个，和 SQL 内容对错无关。
3. 需要**复用**（多个查询共用一套映射）或**嵌套对象/关联映射**（一对多/多对一，04 册）时必须用 resultMap；AS 别名只能解决一次性、扁平的改名。
4. 所有 `resultType` 查询返回的对象**属性全为 null**（对象 new 出来了但没填值），因为全局自动映射被关。
5. 开 `mapUnderscoreToCamelCase=true`，不用写 resultMap 也不用 AS。

---

## 11. Microsoft ToDo 粘贴块（倒序，做完一条删一条）

```
跑 ResultMapTest 五个属性都有值 = 本册完成
Step6 开 mapUnderscoreToCamelCase 验一次
Step5 实验做完：删掉 autoMappingBehavior 那行，全部测试重跑变绿
Step5 复现 NONE 导致属性全 null
Step4 AS 别名第二种解法跑通
Step3 建 EmpVo + resultMap + 三处引用，测试绿
Step2 验"孤儿 SQL"能被字符串调到
Step1 两个字符串方式测试，看到 Invalid bound statement
（开工前先复制一份工程到 D:\Java\Projects\backup_20260919\，Git 还没建仓）
```

**降级线**：
- 只有 45 min → 做 **Step 3 ＋ Step 4 ＋ Step 6**（映射的三种解法），Step 1/2/5 只读不敲。
- 只有 25 min（＝L0 的主产出推进）→ **只读 §3 和 §7 五句小结**，把 resultMap 抄进工程跑绿一次。
- 卡在 Step 5 现象和文档不一致 → **截图发我**，这是本册最容易因版本而表现不同的一步。

---

*生成 2026-09-18 20:35 ｜ 上一册：01 环境与第一个 CRUD ｜ 下一册：03 动态 SQL 全标签（`<if>/<where>/<set>/<foreach>/<trim>`，实体类必须用包装类那笔账在这册结清）*

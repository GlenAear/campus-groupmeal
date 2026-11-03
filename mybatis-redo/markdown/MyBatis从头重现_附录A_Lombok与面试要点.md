# MyBatis 从头重现 · 附录 A ｜ Lombok 讲透 ＋ 这条链上的面试点

> **定位**：不是新知识点册，是**回顾＋被抽问用**。两件事：① 把 Lombok 从"我记得它能生成东西"讲到"我能反编译给你看"；② 把 01/02 册这条链上**面试真会问**的点逐条落到本工程的文件上，每条都给「一句口述版」。
> **前置**：第 01 册（工程能跑、5 条 CRUD 绿）、第 02 册（resultMap、孤儿 SQL、`@Param`）。
> **触发原因**：你 2026-11-03 的日志原话——*"Lombok 的作用遗忘，可以直接生成对象但是不理解，再向我讲解一遍"*。
> **实测口径**：本附录标「实测」的，都是 2026-11-04 在你这台机器（JDK 17.0.20、MySQL 8.4.6、`mydb`）真跑出来的输出；没跑的我一律写「未实测」。

> ⚠️ **菜单名怎么对上**：一律「English（中文）」双标。对不上就 **双击 `Shift` → 切 `Actions` → 直接打英文名**，或看 `D:\我的\复盘\相关信息\IDEA菜单中英对照表.md`。

---

## 第一部分 · Lombok：从"能用"到"能讲"

### 1.1 一句话结论 ＋ `@Data` 到底生成了什么

**结论**：`@Data` = 在**编译期**把 getter／setter／`toString`／`equals`／`hashCode`／一个构造器**写进 .class**，源码里一个字都不多。

用你自己的 `EmpVo` 当例子。下面是现有文件（**不是新建**，路径 `D:\Java\Projects\campus-groupmeal\mybatis-redo\src\main\java\com\groupmeal\vo\EmpVo.java`）的实质内容（省略 `package` 与 `import lombok.Data` / `java.util.Date` 三行，字段一字不差）：

```java
@Data
public class EmpVo {
    private Integer empNo;      // 对应列 empno
    private String  empName;    // 对应列 ename
    private Float   money;      // 对应列 salary
    private Date    hDate;      // 对应列 hiredate
    private Integer deptNo;     // 对应列 deptno
}
```

源码里只有 5 个字段。编译后 `EmpVo.class` 里**真的有**这些方法（下面是 `javap -p target\classes\com\groupmeal\vo\EmpVo.class` 输出里的方法部分，字段声明与上面源码一致，2026-11-04 实测）：

```
public class com.groupmeal.vo.EmpVo {
  public com.groupmeal.vo.EmpVo();
  public java.lang.Integer getEmpNo();
  public java.lang.String getEmpName();
  public java.lang.Float getMoney();
  public java.util.Date getHDate();
  public java.lang.Integer getDeptNo();
  public void setEmpNo(java.lang.Integer);
  public void setEmpName(java.lang.String);
  public void setMoney(java.lang.Float);
  public void setHDate(java.util.Date);
  public void setDeptNo(java.lang.Integer);
  public boolean equals(java.lang.Object);
  protected boolean canEqual(java.lang.Object);
  public int hashCode();
  public java.lang.String toString();
}
```

`@Data` 六件套对照（`EmpVo` 全中）：

| # | 生成的东西 | 在 `EmpVo` 上的具体样子 | 谁在用 |
|---|---|---|---|
| 1 | 每个字段的 getter | `getEmpNo()` / `getMoney()` … | JSON 序列化、JSP EL、`System.out` 之外的所有读取 |
| 2 | 每个字段的 setter | `setMoney(java.lang.Float)` | **MyBatis 填值就是走它**（见 §1.4） |
| 3 | `toString()` | 打印出 `EmpVo(empNo=1, empName=张三, money=9500.0, hDate=…, deptNo=1)` | 你所有 `list.forEach(System.out::println)` 能看懂，全靠它 |
| 4 | `equals()` + `canEqual()` | 五个字段逐个比 | `Assertions.assertEquals(vo1, vo2)`、去重 |
| 5 | `hashCode()` | 同样五个字段参与 | 放进 `HashSet`／`HashMap` 的 key |
| 6 | 构造器（`@RequiredArgsConstructor` 形态） | 因为**没有 final、没有 `@NonNull` 字段**，退化成 `public EmpVo()` **无参构造** | MyBatis／JSP／Spring 反射 new 对象 |

> 「标注」第 6 条是他最容易讲错的点。对照 `Emp.java`（**现有文件**，路径 `src\main\java\com\groupmeal\entity\Emp.java`）：它显式写了 `@NoArgsConstructor` ＋ `@AllArgsConstructor`，`javap -p target\classes\com\groupmeal\entity\Emp.class` 就能看到**两个**构造器：`Emp()` 和 `Emp(Integer, String, Float, Integer, Date)`。**"单靠 @Data 有没有全参构造"——没有**，`@Data` 只给"必填字段"那个（这里等价于无参）。

### 1.2 机制：它是编译期注解处理器，不是运行时魔法

**结论**：Lombok 挂在 `javac` 的 **Annotation Processor（注解处理器）** 扩展点上，在编译那一步改 AST，所以：**源码没有 → .class 有 → 运行时不需要 lombok 的 jar**。

三条推论，都是能被追问的：

| 现象 | 解释 |
|---|---|
| 源码里 `getEmpNo()` 飘红"找不到符号" | IDEA 自己也在编译**之前**读源码，看不到方法 → 需要 **Lombok 插件**（IDEA 2024.1 起自带，无需手动装；你如果是更早版本就要装。未实测你的 IDEA 版本） |
| 插件装了还飘红 | 注解处理没开：`Settings（设置）→ Build, Execution, Deployment（构建、执行、部署）→ Compiler（编译器）→ Annotation Processors（注解处理器）→ 勾 Enable annotation processing（启用注解处理）` |
| pom 里 `<scope>provided</scope>` | 编译期要它跑处理器，**运行时不需要**，所以不打进产物。和 `servlet-api` 用 `provided` 是同一个道理：容器（Tomcat）已经带了，你打进 war 就撞版本 |

`pom.xml`（**现有文件**，工程根 `D:\Java\Projects\campus-groupmeal\mybatis-redo\pom.xml`）里那段就是 `<dependency>` ＋ `org.projectlombok:lombok:1.18.42` ＋ `<scope>provided</scope>`，和 servlet-api 用 `provided` 是同一回事。

> 「标注」本工程**版本口径**（面试会被问"你们用什么版本"）：mybatis **3.5.19**、mysql-connector-j **8.0.33**（MySQL 8 的新坐标 `com.mysql:mysql-connector-j`，不再是 `mysql:mysql-connector-java`）、lombok **1.18.42**（provided）、junit-jupiter **5.9.2**（test）；`maven.compiler.source/target = 17`，`project.build.sourceEncoding = UTF-8`。

### 1.3 怎么亲手验（这是他最认的方式：反编译）

**第一步，先编译**（`target/classes` 可能是旧的或空的）：在工程根目录 `D:\Java\Projects\campus-groupmeal\mybatis-redo` 执行 `D:\Java\Maven\apache-maven-3.9.0\bin\mvn -o -B compile`（`mvn` 不在 PATH，必须写绝对路径；`-o` 离线、`-B` 批处理）。实测输出结尾：`Compiling 5 source files to D:\Java\Projects\campus-groupmeal\mybatis-redo\target\classes` → `BUILD SUCCESS`，Total time 5.280 s。

**第二步，javap 反编译看方法**（第一步做完再做这步）：

```
cmd //c "javap -p target\classes\com\groupmeal\vo\EmpVo.class"
```

⚠️ Git Bash 里必须用 `cmd //c "..."` 包一层——否则反斜杠和 `;` 会被 MSYS 的路径转换改坏，命令看着像在跑其实参数已经错了。想看 `Emp` 的双构造器就把路径换成 `...entity\Emp.class`。

**第三步（可选，最有说服力的一步）**：把 `EmpVo.java` 上的 `@Data` 那行注释掉再跑一次第一步——`BUILD FAILURE`，报错是 `找不到符号: 方法 getEmpNo()`，位置在 `ResultMapTest.java`。**这一步做完记得把注释取消回去。**（未实测取消后再编译的次数，但方向确定：编译期报错，不影响运行时。）

> 「标注」`javap` 属于 JDK，`java -version` = `openjdk 17.0.20`（实测），`javap -version` 同为 17.0.20，所以不用额外装任何东西。`mvn -o -B test` **已实测**（2026-11-04 01:10，Qoder 跑的）：`Tests run: 9, Failures: 0, Errors: 0, Skipped: 0` ＋ `BUILD SUCCESS` —— 前提是 MySQL84 服务起着、`mydb` 有 01 册那 4 行种子（现在有 `sql/mydb.sql` 能一键重建）。

### 1.4 为什么 MyBatis / JSP / Spring 全都吊在 setter 和构造器上

**结论**：框架拿到的是一张 `ResultSet` 和**一个 Class 对象**，它只能做两件事——`Class.newInstance()` 造对象，再按列名找 `setXxx(...)` 调进去。这两样都是 Lombok 生成的。

| 场景 | 依赖的是 | 缺了会怎样 |
|---|---|---|
| MyBatis `resultType="Emp"` 填值 | 无参构造 ＋ setter | 反射 new 不出来（报找不到可用构造器那一类），或对象 new 出来了属性全 null |
| MyBatis `resultMap` 的 `<result property="money" column="salary"/>` | `setMoney(Float)`（property 名 → `set` ＋ 首字母大写） | 找不到 setter → 该属性不填，**不报错**（这才是"属性莫名是 null"的常见成因） |
| `<jsp:useBean id="u" class="com.groupmeal.bean.User"/>` | **必须有 public 无参构造**（JavaBean 规范硬性要求） | JSP 翻译期直接报错 |
| Spring 的 `@Autowired` 字段注入／`@ConfigurationProperties`；Jackson 转 JSON | setter／getter（＋ 无参构造） | 属性绑不上；前端拿到的 json 是空的 |

> 「标注」把他的两门课串起来了：**JSP 那门课要求的"JavaBean"和 MyBatis 要求的"能反射填值的对象"是同一条规范**——无参构造 ＋ `private 字段` ＋ `public getter/setter`。Lombok 只是把第三样自动补上。**注意：一旦你手写任何一个构造器，编译器就不再送你无参构造**——这正是 `Emp.java` 要显式写 `@NoArgsConstructor` 的原因，也是 `EmpVo.java` 什么都不写却有 `EmpVo()` 的原因（它没有别的构造器）。

### 1.5 Lombok 的代价（面试"你怎么看 Lombok"必答，至少两条）

| 代价 | 具体表现 | 应对 |
|---|---|---|
| 继承时 `equals/hashCode` 漏父类字段 | 子类上直接报 warning `Generating equals/hashCode implementation but without a call to superclass` | 显式写 `@EqualsAndHashCode(callSuper = true)` |
| ** setter 全公开 → 对象无法不可变** | 任何拿到 VO 的代码都能 `setMoney(0f)`，字段随便被改 | 需要不可变时用 `@Value`（lombok 的，**不是 JDK 的**；它把字段变 `private final` ＋ 不给 setter ＋ 生成全参构造）。JDK 里做不可变得自己写 final ＋ 无 setter |
| "看不见的方法"增加排错成本 | 单步调试找不到 setter 调用点；重构工具搜不到源码里没有的 `setMoney` | 出问题先 `javap` 看 .class 里到底有什么；必要时用 IDEA 的 `Lombok（罗索克）→ Expand（展开）` 或直接手写一遍 |
| 团队环境成本 ＋ 版本耦合 | 每个 IDE 都要配：IDEA 装 Lombok plugin（新版自带），Eclipse 要跑 `lombok.jar` 安装器改 `eclipse.ini`；有的公司规范**明令禁用**。Lombok 靠"黑"进 `javac` 内部 API，JDK 大版本升级常要跟着升 Lombok | 入职前问一句"你们用不用 Lombok"；JDK 升级后报 `NoSuchFieldError` 之类先怀疑 Lombok 版本（本工程 1.18.42 ＋ JDK 17 实测没事） |

> 「标注」一句能背的表态：**"我用 Lombok 是因为 entity/VO 这类贫血对象里手写 getter/setter 是纯噪音；但我知道代价是排错时方法不在源码里，所以我保留了 `javap` 验证的习惯。"** 这比"方便"两个字值钱得多。

### 1.6 注解速查表（每行一个"什么时候用"）

| 注解 | 什么时候用 | 本工程实例 |
|---|---|---|
| `@Data` | entity／VO／DTO 这种纯数据容器 | `Emp`、`EmpVo` |
| `@Getter` / `@Setter` | 只想要其中一半，或想给个别字段单独放开（如只读字段不写 setter） | 未用 |
| `@ToString` | 只要打印能力；`@ToString.Exclude` 用来**排除密码字段** | 未用（`Conn` 那边的教训：`selAllNoPwd()` 干脆不 select passwd） |
| `@EqualsAndHashCode` | 需要继承语义时必须 `callSuper = true` | 未用（无继承） |
| `@NoArgsConstructor` / `@AllArgsConstructor` | 前者：**写了别的构造器之后，为了框架反射必须补回无参**；后者：测试里一行 new 出完整对象 | `Emp` 两个都用了（`EmpDaoTest` 实际走的是 setter） |
| `@Builder` | 字段 ≥5 个、调用方要"挑着填"时 | 未用 |
| `@Slf4j` | 打日志不想写 `Logger log = LoggerFactory.getLogger(X.class)` | 未用（本工程日志是 MyBatis 的 `STDOUT_LOGGING`，直接喷 System.out） |
| `@Value`（lombok） | 要不可变对象 | 未用 |

---

## 第二部分 · MyBatis 这条链上的面试点

> 写法：每节**结论一句话 → 为什么 → 怎么亲手验 → 一句口述版**。工程根目录 `D:\Java\Projects\campus-groupmeal\mybatis-redo\`。

### 2.1 Mapper 接口为什么没有实现类

**结论**：`session.getMapper(EmpDao.class)` 返回的是 **JDK 动态代理对象**，方法体不存在；调用时代理拿 `namespace（接口全类名）＋ id（方法名）` 去 `Configuration` 里找 `MappedStatement` 执行。

**怎么验（实测 2026-11-04）**：临时打三行——`dao.getClass().getName()` → `jdk.proxy2.$Proxy4`；`Proxy.getInvocationHandler(dao).getClass().getName()` → `org.apache.ibatis.binding.MapperProxy`；`dao instanceof EmpDao` → `true`（`dao = session.getMapper(EmpDao.class)`）。

再看真相的路径：`Ctrl+点击（转到声明）` `getMapper` → 落到 `DefaultSqlSession.getMapper()` → `configuration.getMapper(type, this)` → `MapperRegistry.getMapper()` → `MapperProxyFactory` → `MapperProxy.invoke()`。一路点下去**没有任何 `EmpDaoImpl`**（本工程也确实没有，`src\main\java\com\groupmeal\dao\` 下只有 `EmpDao.java` 一个文件）。

**XML 和接口不是一对一**：`src\main\resources\mapper\EmpMapper.xml` 里有 `<select id="querySalaryGt" resultType="Emp">`，而 `EmpDao.java` **没有**同名方法——它是"孤儿 SQL"。用字符串 API 能调通：`src\test\java\com\groupmeal\StringApiTest.java` 的 `调一个接口里没有的SQL()` 调 `session.selectList("com.groupmeal.dao.EmpDao.querySalaryGt", 5000)`。**实测行数 = 7**（种子 4 行 ＋ 库里还有 3 行历史残留 `测试员工`；把那 3 行清掉才是 4 行）。

**一句口述版**：*"Mapper 接口不用写实现，MyBatis 用 JDK 动态代理生成 `MapperProxy`，靠 namespace 等于接口全类名、id 等于方法名这两个字符串拼起来定位 SQL；所以接口和 XML 是多对一的注册关系，XML 里可以有接口没有的语句。"*

> 「标注」**纠错**：02 册 §1.1 说"字符串方式写错 id 报 `Invalid bound statement`"，实测不准确，两条路径的异常**不一样**。① **字符串 API** 写错 id（实测 `queryall`）→ `org.apache.ibatis.exceptions.PersistenceException`，内层 `IllegalArgumentException: Mapped Statements collection does not contain value for com.groupmeal.dao.EmpDao.queryall`（所以 `StringApiTest.字符串方式故意写错一次()` 断言 `contains("queryall")` 是对的）。② **接口有方法、XML 没这个 id** → `org.apache.ibatis.binding.BindingException: Invalid bound statement (not found): 接口全类名.方法名`（临时接口实测到原样文本）。面试区分就说：**字符串路径是查表失败，代理路径是绑定失败**。

### 2.2 `#{}` 与 `${}`：一个占位，一个拼串

**结论**：`#{}` 编译成 JDBC 的 `?`，走 `PreparedStatement.setString/setXxx`，**参数不可能变成语法**；`${}` 是**字符串直接替换进 SQL**，等于你自己把用户的输入拼进语句。

`EmpMapper.xml`（现有文件）里全是 `#{}`：`where empno = #{empno}`、`values(#{ename}, #{salary}, #{deptno}, #{hiredate})`、`where salary &gt; #{minSal}`。所以本工程**没有注入面**。

**实测对照（2026-11-04，只读 select）**：临时写两条语句，一条用 `${minSal}` 一条用 `#{name}`：

| 语句 ＋ 输入 | 日志里 `==>  Preparing:` 的内容 | 返回行数 |
|---|---|---|
| `where salary > ${minSal}` ＋ `9000` | `... where salary > 9000`（`Parameters:` 为空） | 2 |
| `where salary > ${minSal}` ＋ `9000 or 1=1` | `... where salary > 9000 or 1=1` | **7（全表被捞出）** |
| `where ename = #{name}` ＋ `张三` | `... where ename = ?`，`Parameters: 张三(String)` | 1 |
| `where ename = #{name}` ＋ `' or '1'='1` | `... where ename = ?`，`Parameters: ' or '1'='1(String)` | **0（当字面量比较）** |

看到了吧：`${}` 那行日志里 SQL **被用户输入改写了结构**，`#{}` 那行永远还是 `?`。生产上更狠的串是 `-9000 union select user(), host() from user ...`（把别人的表带出来）或者在 `update`/`delete` 的位置拼 `; drop table emp`——那就是删表事故。

`${}` 唯一正当用途是**没法用占位符的位置**：表名、列名、`order by` 的方向。用了就必须**白名单校验**——做法是新建一个常量集合（示意，不属于本工程）`private static final Set<String> ORDER_COLS = Set.of("empno", "salary", "hiredate");`，进来先 `if (!ORDER_COLS.contains(col)) throw new IllegalArgumentException("非法排序列: " + col);`，校验过了 XML 里才敢写 `order by ${col}`。

**一句口述版**：*"井号是预编译占位符，值永远不会变成 SQL 语法，所以防注入；美元符是字符串拼接，只能用在表名、列名、order by 这类占位符放不进去的地方，而且必须白名单校验。"*

### 2.3 一级缓存（正菜在 06 册，这里只给结论 ＋ 怎么验）

**结论**：一级缓存 = **SqlSession 级**，默认开、**不能整个关掉**（`localCacheScope=STATEMENT` 只是把作用域缩到单条语句，等于用完即弃）；要手动清就 `session.clearCache()`。**同一个 session、同一个 `statement id`、同一个参数** → 第二次不发 SQL，直接给你同一个对象引用。

**实测**（`MyBatisUtil.getSession()` × 两种用法，数日志里 `Preparing:` 的次数）：

| 用法 | `Preparing:` 出现次数 | 返回值 |
|---|---|---|
| 同一个 session 连查两次 `dao.queryAll()` | **1** | 两次是**同一个 List 实例**（`a == b` 为 true），元素对象也同一个 |
| 两个 session 各查一次 | **2** | 元素是不同对象（`a.get(0) == b.get(0)` 为 false） |

日志能看见，是因为 `mybatis-config.xml` 的 `<settings>` 里有 `<setting name="logImpl" value="STDOUT_LOGGING"/>`——`StdOutImpl` 适配器，直接喷 `System.out`，前缀是 `==>  Preparing:` / `==> Parameters:`。

**和 02 册的关联点**：02 册 §5.4 要求"实验做完把 `autoMappingBehavior` 那行删掉"，本质原因是**缓存实验看的是"属性有没有值"**——如果全局 `NONE` 让所有 `resultType` 查询的属性都是 null，你会把"映射被关了"误判成"缓存命中了"。这两件事一个是**填值**阶段、一个是**发不发 SQL**阶段，别混。

**一句口述版**：*"一级缓存在 SqlSession 内，命中条件是 statement id ＋ 参数相同，默认开启；所以真实项目里一次请求一个 session，事务提交或 session 关闭缓存就没了，这也是 MyBatis 官方建议不要跨线程复用 session 的原因之一。"*（二级缓存：`namespace` 级、跨 session、要在 XML 开 `<cache/>` 且实体序列化，06 册正菜，这里不展开。）

### 2.4 SqlSession 生命周期与事务

**结论**：一个 `SqlSession` = 一条数据库连接 ＋ 一段事务边界。`MyBatisUtil.getSession()` 走 `factory.openSession()`，**等价于 `openSession(false)`，不自动提交**（`src\main\java\com\groupmeal\util\MyBatisUtil.java` 的注释就是这么写的：「读操作用：默认不自动提交」）；写操作要么手动 `commit()`，要么用 `getSession(true)` 自动提交。

**怎么验**：把 `EmpDaoTest.增()` 用的 session 从 `MyBatisUtil.getSession(true)` 换成 `getSession()`，测试照样**绿**，但库里没那行（没提交，`close()` 时回滚）。测试类用 autoCommit 就是为了"每个测试自己闭环"。真实项目为什么不在 DAO 里提交：一次业务常跨多个 DAO 方法（下单 = 插订单 ＋ 扣库存 ＋ 加积分），提交权属于**业务方法边界**，即 Spring 的 `@Transactional`；Spring 用 `SqlSessionTemplate` 把 session 绑到线程上，同事务内所有 Mapper 调用共用一个 session——顺带让一级缓存真正起作用。（这条实验**未实测**，方向确定：`openSession(false)` ＋ 不 `commit()` ＝ 回滚。）

**"自己插的自己删"这个习惯的由来**：`EmpDaoTest.增()` 末尾有 `assertEquals(1, dao.deleteByEmpno(emp.getEmpno()))`，`改()` 末尾把工资 `setSalary(origin)` 还原。原因就是这台机器上跑过 3 次的历史：库里留了 3 行 `测试员工`（empno 6/8/10），张三工资从种子 8000 被推到 **9500**（实测只读）。autoCommit 让每次测试真的落库，所以**测试必须自带清理**，否则"按主键查断言张三是 8000"这类验证会越来越对不上。

**一句口述版**：*"SqlSession 不是线程安全的，也不能当单例用；工程惯例是一请求一 session，纯 MyBatis 自己 openSession/commit/close，进 Spring 就交给 @Transactional 和 SqlSessionTemplate 托管。"*

### 2.5 结果映射的三种解法与优先级（这一节最容易讲错，逐字看）

**结论（定例）**：**同名 → `mapUnderscoreToCamelCase` → resultMap**。

关键事实：**自动映射是"大小写不敏感"的逐名比较**，和驼峰无关。本工程列名是全小写连写（`empno`、`deptno`），而 Java 属性是 `empNo`、`deptNo`——只差大小写，**所以能自动映射上**。`src\main\resources\mybatis-config.xml` 里 `mapUnderscoreToCamelCase=true` 对这五个列**没有任何作用**，因为它们**没有下划线**。

`EmpVo` 五个属性正好覆盖两种情况（实测：临时用 `resultType="com.groupmeal.vo.EmpVo"` 直接查 `select empno, ename, salary, deptno, hiredate from emp`）：

| VO 属性 | 数据库列 | 自动映射？ | 为什么 |
|---|---|---|---|
| `empNo` | `empno` | **能**（值=1） | 忽略大小写后两串完全相等：`EMPNO` = `EMPNO`。**不是驼峰转换的功劳** |
| `deptNo` | `deptno` | **能**（值=1） | 同上 |
| `empName` | `ename` | **不能**（null） | 忽略大小写也不等（`ENAME` ≠ `EMPNAME`），且列里没下划线可转 |
| `money` | `salary` | **不能**（null） | 完全不同的词 |
| `hDate` | `hiredate` | **不能**（null） | 同上 |

再加一组对照（列名改成下划线风格 `emp_no / emp_name / money / h_date / dept_no` 之后）：

| 驼峰开关 | 结果（实测） |
|---|---|
| **关** | 只有 `money=9500.0` 有值（列名与属性名精确同名）；其余四个全 null |
| **开**（本工程配置） | 五个**全有值**：`emp_no→empNo`、`emp_name→empName`、`h_date→hDate`、`dept_no→deptNo` 都被转换后对上 |

→ 一句话：**开关管的是"有下划线的列"，大小写不敏感管的是"只差大小写的列"，两者不是同一件事。**

**那本工程 `EmpMapper.xml` 里的 `<resultMap id="empVoMap">` 是不是多余？** 不多余：`ename→empName`、`salary→money`、`hiredate→hDate` 这种"词都不同"的映射，只有**显式**手段能救——resultMap 或 SQL 里的 `AS` 别名（02 册 §4），自动映射再怎么开关都认不出来。`queryAllVo` 用的就是 `resultMap="empVoMap"`，`ResultMapTest.不一致字段也能映射()` 的三个断言（`getEmpName/getMoney/getHDate` 非 null）就是它的验收。

**一句口述版**：*"三种解法按成本排序：字段同名靠自动映射；只有下划线/驼峰差异就开 `mapUnderscoreToCamelCase`；词语本身不一样才写 resultMap 或用 AS 别名。自动映射的比较是大小写不敏感的，所以 `empno` 能落到 `empNo` 上；而 `ename` 落不到 `empName`，必须显式映射。"*

### 2.6 从手写 DAO 到 MyBatis：它到底替你省掉了什么

**结论**：MyBatis 接管的是**除"SQL 本身"以外**的所有 JDBC 机械代码。他手写的 JDBC 版在同一仓库的另一门课里（**只读、不改**）：`D:\Java\Projects\campus-groupmeal\javaweb-course\webdemo2\src\main\java\com\groupmeal\util\Conn.java` 与 `...\dao\UsersDao.java`。

| 手写版在干什么（真实代码） | MyBatis 里由谁做 | 本工程对应 |
|---|---|---|
| `Conn.getConn()` 里 `Class.forName("com.mysql.cj.jdbc.Driver")` ＋ `DriverManager.getConnection(url,user,pwd)` | `SqlSessionFactory` 建 `PooledDataSource`，驱动反射加载 | `mybatis-config.xml` 的 `<dataSource type="POOLED">` ＋ `jdbc.properties` |
| `bind(ps, args)`：`for(i) ps.setObject(i+1, args[i])` | `#{}` 占位符自动绑参（按类型选 setter） | `EmpMapper.xml` 的 `values(#{ename}, #{salary}…)` |
| `Conn.selResult()`：`ResultSetMetaData` 取列数、`while(rs.next())` 每行塞一个 `Map`；`UsersDao.selUser()` 再手写 5 行 `u.setUid(...)`、`String.valueOf(r.get("username"))` | 结果集 → 对象：反射 new ＋ 调 setter ＋ 自动映射，**这一整段没有** | `resultType="Emp"`、`resultMap="empVoMap"`；`EmpDao.queryByEmpno()` 只有一行声明 |
| `close(rs, ps, conn)` 三个 try 吞异常；方法签名一路 `throws Exception` | `SqlSession.close()` ＋ 异常统一包成 `PersistenceException` 等 | `MyBatisUtil.close(session)` ＋ `finally` |
| SQL 写在 Java 里（`"select uid,username,passwd,email,regDate from users order by uid"` 散在 DAO）；每次查询现开现关连接；分页自己拼 `limit` | SQL 搬到 XML，Java 侧只剩接口；连接池 ＋ 一级缓存；`databaseIdProvider` ＋ 插件 | `mapper/EmpMapper.xml` 里 8 条语句（5 select／1 insert／1 update／1 delete）；`POOLED`（→ §2.3/2.8） |
| 主键回填：自己再查一次 `last_insert_id()` | `useGeneratedKeys="true" keyProperty="empno"` | `saveEmp`（实测打印出回填的 `empno`） |

**一句口述版**：*"MyBatis 不是替代 JDBC，是包装 JDBC：连接、参数绑定、结果集转对象、资源释放、异常这几段模板代码它全包了，我只留 SQL 和映射规则；我仓库里有同一套业务的手写 JDBC 版和 MyBatis 版，可以直接对比给你看省了哪几行。"*

### 2.7 动态 SQL（03 册正菜，这里只要一个"为什么"）

**预告**：`<if>`／`<where>`／`<set>`／`<trim>`／`<foreach>`／`<choose>`。`<where>` 存在的意义就是**自动吃掉第一个多余的 `AND`**，`<set>` 自动去掉尾随逗号——`updateEmp` 以后要改成"传了才更新"就靠它。

**为什么 XML 里的比较号要写 `&gt;`**：XML 解析器把 `<` 当标签起始、`&` 当实体起始。SQL 里的 `<` 裸写会让**文件解析失败**（连构建都过不去），`>` 勉强能过但**推荐一律转义**。他 `EmpMapper.xml`（现有文件）里 `querySalaryGt` 的真实写法就是 `select empno,ename,salary,deptno,hiredate from emp where salary &gt; #{minSal}`。

两种解法：① 实体 `&gt;` / `&lt;` / `&lt;=`；② 整段包 `<![CDATA[ ... ]]>`，里面原样写。条件复杂时 CDATA 更省事，但 `<if>` 这类标签不能进 CDATA（那就等于没写）。

**一句口述版**：*"动态 SQL 解决的是'条件可有可无'的拼接问题；XML 里比较号必须转义或用 CDATA，因为 `<` 是标签起始符。"*

### 2.8 分页（05/06 册内容，只给原理）

**结论**：手写就是 SQL 里加 `limit #{offset}, #{size}`（MySQL 方言），代价是要自己再写一条 `select count(*)` 拿总数。PageHelper 的做法是：**注册一个 MyBatis 拦截器（`Interceptor`），在 `Executor` 执行前把原 SQL 改写成带 `limit` 的 SQL，并自动执行一条 count 查询，把结果塞进 `PageInfo`**。

关键认知（面试会追问"它是不是内存分页"）：**是数据库分页，不是把全表查进内存再 cut**；`PageHelper.startPage()` 把这些参数存在 `ThreadLocal` 里，所以**必须紧跟第一条查询**，中间不能插别的 SQL，否则会"分页打到不该打的那条语句上"。

**一句口述版**：*"PageHelper 用 MyBatis 的拦截器机制改写 SQL 加分页、并自动补一条 count 查询，本质还是数据库分页；用 startPage 要当心它靠 ThreadLocal 传参。"*

### 2.9 `@Param` 与多参数（02 册 §6.5 的 30 秒版）

**结论**：**0 个或 1 个参数**随便写；**≥2 个参数**必须每个都 `@Param("名字")`，否则 XML 里的 `#{deptno}` 找不到名字。

报错原文（背下来，一眼认出）：`org.apache.ibatis.binding.BindingException: Parameter 'deptno' not found. Available parameters are [arg1, arg0, param1, param2]`

选择规则：参数就是**一个实体的字段** → 传对象（`saveEmp(Emp)`、`updateEmp(Emp)` 就是这样）；**几个独立条件** → `@Param`；**个数不定／来自前端表单** → `Map`。`src\main\java\com\groupmeal\dao\EmpDao.java` 现在所有方法都是 0 或 1 个参数，所以看不见这个坑——**面试要主动说出"我踩过"**。

**一句口述版**：*"多参数时 MyBatis 只按位置给参数起名（arg0/param1），不知道你的语义名字，所以 `#{}` 对不上；`@Param` 就是把名字钉上去，单参数传对象时它按 getter 取值。"*

### 2.10 连接参数外置与口令安全

现状（`mybatis-config.xml` **现有文件**，配置第一段）：`<properties resource="jdbc.properties"/>` 在最前面，紧跟 `<settings>`（里面只有 `logImpl=STDOUT_LOGGING` 和 `mapUnderscoreToCamelCase=true` 两行），`<dataSource>` 里四个值全是 `${driver} ${url} ${user} ${password}`——**源码里不出现任何真实口令**。

**为什么 `<properties>` 必须在 `<settings>` 之前**：DTD 硬性规定元素顺序，写错**不是警告，是构建失败**。实测（我故意把 `<settings>` 挪到前面再 `build()`）：`PersistenceException: ### Error building SqlSession. ### Cause: BuilderException: Error creating document instance. Cause: SAXParseException; 元素类型为 "configuration" 的内容必须匹配 "(properties?,settings?,typeAliases?,typeHandlers?,objectFactory?,objectWrapperFactory?,reflectorFactory?,plugins?,environments?,databaseIdProvider?,mappers?)"`。这条顺序串本身就是答案：**properties → settings → typeAliases → … → environments → … → mappers**，工程里注释第 ⓪①②③④ 段的编号就是照着它排的。

**口令卫生（工程素养，面试讲这个加分）**：`jdbc.properties` 被**仓库根** `.gitignore` 第 44 行 `**/jdbc.properties` 排除（实测 `git check-ignore -v` 命中该规则），仓库里只留 `jdbc.properties.example`（值是 `你的MySQL账号` 这种占位）。新机器上手：复制改名，只改 `user`／`password`。

> 「标注」最关键的一句安全意识：**如果明文口令曾经提交到公开仓库，改一次提交是抹不掉的**——历史里还在（`git log -p`、fork、CI 缓存、镜像都留着）。唯一的止损是**改账号口令让它作废**，顺手再考虑强推改写历史（会打乱协作者）。所以顺序是：先换口令，再谈清理。

**一句口述版**：*"我把数据库四要素外置成 jdbc.properties，配置文件只留占位符；那份文件进 .gitignore，仓库里放 .example 模板。已提交过的口令只能靠改账号作废，不能靠改提交抹掉。"*

### 2.11 常见报错急救表（现象原文 → 真因 → 一句修法）

| 现象（尽量原样） | 真因 | 修法 |
|---|---|---|
| `BindingException: Invalid bound statement (not found): com.groupmeal.dao.EmpDao.xxx` | **接口有方法，XML 没那个 id**，或 namespace ≠ 接口全类名，或 XML 没在 `<mappers>` 注册 | 三查：namespace 全类名 → id 与方法名逐字一致 → `<mapper resource="mapper/EmpMapper.xml"/>` 在不在 |
| `PersistenceException: ... IllegalArgumentException: Mapped Statements collection does not contain value for ...queryall` | 字符串 API 的 `namespace.id` 拼错（**注意**：这跟上一行不是同一个异常，§2.1 有实测） | 把字符串改成接口常量，或干脆别用字符串 API |
| `BindingException: Parameter 'deptno' not found. Available parameters are [arg1, arg0, param1, param2]` | ≥2 个参数没 `@Param` | 每个参数加 `@Param("...")`（§2.9） |
| `SQLException: Public Key Retrieval is not allowed` | MySQL 8 的 `caching_sha2_password` 首次认证要走 RSA 公钥，非 SSL 连接默认拒绝取公钥 | URL 加 `allowPublicKeyRetrieval=true`（本工程 `jdbc.properties` 的 url **已经带了**），或开 SSL |
| `ClassNotFoundException: com.mysql.cj.jdbc.Driver` | 没引驱动依赖／坐标写成了老的 `mysql:mysql-connector-java`／`<properties>` 的 key 名和 `${driver}` 不一致 | 用 `com.mysql:mysql-connector-j:8.0.33`（pom 现状），driver 类名 `com.mysql.cj.jdbc.Driver` |
| `IllegalArgumentException: Result Maps collection does not contain value for ...`（"找不到结果映射"） | `resultMap="empVoMap"` 引用名和 `<resultMap id="">` 不一致，或引用了别的 namespace 又没写全限定名 | 逐字对齐 id；跨 namespace 引用写 `namespace.id` |
| 对象不是 null，但**属性全 null**（`Emp(empno=null, ename=null…)`） | 全局 `autoMappingBehavior=NONE`（02 册 §5.2 的实验），或列名和属性名谁都对不上 | 删掉 `NONE` 那行（→ §2.3 关联点），或给该查询配 resultMap／AS 别名 |
| 查出来的中文是 `???` 或乱码 | 三层都要对：库/表字符集（本工程建表脚本 `DEFAULT CHARACTER SET utf8mb4`）、JDBC URL 的 `characterEncoding=utf8` ＋ `useUnicode=true`、控制台/文件编码（pom 里 `project.build.sourceEncoding=UTF-8`，本机 `cmd` 默认页是 GBK 需 `chcp 65001`） | 逐层查，最常漏的是 URL 参数 |
| `hiredate` 比我插的日期早/晚 8 小时 | 服务端时区与 JVM 口径不一致 | URL 加 `serverTimezone=Asia/Shanghai`（本工程 url 已带） |
| 启动就报 `元素类型为 "configuration" 的内容必须匹配 "(properties?,settings?,...)"` | 配置文件元素顺序错（§2.10 实测原话） | 按那条顺序串重排 |

> 「标注」本机口径（实测）：MySQL **8.4.6**（`select @@version`），`root@localhost` 的认证插件是 **caching_sha2_password**，而且 `select @@default_authentication_plugin` 直接报 `Unknown system variable`——这个变量在 8.4 已经没了，`mysql_native_password` 那套老写法在新版上指望不住。所以**新项目别再照老教程配 native 认证**，`allowPublicKeyRetrieval=true` 才是当前的解法。（"native 已从 8.4 移除"这一句我是按官方变更说明写的，本机只验到了上面这些现象。）

---

## 结尾 · 自检 8 问（能被当堂追问的）

1. `@Data` 生成了哪六样？为什么说"运行时不需要 lombok"？（→ §1.1、§1.2）
2. `EmpVo` 只有 `EmpVo()` 一个构造器，`Emp` 却有两个，差在哪？（→ §1.1 表第 6 行 ＋ §1.4 标注）
3. 我怎么向你证明 Lombok 真的生成了 setter？给命令。（→ §1.3：`mvn -o -B compile` ＋ `cmd //c "javap -p target\classes\...EmpVo.class"`）
4. `Invalid bound statement` 和 `Mapped Statements collection does not contain value` 什么时候各出现一次？（→ §2.1 纠错标注）
5. 列 `empno` 能自动映射到属性 `empNo`，是靠驼峰开关吗？（→ §2.5，答案：**不是**，靠大小写不敏感；`mapUnderscoreToCamelCase` 对无下划线的列不做任何事）
6. 同一个 session 连查两次只发一条 SQL，你怎么让我亲眼看到？日志哪来的？（→ §2.3 ＋ `logImpl=STDOUT_LOGGING`）
7. `openSession()` 和 `openSession(true)` 差别是什么？为什么你的测试自己插完又自己删？（→ §2.4）
8. 表名列名 `order by` 只能 `${}`，那怎么防注入？（→ §2.2 白名单校验）

## 面试 30 秒总述（背这段）

> "MyBatis 我从头重现过一遍——不是跟着教程抄，是我先把 JDBC 手写实现过一次：`Conn` 里注册驱动、`DriverManager` 拿连接、`PreparedStatement` 逐个 `setObject` 绑参、`ResultSetMetaData` 遍历拼对象、三个 `try` 关资源；DAO 里 SQL 和 Java 混在一起。然后我用 MyBatis 把同一件事重做：连接交给 `<dataSource type="POOLED">` 和 `jdbc.properties`，SQL 搬到 `EmpMapper.xml`，参数交给 `#{}` 预编译，结果集交给 `resultType`／`resultMap` 反射填 setter，资源和异常框架管，DAO 只剩一个接口，`getMapper()` 用 `MapperProxy` 动态代理，靠 namespace ＋ id 定位语句。所以我能说清 MyBatis 到底帮你省掉了什么，也能回答它替不掉的部分——映射规则和 SQL 本身还是你的责任。顺带我把 entity/VO 用 Lombok 的 `@Data` 简化，但我知道它是编译期注解处理器，出问题我能用 `javap` 把生成的 setter 反编译出来给你看。"

---

## 附：本附录实测台账（2026-11-04，这台机器）

| 我验过什么 | 方式 | 结果 |
|---|---|---|
| 依赖版本、`pom.xml` 现状 | 读文件 | mybatis 3.5.19 / mysql-connector-j 8.0.33 / lombok 1.18.42 provided / junit-jupiter 5.9.2 test；编译级别 17，源码编码 UTF-8 |
| 编译 ＋ Lombok 产物 | `D:\Java\Maven\apache-maven-3.9.0\bin\mvn -o -B compile` → `cmd //c "javap -p target\classes\com\groupmeal\vo\EmpVo.class"`（`Emp.class` 同） | `BUILD SUCCESS`（5 个源文件，5.280 s）；`EmpVo` 的 getter/setter/`equals`/`canEqual`/`hashCode`/`toString` ＋ **仅无参构造**；`Emp` 多一个全参构造。`java -version` / `javap -version` = 17.0.20 |
| 库实况（全程只 `select`／`show`，没改过任何数据） | JDBC 直连 `mydb` | `emp` 列序 `empno, salary, ename, deptno, hiredate`、**无外键**；现 7 行（种子 4 ＋ `测试员工` 3 行 empno 6/8/10），张三 salary 现值 **9500**；`dept` 3 行；MySQL **8.4.6**；`root@localhost` 插件 = `caching_sha2_password`，`@@default_authentication_plugin` 已不存在 |
| 代理类 ＋ 两种"找不到 SQL"异常 | `getMapper` 打印；字符串 API 写错 id；临时接口方法无 SQL | `jdk.proxy2.$Proxy4` / handler `org.apache.ibatis.binding.MapperProxy`；`Mapped Statements collection does not contain value for ...` vs `BindingException: Invalid bound statement (not found): ...` |
| `#{}` vs `${}`、一级缓存、自动映射四组合、配置元素顺序 | 临时 mapper ＋ 数 `Preparing:` 次数 ＋ 换配置跑 `build()` | 见 §2.2 / §2.3 / §2.5 表格；顺序错报 `PersistenceException` ＋ SAXParseException（含整条顺序串） |
| 口令没进仓库 | `git check-ignore -v src/main/resources/jdbc.properties` | 命中仓库根 `.gitignore:44` 的 `**/jdbc.properties`；`git ls-files` 里只有 `.example` |

**未实测（别当已验）**：① 你的 IDEA 版本与 Lombok 插件是否自带（文档按官方"2024.1 起自带"写）；② `mvn -o -B test` 全绿；③ §2.4 里"把 `getSession(true)` 改成 `getSession()` 测试仍绿但库里没落"这一实验（方向确定）；④ §1.3 第三步"注释掉 `@Data` 后编译报错"的具体报错文本；⑤ PageHelper／二级缓存／动态 SQL 的实际行为（03/05/06 册的正菜，本附录只给结论）。

*生成 2026-11-04 ｜ 上一册：02 Mapper 接口与结果映射 ｜ 下一册：03 动态 SQL 全标签*

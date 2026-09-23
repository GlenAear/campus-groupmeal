  # campus-groupmeal · 校园拼单订餐 ＋ 智能取餐柜

面向高校宿舍/教学楼场景的拼单订餐系统：同楼栋用户凑单达到起送门槛后合并下单，
取餐柜按格口分配，减少配送成本与等待时间。

## 技术栈

- **后端**：Java 17 · Spring Boot 3（规划中）· MyBatis · MySQL 8.4
- **前端**：简单连通（HTML/JSP → 后续按需要换 Vue）
- **构建**：Maven 3.9

## 目录

| 目录 | 内容 |
|---|---|
| `groupmeal-server/` | 项目主工程（SpringBoot，随培训进度落地） |
| `mybatis-redo/` | MyBatis 从头重现练习工程（对照课程 PDF 逐册手写一遍） |
| `javaweb-course/` | 课程作业与实验（JavaWeb / JSP / Servlet） |
| `docs/` | 选型分析、实验报告、设计文档 |

## 当前进度

- [x] 项目方向定案与选型分析（2026-09-17）
- [x] Tomcat 10.1.5 + IDEA 2024.1.2 环境搭建（实验报告1）
- [ ] MyBatis 全链路重现（Part 01–09）
- [ ] SpringBoot 主工程骨架
- [ ] V1：拼单成团 + 取餐柜格口分配

## 作者

侯奕聪 ｜ 陕西学前师范学院 · 24 级物联网二班

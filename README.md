> **完整文档见 [docs/项目文档.md](docs/项目文档.md)**(架构/部署/接口清单/权限模型/数据库设计/踩坑记录)。
> **演示视频**: [通用后台底座-演示.mp4](通用后台底座-演示.mp4)(46s,八章节自动走查录制,2026-10-27)。

后端 Spring Boot 2.7(Java 8)+ MySQL,前端 Vue 3 + Vite 4,全自建 UI 组件,无第三方组件库。

## 运行

**方式一(推荐):双击项目根目录的 `启动.bat`**,它会检查 Docker/MySQL、分别弹出后端和前端窗口,然后浏览器打开 http://localhost:5173(账号 admin / admin123)。

**方式二(手动)**:注意下面命令不要带任何注释复制进 cmd。

前提:本机 Docker Desktop 已启动且有 `mysql` 容器(localhost:3306, root/123456),JDK 8、Maven 3.6+、Node 14.18+。

首次运行先初始化数据库(在项目根目录执行,两行分开):

```
docker cp sql/admin_base.sql mysql:/tmp/admin_base.sql
docker exec mysql sh -c "mysql -uroot -p123456 --default-character-set=utf8mb4 -e \"source /tmp/admin_base.sql\""
```

启动后端(窗口 1):

```
cd backend
mvn spring-boot:run
```

启动前端(窗口 2):

```
cd frontend
npm install
npm run dev
```

完成后浏览器打开 http://localhost:5173 。

演示账号:`admin / admin123`(超级管理员);`chensy / admin123`(系统管理员);`wumin / admin123`(业务管理员,数据权限=本部门及以下)。

## 功能(对应视频 8 个章节)

| 章节 | 内容 |
| --- | --- |
| 01 登录 | 深色时钟环动效、验证码(参数可开关)、登录按钮收成光球→对勾 |
| 02 工作台 | 登录脉搏折线(24h/7d/30d)、里程表滚动数字、成功率刻度环、数据权限分布 |
| 03 命令面板 | ⌘K/Ctrl+K 搜页面 + 「?问系统」权限溯源(谁能删除用户/闲置角色/未分配用户/孤立菜单) |
| 04 权限溯源 | 角色管理-溯源:权限全景星图(用户/角色/菜单节点+卫星+粒子动效),点角色展开授权链与详情 |
| 05 组织画布 | 部门管理-画布:树形组织图,点击部门光点从根节点追过去,详情卡+成员 |
| 06 日志 | 登录日志(指标卡+星期×小时热力图+趋势)、操作日志(AOP 自动记录,行内展开 JSON 参数) |
| 07 字典·参数 | 字典卡片原地展开(邻居自动让位)、参数分组卡片+未保存保存条(查看差异/放弃/批量保存) |
| 08 外观 | 参数里切 light/dark/auto 全站深色过渡,配置持久化,⌘K 也可切换 |

底座功能:用户/角色/菜单/部门/岗位/字典/参数 CRUD、菜单+按钮两级权限码(前端按钮级隐藏+后端注解拦截)、
数据权限(全部/本部门及以下/本部门/仅本人)、JWT+内存会话(退出登录/在线用户统计)、验证码、代码生成骨架预览。

## 结构

```
backend/   Spring Boot 2.7 单模块
  src/main/java/com/adminbase/
    common/    R/分页/JWT/上下文/权限注解/操作日志注解
    auth/      拦截器/Token会话/验证码
    aspect/    操作日志切面
    config/    WebConfig(拦截器/CORS/BCrypt Bean)
    controller/ auth system(7) monitor(2) dashboard trace codegen
    mapper/    MyBatis 注解 SQL
    domain/    实体
frontend/  Vue 3 + Vite 4
  src/ui/       自建组件库(按钮/输入/选择/开关/表格/弹窗/抽屉/滚动数字/刻度环...)
  src/viz/      星图/组织画布/热力图
  src/views/    登录/工作台/系统管理7页/监控2页/代码生成
  src/stores/   pinia:auth(菜单/权限)/theme
sql/admin_base.sql  建库脚本(含生成 30 天模拟日志的存储过程)
```

## 注意

- 后端端口在 `backend/src/main/resources/application.yml`(`SERVER_PORT`,默认 8081;本机 8080 常驻其他服务)。
- 数据库连接走环境变量 `DB_HOST/DB_PORT/DB_NAME/DB_USER/DB_PWD`,默认值对应本机 Docker。
- JWT 密钥用 `JWT_SECRET` 环境变量覆盖,生产必须换。
- 岗位/角色/字典等种子数据即视频同款(酷炫科技集团/周明远/陈思远/吴敏…)。
- Node 14 环境锁定了 vite@4;升级 Node 后可换 vite@5+,代码无需改动。

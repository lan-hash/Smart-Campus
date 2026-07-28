# Smart Campus (智能校园综合服务平台)

基于 Spring Boot 3 + Spring AI Alibaba + Vue3 的智能校园综合服务平台，集二手交易、代课服务、学习论坛、游戏社区、表白墙、AI 助手、消息通知、后台管理于一体。

---

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.2.5 |
| AI 集成 | Spring AI Alibaba (通义千问) |
| 持久层 | MyBatis-Plus 3.5.5 |
| 数据库 | MySQL 8.0 (utf8mb4) |
| 缓存 | Redis |
| 文件存储 | 阿里云 OSS |
| 认证 | JWT (JJWT 0.11.5) + BCrypt |
| 前端框架 | Vue 3 + Vite |
| UI 组件 | Element Plus |
| 状态管理 | Pinia |
| 路由 | Vue Router 4 |

---

## 功能模块

| # | 模块 | 接口数 | 说明 |
|---|------|--------|------|
| 1 | 基础公共 | - | 统一返回格式、异常处理、JWT 认证、跨域、OSS 上传 |
| 2 | 用户模块 | 11 | 注册登录、个人资料、校园认证、关注/拉黑 |
| 3 | 论坛模块 | 14 | 学习论坛 + 游戏论坛，帖子/评论/点赞/收藏 |
| 4 | 表白墙 | 7 | 匿名/实名表白、点赞、评论 |
| 5 | 二手交易 | 13 | 商品发布、收藏、购买、交易管理 |
| 6 | 代课服务 | 9 | 发布需求、接单、完成、评价 |
| 7 | 消息通知 | 9 | 私信聊天、系统通知、系统公告 |
| 8 | AI 智能 | 5 | AI 聊天、会话管理、内容审核 |
| 9 | 后台管理 | 14 | 数据面板、用户管理、举报处理、帖子管理、公告管理 |
| | **合计** | **82** | |

---

## 项目结构

```
springboot/
├── campus-platform.sql          # 数据库初始化脚本（27张表）
├── campus-backend/              # Spring Boot 后端
│   ├── src/main/java/com/campus/
│   │   ├── common/               # 公共模块
│   │   │   ├── config/           # 配置类 (Web/CORS/OSS/Redis/MyBatis-Plus)
│   │   │   ├── exception/        # 异常处理
│   │   │   ├── result/           # 统一返回 Result / PageResult
│   │   │   └── util/             # 工具类 (JWT/Redis)
│   │   ├── controller/           # 控制器层
│   │   ├── dto/                  # 请求参数对象
│   │   ├── entity/               # 数据库实体
│   │   ├── interceptor/          # JWT 拦截器
│   │   ├── mapper/               # MyBatis Mapper
│   │   ├── service/              # 服务接口
│   │   │   └── impl/             # 服务实现
│   │   └── vo/                   # 视图对象
│   └── src/main/resources/
│       ├── application.yml      # 主配置（环境变量占位）
│       ├── application-local.yml # 本地配置（不提交，含敏感信息）
│       └── mapper/               # MyBatis XML 映射文件
├── campus-frontend/             # Vue3 前端
│   ├── src/
│   │   ├── api/                  # 接口请求
│   │   ├── views/                # 页面组件
│   │   ├── stores/               # Pinia 状态管理
│   │   ├── router/               # 路由配置
│   │   └── App.vue
│   ├── package.json
│   └── vite.config.js
└── .gitignore
```

---

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+
- Node.js 16+
- 阿里云 OSS 账号（用于图片存储）

### 1. 数据库初始化

```bash
mysql -u root -p < campus-platform.sql
```

该脚本会创建 `campus_platform` 数据库及全部 27 张表和初始数据（含默认管理员账号 admin / admin123）。

### 2. 后端配置

```bash
cd campus-backend
```

创建 `src/main/resources/application-local.yml`，填入真实配置：

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_platform?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: 你的数据库用户名
    password: 你的数据库密码
  data:
    redis:
      host: localhost
      port: 6379
  ai:
    dashscope:
      api-key: 你的通义千问API Key

aliyun:
  oss:
    endpoint: oss-cn-hangzhou.aliyuncs.com
    access-key-id: 你的AK
    access-key-secret: 你的SK
    bucket-name: 你的桶名
```

### 3. 启动后端

```bash
mvn spring-boot:run
```

后端运行在 `http://localhost:8080`

### 4. 启动前端

```bash
cd campus-frontend
npm install
npm run dev
```

前端运行在 `http://localhost:5173`，已配置代理将请求转发到后端。

---

## API 概览

### 用户模块 `/user`

| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|:------:|
| POST | /user/register | 注册 | 否 |
| POST | /user/login | 登录 | 否 |
| GET | /user/info | 获取当前用户信息 | 是 |
| PUT | /user/profile | 修改个人资料 | 是 |
| POST | /user/avatar | 上传头像 | 是 |
| POST | /user/campus-verify | 校园认证 | 是 |
| POST | /user/follow/{userId} | 关注 | 是 |
| DELETE | /user/follow/{userId} | 取消关注 | 是 |
| POST | /user/block/{userId} | 拉黑 | 是 |
| DELETE | /user/block/{userId} | 取消拉黑 | 是 |
| GET | /user/follows | 关注列表 | 是 |
| GET | /user/fans | 粉丝列表 | 是 |

### 论坛模块 `/forum`

| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|:------:|
| GET | /forum/categories | 版块列表 | 否 |
| GET | /forum/posts | 帖子分页 | 否 |
| GET | /forum/posts/{id} | 帖子详情 | 否 |
| POST | /forum/posts | 发布帖子 | 是 |
| PUT | /forum/posts/{id} | 编辑帖子 | 是 |
| DELETE | /forum/posts/{id} | 删除帖子 | 是 |
| POST | /forum/posts/{id}/like | 点赞/取消 | 是 |
| POST | /forum/posts/{id}/collect | 收藏/取消 | 是 |
| GET | /forum/posts/{id}/comments | 评论列表 | 否 |
| POST | /forum/posts/{id}/comments | 发表评论 | 是 |
| POST | /forum/comments/{id}/reply | 回复评论 | 是 |
| POST | /forum/comments/{id}/like | 评论点赞 | 是 |
| GET | /forum/my/posts | 我的帖子 | 是 |
| GET | /forum/my/collects | 我的收藏 | 是 |

### 表白墙模块 `/confession`

| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|:------:|
| GET | /confession/list | 表白列表 | 否 |
| GET | /confession/{id}/comments | 评论列表 | 否 |
| POST | /confession | 发布表白 | 是 |
| POST | /confession/{id}/like | 点赞/取消 | 是 |
| POST | /confession/{id}/comments | 发表评论 | 是 |
| DELETE | /confession/{id} | 删除表白 | 是 |
| GET | /confession/my | 我的表白 | 是 |

### 二手交易模块 `/secondhand`

| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|:------:|
| GET | /secondhand/categories | 分类列表 | 否 |
| GET | /secondhand/products | 商品列表 | 否 |
| GET | /secondhand/products/{id} | 商品详情 | 否 |
| POST | /secondhand/products | 发布商品 | 是 |
| PUT | /secondhand/products/{id} | 编辑商品 | 是 |
| PUT | /secondhand/products/{id}/status | 修改状态 | 是 |
| DELETE | /secondhand/products/{id} | 删除商品 | 是 |
| POST | /secondhand/products/{id}/favorite | 收藏/取消 | 是 |
| POST | /secondhand/products/{id}/buy | 发起购买 | 是 |
| GET | /secondhand/my/products | 我的发布 | 是 |
| GET | /secondhand/my/favorites | 我的收藏 | 是 |
| GET | /secondhand/transactions | 我的交易 | 是 |
| PUT | /secondhand/transactions/{id}/status | 更新交易 | 是 |

### 代课服务模块 `/course`

| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|:------:|
| GET | /course/orders | 订单列表 | 否 |
| GET | /course/orders/{id} | 订单详情 | 否 |
| POST | /course/orders | 发布需求 | 是 |
| POST | /course/orders/{id}/accept | 接单 | 是 |
| PUT | /course/orders/{id}/complete | 完成订单 | 是 |
| PUT | /course/orders/{id}/cancel | 取消订单 | 是 |
| POST | /course/orders/{id}/evaluate | 评价 | 是 |
| GET | /course/orders/{id}/evaluations | 评价列表 | 否 |
| GET | /course/my/orders | 我的订单 | 是 |

### 消息通知模块 `/message`

| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|:------:|
| POST | /message/chat | 发送私聊 | 是 |
| GET | /message/chat/{userId} | 聊天记录 | 是 |
| GET | /message/chat/unread | 未读私聊数 | 是 |
| PUT | /message/chat/read/{fromUserId} | 标记已读 | 是 |
| POST | /message/chat/upload | 图片消息 | 是 |
| GET | /message/notifications | 通知列表 | 是 |
| GET | /message/notifications/unread | 未读通知数 | 是 |
| PUT | /message/notifications/read-all | 全部已读 | 是 |
| GET | /message/notices | 公告列表 | 否 |

### AI 智能模块 `/ai`

| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|:------:|
| POST | /ai/chat | 发送消息 | 是 |
| GET | /ai/chat/sessions | 会话列表 | 是 |
| GET | /ai/chat/{sessionId}/messages | 历史消息 | 是 |
| DELETE | /ai/chat/{sessionId} | 删除会话 | 是 |
| POST | /ai/review | 内容审核 | 是 |

### 后台管理模块 `/admin`

| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|:------:|
| POST | /admin/login | 管理员登录 | 否 |
| GET | /admin/dashboard | 数据面板 | 是 |
| GET | /admin/users | 用户列表 | 是 |
| PUT | /admin/users/{id}/status | 封禁/解封 | 是 |
| GET | /admin/reports | 举报列表 | 是 |
| PUT | /admin/reports/{id}/handle | 处理举报 | 是 |
| GET | /admin/posts | 帖子管理 | 是 |
| PUT | /admin/posts/{id}/status | 帖子审核 | 是 |
| PUT | /admin/posts/{id}/top | 置顶/取消 | 是 |
| PUT | /admin/posts/{id}/essence | 加精/取消 | 是 |
| GET | /admin/notices | 公告列表 | 是 |
| POST | /admin/notices | 发布公告 | 是 |
| DELETE | /admin/notices/{id} | 删除公告 | 是 |

---

## 数据库表（27张）

| # | 表名 | 说明 | 模块 |
|---|------|------|------|
| 1 | sys_user | 用户表 | 用户 |
| 2 | user_follow | 关注关系 | 用户 |
| 3 | user_block | 拉黑关系 | 用户 |
| 4 | secondhand_category | 二手分类 | 二手交易 |
| 5 | secondhand_product | 二手商品 | 二手交易 |
| 6 | secondhand_favorite | 商品收藏 | 二手交易 |
| 7 | secondhand_transaction | 交易记录 | 二手交易 |
| 8 | secondhand_evaluation | 交易评价 | 二手交易 |
| 9 | course_order | 代课订单 | 代课服务 |
| 10 | course_evaluation | 代课评价 | 代课服务 |
| 11 | forum_category | 论坛版块 | 论坛 |
| 12 | forum_post | 论坛帖子 | 论坛 |
| 13 | forum_comment | 论坛评论 | 论坛 |
| 14 | forum_like | 论坛点赞 | 论坛 |
| 15 | forum_collect | 论坛收藏 | 论坛 |
| 16 | forum_tag | 论坛标签 | 论坛 |
| 17 | forum_post_tag | 帖子标签关联 | 论坛 |
| 18 | confession | 表白墙 | 表白墙 |
| 19 | confession_comment | 表白评论 | 表白墙 |
| 20 | confession_like | 表白点赞 | 表白墙 |
| 21 | chat_message | 私聊消息 | 消息 |
| 22 | notification | 系统通知 | 消息 |
| 23 | system_notice | 系统公告 | 消息/管理 |
| 24 | ai_chat_record | AI 对话记录 | AI |
| 25 | ai_content_review | AI 审核记录 | AI |
| 26 | report | 举报 | 后台管理 |
| 27 | operation_log | 操作日志 | 后台管理 |

---

## 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 学生 | 自行注册 | - |

---

## 代码规范

- **Controller** — 只包含接口定义，不含业务逻辑
- **Service** — 接口与实现分离（service/ + service/impl/）
- **Mapper** — 简单 SQL 用注解，复杂 SQL（分页/多表关联/动态条件）用 XML
- **DTO** — 请求参数，带 `@Valid` 校验
- **VO** — 返回视图，`@JsonInclude(NON_NULL)` 控制字段
- **Entity** — `@TableField(fill = FieldFill.INSERT)` 自动填充 createTime

---

## License

MIT

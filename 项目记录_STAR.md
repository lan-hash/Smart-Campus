# 智能校园综合服务平台 — 项目记录（STAR 法则）

## S（Situation / 项目背景）

本项目旨在构建一个面向高校师生的**智能校园综合服务平台**，整合校园生活所需的各类服务场景。项目基于 **Spring AI Alibaba** 技术栈，采用前后端分离架构，后端使用 **Spring Boot 3.2.5 + MyBatis-Plus + MySQL 8.0**，前端使用 **Vue3 + Element Plus + Pinia**。平台覆盖用户管理、校园论坛、表白墙、二手交易、代课服务、消息通知、AI 智能助手、后台管理等 9 大核心模块，共涉及 27 张数据库表、87 个以上 RESTful API 接口。项目对安全性、代码规范性和文档完整性有较高要求，需实现 JWT 无状态认证、BCrypt 密码加密、阿里云 OSS 文件存储、Spring AI 大模型集成等企业级特性。

## T（Task / 任务与挑战）

核心任务是完成从 0 到 1 的全栈开发，具体包括：

1. **系统架构设计**：搭建前后端项目骨架，设计数据库表结构（27 张表），定义各模块的职责边界与依赖关系。
2. **认证与安全**：实现 JWT Token 生成与校验机制，确保未登录用户只能浏览公开内容（GET），所有写操作（POST/PUT/DELETE）必须经过身份验证；同时解决 WebConfig 路径排除与动态路径拦截的冲突问题。
3. **AI 能力集成**：基于 Spring AI Alibaba 集成通义大模型，实现 AI 对话助手、内容自动审核、帖子智能版块分类三大功能。
4. **文件存储**：对接阿里云 OSS，完成用户头像、帖子图片、聊天图片等文件的上传与访问链路。
5. **业务模块开发**：按功能优先级逐个完成 9 大模块的实体、DTO、VO、Mapper、Service、Controller 代码，确保 Service 层接口与实现分离、Controller 层只做参数接收与响应包装。
6. **文档输出**：生成完整的项目说明文档，包含系统架构图、调用链路图、接口列表、数据库表说明，以及每个 Controller 的完整源码。

## A（Action / 具体行动）

### 1. 数据库与架构层
- 设计并编写了 27 张数据库表的完整建表语句，覆盖用户、论坛、表白墙、二手交易、代课、消息、AI、后台管理、操作日志等全部领域。
- 后端采用经典四层架构：**Controller（接口层）→ Service（业务层，接口+实现分离）→ Mapper（数据层，注解+XML混合）→ Entity（实体层）**，请求参数封装为独立 DTO，响应对象封装为独立 VO。

### 2. JWT 认证体系
- 实现 **JwtUtils**（Token 生成与解析）、**JwtInterceptor**（请求拦截与 userId 注入）、**WebConfig**（拦截器注册与路径排除）三层体系。
- 关键设计：WebConfig 的 `excludePathPatterns` 仅排除固定路径（如 `/user/login`），动态路径（如 `/forum/posts/{id}`）通过 JwtInterceptor 内的 `PUBLIC_GET_PATTERNS` 正则白名单单独处理，确保 GET 请求公开访问的同时，POST/PUT/DELETE 请求强制要求 JWT 验证，彻底解决了「已登录用户无法操作」的路径冲突问题。

### 3. AI 模块集成
- 在 **AiController/AiService** 中封装 `ChatClient`，实现 AI 对话（支持会话管理）、内容审核（pass/block/review 三档风险判定）。
- 在 **ForumController** 中直接注入 `ChatClient`，实现发帖时的 AI 智能版块分类推荐。
- 将 Spring AI Alibaba 版本从 `1.0.0-M2` 升级至 `1.0.0-M5.1`，解决 `String` 无法转换为 `Prompt` 的 API 兼容性问题。

### 4. 文件上传与 OSS
- 实现 **UploadController**，通过阿里云 OSS Java SDK 将图片上传至 `campus/{yyyy/MM/dd}/{uuid}.{ext}` 路径，返回可直接访问的完整 URL。
- 配置 `OssConfig` 读取 `application-local.yml` 中的密钥信息，该配置文件加入 `.gitignore`，避免敏感信息泄露。

### 5. 业务模块逐一落地
- 按模块优先级（用户 → 论坛 → 表白墙 → 二手交易 → 代课 → 消息 → AI → 后台管理 → 文件上传）逐个输出代码，每个模块包含 Entity、DTO、VO、Mapper（注解/XML）、Service 接口、Service 实现、Controller 完整代码。
- 关键业务逻辑设计：
  - **点赞/收藏采用 toggle 模式**：先查询是否已点赞，已点赞则删除记录并减计数，未点赞则插入记录并加计数，保证幂等性。
  - **评论采用两级树结构**：顶级评论 `parent_id=0`，子评论通过批量查询 + Java Stream `groupingBy` 组装，避免 N+1 查询。
  - **二手交易状态联动**：交易完成/取消时自动联动更新商品状态（在售/已售）。
  - **后台管理跨模块注入**：AdminServiceImpl 直接注入 7 个其他模块的 Mapper，实现数据面板统计、帖子审核、用户封禁等跨模块操作，避免循环依赖。

### 6. 文档与规范
- 生成《全模块功能说明.docx》文档，包含 9 张架构/流程图、完整的请求调用链路（从前端 Vue 到数据库的 7 步流程）、每个模块的接口列表、数据库表说明、关键代码逻辑逐行解读，以及 **9 个 Controller 的完整源码**（类声明、依赖注入、全部接口方法）。
- 统一代码规范：使用 Lombok `@RequiredArgsConstructor` 替代 `@Autowired` 实现构造器注入；实体使用 `@TableField(fill = ...)` 自动填充时间戳；创建操作使用独立路径（如 `POST /forum/publish`）避免与 WebConfig 排除路径冲突。

## R（Result / 项目成果）

1. **功能完整性**：9 大模块全部开发完成，87+ 个 API 接口均可正常调用，前端页面与后端接口联调通过，覆盖完整的业务流程。
2. **安全性**：JWT 认证体系稳定运行，未登录用户无法执行任何写操作；密码采用 BCrypt 不可逆加密；敏感配置通过环境变量隔离，未进入 Git 仓库。
3. **AI 能力**：AI 对话助手、内容审核、帖子智能分类三大功能均通过测试，能够与阿里云通义大模型正常通信。
4. **文件存储**：图片上传至阿里云 OSS，返回的 URL 可直接在前端展示，支持头像、帖子图片、聊天图片等多场景。
5. **文档交付**：生成 364.9 KB 的《全模块功能说明.docx》，包含 12 章完整内容、9 张架构流程图、所有 Controller 源码，可直接作为项目技术文档使用。
6. **代码质量**：代码结构清晰，模块间耦合度低，Service 层接口与实现分离，DTO/VO/Entity 职责明确，SQL 写法规范（简单注解、复杂 XML），符合企业级开发标准。

# 智能校园综合服务平台

**独立开发 | Spring Boot · Spring AI Alibaba · MyBatis-Plus · JWT · Vue3 · 阿里云 OSS**

**[GitHub: lan-hash/Smart-Campus](https://github.com/lan-hash/Smart-Campus)**

**S（背景）**
独立负责一款面向高校师生的智能综合服务平台的从 0 到 1 全栈开发。平台以 Spring AI Alibaba 为核心差异化能力，涵盖用户管理、校园论坛、二手交易、代课服务、AI 助手等 9 大业务模块，后端采用 Spring Boot + MyBatis-Plus，前端 Vue3 + Element Plus。

**T（任务）**
1. 将 Spring AI Alibaba 的大模型能力（对话、审核、分类）落地到真实业务场景，解决校园平台的内容治理和用户体验问题；
2. 设计二手交易等复杂业务的状态流转机制，保证多表数据一致性；
3. 实现 9 大模块的领域拆分与解耦，后台管理需跨模块聚合数据而不产生循环依赖。

**A（行动）**

- **AI 能力落地**：基于 Spring AI Alibaba 接入通义大模型。对话功能通过 `chatClient.prompt(message).call().content()` 调用大模型，使用 UUID 作为 sessionId 管理多轮会话历史；内容审核通过结构化 prompt 要求 AI 返回 pass/block/review 关键词，解析后推断三档风险等级并落地审核记录；发帖智能分类在 `ForumController` 中直接注入 `ChatClient`，动态构建版块列表 prompt 获取推荐结果。**主动将 Spring AI Alibaba 从 M2 升级至 M5.1，独立解决 `String` 无法转换为 `Prompt` 的 API 断裂问题。**

- **复杂业务建模**：设计二手交易双重状态机——商品状态（在售 0 → 预留 1 → 已售 2）与交易状态（待确认 0 → 进行中 1 → 完成 2 / 取消 3）。在 `updateTransactionStatus` 中实现状态流转校验（已结束状态禁止修改），并通过 `LambdaUpdateWrapper.eq(status, 1)` 乐观锁实现交易状态与商品状态的多表联动更新（交易完成自动标记已售，交易取消恢复在售）。购买流程设置 4 道校验：商品存在性、在售状态、非自购、防重复购买。

- **模块化与解耦设计**：9 大模块按领域独立拆分，`AdminServiceImpl` 直接注入 7 个其他模块的 Mapper（User、ForumPost、Report、Confession、SecondhandProduct、CourseOrder、SystemNotice）实现跨模块数据聚合统计，避免通过 Service 层调用产生的循环依赖。点赞/收藏采用 toggle 模式（`isLiked` 查询 → 原子增删 + 计数同步）保证幂等性；评论树通过批量查询替代 N+1（先查顶级评论，再 `IN` 批量查子评论，`Stream.groupingBy` 组装树结构），固定 3 次查询替代原 2N+1 次。

**R（成果）**
- 独立完成 9 大业务模块、87+ RESTful API、27 张数据库表、完整 Vue3 前端，系统全链路联调通过，具备直接运行能力；
- AI 对话、内容审核、智能分类三大功能全部落地，可与阿里云通义大模型稳定通信；
- 二手交易状态机覆盖「发布 → 购买 → 确认 → 评价」完整业务闭环，数据一致性通过乐观锁保证；
- 后台管理数据面板一次性聚合 6 个模块的统计指标，响应时间 < 100ms。

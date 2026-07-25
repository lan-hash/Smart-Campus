-- ============================================================
-- 智能校园综合服务平台 数据库初始化脚本
-- 数据库: campus_platform
-- 字符集: utf8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS `campus_platform` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `campus_platform`;

-- ============================================================
-- 一、用户模块
-- ============================================================

-- 1. 用户表
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名/登录账号',
  `password` VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
  `nickname` VARCHAR(50) NOT NULL COMMENT '昵称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `gender` TINYINT DEFAULT 0 COMMENT '性别 0未知 1男 2女',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `campus` VARCHAR(100) DEFAULT NULL COMMENT '所在学校',
  `student_id` VARCHAR(50) DEFAULT NULL COMMENT '学号',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  `bio` VARCHAR(200) DEFAULT NULL COMMENT '个人简介',
  `role` TINYINT NOT NULL DEFAULT 0 COMMENT '角色 0学生 1管理员',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0正常 1禁用',
  `campus_verified` TINYINT NOT NULL DEFAULT 0 COMMENT '校园认证 0未认证 1已认证',
  `post_count` INT NOT NULL DEFAULT 0 COMMENT '发帖数',
  `follow_count` INT NOT NULL DEFAULT 0 COMMENT '关注数',
  `fans_count` INT NOT NULL DEFAULT 0 COMMENT '粉丝数',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_campus` (`campus`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 用户关注关系表
DROP TABLE IF EXISTS `user_follow`;
CREATE TABLE `user_follow` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `follower_id` BIGINT NOT NULL COMMENT '关注者ID',
  `following_id` BIGINT NOT NULL COMMENT '被关注者ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_follow` (`follower_id`, `following_id`),
  KEY `idx_following` (`following_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注关系表';

-- 3. 用户拉黑表
DROP TABLE IF EXISTS `user_block`;
CREATE TABLE `user_block` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '拉黑发起者',
  `blocked_id` BIGINT NOT NULL COMMENT '被拉黑者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_block` (`user_id`, `blocked_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户拉黑表';

-- ============================================================
-- 二、校园二手交易模块
-- ============================================================

-- 4. 二手商品分类表
DROP TABLE IF EXISTS `secondhand_category`;
CREATE TABLE `secondhand_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `icon` VARCHAR(255) DEFAULT NULL COMMENT '分类图标',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='二手商品分类表';

-- 5. 二手商品表
DROP TABLE IF EXISTS `secondhand_product`;
CREATE TABLE `secondhand_product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '发布者ID',
  `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
  `title` VARCHAR(100) NOT NULL COMMENT '商品标题',
  `description` TEXT COMMENT '商品描述',
  `price` DECIMAL(10,2) NOT NULL COMMENT '售价',
  `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
  `condition_level` TINYINT NOT NULL DEFAULT 5 COMMENT '新旧程度 1-10成新',
  `images` VARCHAR(1000) DEFAULT NULL COMMENT '图片URL(JSON数组)',
  `location` VARCHAR(100) DEFAULT NULL COMMENT '交易地点',
  `contact` VARCHAR(100) DEFAULT NULL COMMENT '联系方式',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0在售 1已售 2下架',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览量',
  `favorite_count` INT NOT NULL DEFAULT 0 COMMENT '收藏数',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='二手商品表';

-- 6. 二手商品收藏表
DROP TABLE IF EXISTS `secondhand_favorite`;
CREATE TABLE `secondhand_favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `product_id` BIGINT NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_fav` (`user_id`, `product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='二手商品收藏表';

-- 7. 二手交易记录表
DROP TABLE IF EXISTS `secondhand_transaction`;
CREATE TABLE `secondhand_transaction` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `buyer_id` BIGINT NOT NULL COMMENT '买家ID',
  `seller_id` BIGINT NOT NULL COMMENT '卖家ID',
  `price` DECIMAL(10,2) NOT NULL COMMENT '成交价',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0待确认 1交易中 2已完成 3已取消',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_buyer` (`buyer_id`),
  KEY `idx_seller` (`seller_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='二手交易记录表';

-- 8. 二手交易评价表
DROP TABLE IF EXISTS `secondhand_evaluation`;
CREATE TABLE `secondhand_evaluation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `transaction_id` BIGINT NOT NULL COMMENT '交易ID',
  `from_user_id` BIGINT NOT NULL COMMENT '评价人',
  `to_user_id` BIGINT NOT NULL COMMENT '被评价人',
  `score` TINYINT NOT NULL DEFAULT 5 COMMENT '评分 1-5',
  `content` VARCHAR(500) DEFAULT NULL COMMENT '评价内容',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_transaction` (`transaction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='二手交易评价表';

-- ============================================================
-- 三、校园代课服务模块
-- ============================================================

-- 9. 代课订单表
DROP TABLE IF EXISTS `course_order`;
CREATE TABLE `course_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '发布者ID(需求方)',
  `accept_user_id` BIGINT DEFAULT NULL COMMENT '接单者ID(代课方)',
  `course_name` VARCHAR(100) NOT NULL COMMENT '课程名称',
  `course_type` VARCHAR(50) DEFAULT NULL COMMENT '课程类型',
  `class_time` VARCHAR(200) NOT NULL COMMENT '上课时间',
  `location` VARCHAR(100) DEFAULT NULL COMMENT '上课地点',
  `salary` DECIMAL(10,2) NOT NULL COMMENT '薪资',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '需求描述',
  `contact` VARCHAR(100) DEFAULT NULL COMMENT '联系方式',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0待接单 1进行中 2已完成 3已取消',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览量',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代课订单表';

-- 10. 代课评价表
DROP TABLE IF EXISTS `course_evaluation`;
CREATE TABLE `course_evaluation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `from_user_id` BIGINT NOT NULL COMMENT '评价人',
  `to_user_id` BIGINT NOT NULL COMMENT '被评价人',
  `score` TINYINT NOT NULL DEFAULT 5 COMMENT '评分 1-5',
  `content` VARCHAR(500) DEFAULT NULL COMMENT '评价内容',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代课评价表';

-- ============================================================
-- 四、论坛模块(学习论坛 + 游戏论坛)
-- ============================================================

-- 11. 论坛版块表
DROP TABLE IF EXISTS `forum_category`;
CREATE TABLE `forum_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL COMMENT '版块名称',
  `type` TINYINT NOT NULL COMMENT '类型 0学习论坛 1游戏论坛',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '版块描述',
  `icon` VARCHAR(255) DEFAULT NULL COMMENT '版块图标',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛版块表';

-- 12. 论坛帖子表
DROP TABLE IF EXISTS `forum_post`;
CREATE TABLE `forum_post` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '发布者ID',
  `category_id` BIGINT NOT NULL COMMENT '版块ID',
  `title` VARCHAR(150) NOT NULL COMMENT '标题',
  `content` LONGTEXT COMMENT '正文内容',
  `images` VARCHAR(2000) DEFAULT NULL COMMENT '图片URL(JSON数组)',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览量',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` INT NOT NULL DEFAULT 0 COMMENT '评论数',
  `collect_count` INT NOT NULL DEFAULT 0 COMMENT '收藏数',
  `is_top` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶',
  `is_essence` TINYINT NOT NULL DEFAULT 0 COMMENT '是否加精',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0正常 1审核中 2违规',
  `ai_category` VARCHAR(50) DEFAULT NULL COMMENT 'AI智能分类标签',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛帖子表';

-- 13. 论坛评论表
DROP TABLE IF EXISTS `forum_comment`;
CREATE TABLE `forum_comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `post_id` BIGINT NOT NULL COMMENT '帖子ID',
  `user_id` BIGINT NOT NULL COMMENT '评论人ID',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父评论ID(0为顶级评论)',
  `reply_to_user_id` BIGINT DEFAULT NULL COMMENT '回复目标用户ID',
  `content` VARCHAR(1000) NOT NULL COMMENT '评论内容',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_post` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛评论表';

-- 14. 论坛点赞表
DROP TABLE IF EXISTS `forum_like`;
CREATE TABLE `forum_like` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `target_id` BIGINT NOT NULL COMMENT '目标ID',
  `target_type` TINYINT NOT NULL COMMENT '类型 0帖子 1评论',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_like` (`user_id`, `target_id`, `target_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛点赞表';

-- 15. 论坛收藏表
DROP TABLE IF EXISTS `forum_collect`;
CREATE TABLE `forum_collect` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `post_id` BIGINT NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_collect` (`user_id`, `post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛收藏表';

-- 16. 论坛标签表
DROP TABLE IF EXISTS `forum_tag`;
CREATE TABLE `forum_tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(30) NOT NULL COMMENT '标签名',
  `category_id` BIGINT DEFAULT NULL COMMENT '所属版块',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛标签表';

-- 17. 帖子标签关联表
DROP TABLE IF EXISTS `forum_post_tag`;
CREATE TABLE `forum_post_tag` (
  `post_id` BIGINT NOT NULL,
  `tag_id` BIGINT NOT NULL,
  PRIMARY KEY (`post_id`, `tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子标签关联表';

-- ============================================================
-- 五、匿名表白墙模块
-- ============================================================

-- 18. 表白墙表
DROP TABLE IF EXISTS `confession`;
CREATE TABLE `confession` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '发布者ID',
  `content` VARCHAR(1000) NOT NULL COMMENT '表白内容',
  `images` VARCHAR(2000) DEFAULT NULL COMMENT '图片URL(JSON数组)',
  `is_anonymous` TINYINT NOT NULL DEFAULT 1 COMMENT '是否匿名 0实名 1匿名',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` INT NOT NULL DEFAULT 0 COMMENT '评论数',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0正常 1审核中 2违规',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='表白墙表';

-- 19. 表白墙评论表
DROP TABLE IF EXISTS `confession_comment`;
CREATE TABLE `confession_comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `confession_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `content` VARCHAR(500) NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_confession` (`confession_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='表白墙评论表';

-- 20. 表白墙点赞表
DROP TABLE IF EXISTS `confession_like`;
CREATE TABLE `confession_like` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `confession_id` BIGINT NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_like` (`user_id`, `confession_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='表白墙点赞表';

-- ============================================================
-- 六、消息通知模块
-- ============================================================

-- 21. 私聊消息表
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `from_user_id` BIGINT NOT NULL COMMENT '发送者',
  `to_user_id` BIGINT NOT NULL COMMENT '接收者',
  `content` VARCHAR(1000) NOT NULL COMMENT '消息内容',
  `type` TINYINT NOT NULL DEFAULT 0 COMMENT '类型 0文字 1图片',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_to_user` (`to_user_id`, `is_read`),
  KEY `idx_pair` (`from_user_id`, `to_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='私聊消息表';

-- 22. 通知表
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '接收用户',
  `type` TINYINT NOT NULL COMMENT '类型 0点赞 1评论 2关注 3系统 4交易 5代课',
  `title` VARCHAR(100) DEFAULT NULL COMMENT '标题',
  `content` VARCHAR(500) DEFAULT NULL COMMENT '内容',
  `target_id` BIGINT DEFAULT NULL COMMENT '目标对象ID',
  `from_user_id` BIGINT DEFAULT NULL COMMENT '来源用户',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- 23. 系统公告表
DROP TABLE IF EXISTS `system_notice`;
CREATE TABLE `system_notice` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(150) NOT NULL COMMENT '公告标题',
  `content` TEXT COMMENT '公告内容',
  `admin_id` BIGINT NOT NULL COMMENT '发布管理员',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统公告表';

-- ============================================================
-- 七、Spring AI Alibaba 智能核心模块
-- ============================================================

-- 24. AI对话记录表
DROP TABLE IF EXISTS `ai_chat_record`;
CREATE TABLE `ai_chat_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `session_id` VARCHAR(64) NOT NULL COMMENT '会话ID',
  `role` VARCHAR(20) NOT NULL COMMENT '角色 user/assistant',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_session` (`session_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话记录表';

-- 25. AI内容审核记录表
DROP TABLE IF EXISTS `ai_content_review`;
CREATE TABLE `ai_content_review` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `target_type` TINYINT NOT NULL COMMENT '目标类型 0帖子 1表白 2商品 3评论',
  `target_id` BIGINT NOT NULL COMMENT '目标ID',
  `content` TEXT COMMENT '送审内容',
  `result` VARCHAR(20) DEFAULT NULL COMMENT '审核结果 pass/review/block',
  `reason` VARCHAR(500) DEFAULT NULL COMMENT '审核理由',
  `risk_level` TINYINT DEFAULT 0 COMMENT '风险等级 0无 1低 2中 3高',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI内容审核记录表';

-- ============================================================
-- 八、后台管理模块
-- ============================================================

-- 26. 举报统一表
DROP TABLE IF EXISTS `report`;
CREATE TABLE `report` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `reporter_id` BIGINT NOT NULL COMMENT '举报人ID',
  `target_type` TINYINT NOT NULL COMMENT '目标类型 0商品 1帖子 2表白 3代课 4用户',
  `target_id` BIGINT NOT NULL COMMENT '目标ID',
  `reason` VARCHAR(100) NOT NULL COMMENT '举报原因',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '详细描述',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0待处理 1已处理 2已驳回',
  `handler_id` BIGINT DEFAULT NULL COMMENT '处理人',
  `handle_remark` VARCHAR(500) DEFAULT NULL COMMENT '处理备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `handle_time` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报统一表';

-- 27. 操作日志表
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `admin_id` BIGINT NOT NULL COMMENT '操作管理员',
  `operation` VARCHAR(100) NOT NULL COMMENT '操作描述',
  `method` VARCHAR(200) DEFAULT NULL COMMENT '请求方法',
  `params` TEXT COMMENT '请求参数',
  `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ============================================================
-- 九、初始化数据
-- ============================================================

-- 默认管理员账号 (密码: admin123, BCrypt加密)
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `role`, `status`, `campus_verified`)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7ZT6/jLKe', '系统管理员', 1, 0, 1);

-- 二手商品分类
INSERT INTO `secondhand_category` (`name`, `icon`, `sort`) VALUES
('教材书籍', 'Book', 1),
('数码电子', 'Monitor', 2),
('生活用品', 'Box', 3),
('服饰鞋包', 'ShoppingBag', 4),
('运动器材', 'Basketball', 5),
('其他', 'More', 6);

-- 论坛版块(学习)
INSERT INTO `forum_category` (`name`, `type`, `description`, `icon`, `sort`) VALUES
('学习心得', 0, '分享学习经验与方法', 'Reading', 1),
('课程答疑', 0, '课程问题互助解答', 'ChatDotRound', 2),
('考研考证', 0, '考研考证经验分享', 'Trophy', 3),
('资料分享', 0, '学习资料共享', 'FolderOpened', 4),
('组队学习', 0, '寻找学习搭子', 'UserFilled', 5);

-- 论坛版块(游戏)
INSERT INTO `forum_category` (`name`, `type`, `description`, `icon`, `sort`) VALUES
('游戏攻略', 1, '游戏通关攻略分享', 'Aim', 1),
('开黑邀约', 1, '组队开黑招募', 'UserFilled', 2),
('赛事讨论', 1, '电竞赛事交流', 'Trophy', 3),
('吐槽交流', 1, '游戏吐槽闲聊', 'ChatLineSquare', 4);

-- 系统公告
INSERT INTO `system_notice` (`title`, `content`, `admin_id`) VALUES
('欢迎使用智能校园综合服务平台', '本平台集二手交易、代课服务、学习论坛、游戏社区、表白墙及AI智能服务于一体，欢迎体验！', 1);

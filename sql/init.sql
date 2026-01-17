-- ============================================
-- 化妆师预约平台数据库脚本
-- 数据库名: beauty
-- 字段命名: 驼峰式
-- ============================================

CREATE DATABASE IF NOT EXISTS beauty DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE beauty;

-- -------------------------------------------
-- 1. 用户表 (user)
-- -------------------------------------------
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(加密)',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `role` TINYINT NOT NULL DEFAULT 0 COMMENT '角色: 0用户 1化妆师 2管理员',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0禁用 1正常',
    `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDeleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_phone` (`phone`),
    KEY `idx_role` (`role`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表';

-- -------------------------------------------
-- 2. 化妆师表 (artist)
-- -------------------------------------------
CREATE TABLE `artist` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `userId` BIGINT NOT NULL COMMENT '关联用户ID',
    `realName` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `title` VARCHAR(100) DEFAULT NULL COMMENT '职称/头衔',
    `description` TEXT COMMENT '个人简介',
    `experienceYears` INT DEFAULT 0 COMMENT '从业年限',
    `specialties` VARCHAR(255) DEFAULT NULL COMMENT '擅长领域(JSON数组)',
    `portfolioImages` TEXT COMMENT '作品集图片(JSON数组)',
    `rating` DECIMAL(2, 1) DEFAULT 5.0 COMMENT '平均评分',
    `orderCount` INT DEFAULT 0 COMMENT '接单数量',
    `basePrice` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '起步价格',
    `isRecommend` TINYINT DEFAULT 0 COMMENT '是否推荐: 0否 1是',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0休息 1可接单',
    `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDeleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_userId` (`userId`),
    KEY `idx_status` (`status`),
    KEY `idx_rating` (`rating`),
    KEY `idx_isRecommend` (`isRecommend`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '化妆师表';

-- -------------------------------------------
-- 3. 服务分类表 (serviceCategory)
-- -------------------------------------------
CREATE TABLE `serviceCategory` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `icon` VARCHAR(255) DEFAULT NULL COMMENT '分类图标',
    `sortOrder` INT DEFAULT 0 COMMENT '排序权重',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDeleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_sortOrder` (`sortOrder`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '服务分类表';

-- -------------------------------------------
-- 4. 服务套餐表 (service)
-- -------------------------------------------
CREATE TABLE `service` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `artistId` BIGINT NOT NULL COMMENT '化妆师ID',
    `categoryId` BIGINT DEFAULT NULL COMMENT '分类ID',
    `name` VARCHAR(100) NOT NULL COMMENT '套餐名称',
    `description` TEXT COMMENT '套餐描述',
    `price` DECIMAL(10, 2) NOT NULL COMMENT '价格',
    `duration` INT DEFAULT 60 COMMENT '服务时长(分钟)',
    `coverImage` VARCHAR(255) DEFAULT NULL COMMENT '封面图片',
    `images` TEXT COMMENT '详情图片(JSON数组)',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0下架 1上架',
    `sortOrder` INT DEFAULT 0 COMMENT '排序权重',
    `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDeleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_artistId` (`artistId`),
    KEY `idx_categoryId` (`categoryId`),
    KEY `idx_status` (`status`),
    KEY `idx_sortOrder` (`sortOrder`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '服务套餐表';

-- -------------------------------------------
-- 5. 预约订单表 (appointment)
-- -------------------------------------------
CREATE TABLE `appointment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `orderNo` VARCHAR(32) NOT NULL COMMENT '订单编号',
    `userId` BIGINT NOT NULL COMMENT '用户ID',
    `artistId` BIGINT NOT NULL COMMENT '化妆师ID',
    `serviceId` BIGINT NOT NULL COMMENT '服务套餐ID',
    `appointmentDate` DATE NOT NULL COMMENT '预约日期',
    `appointmentTime` TIME NOT NULL COMMENT '预约时间',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '服务地址',
    `contactName` VARCHAR(50) DEFAULT NULL COMMENT '联系人姓名',
    `contactPhone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `remark` TEXT COMMENT '备注',
    `totalAmount` DECIMAL(10, 2) NOT NULL COMMENT '总金额',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0待支付 1待确认 2已确认 3服务中 4已完成 5已取消 6已拒绝',
    `cancelReason` VARCHAR(255) DEFAULT NULL COMMENT '取消/拒绝原因',
    `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDeleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_orderNo` (`orderNo`),
    KEY `idx_userId` (`userId`),
    KEY `idx_artistId` (`artistId`),
    KEY `idx_serviceId` (`serviceId`),
    KEY `idx_status` (`status`),
    KEY `idx_appointmentDate` (`appointmentDate`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '预约订单表';

-- -------------------------------------------
-- 6. 支付记录表 (payment)
-- -------------------------------------------
CREATE TABLE `payment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `appointmentId` BIGINT NOT NULL COMMENT '预约订单ID',
    `paymentNo` VARCHAR(64) NOT NULL COMMENT '支付流水号',
    `tradeNo` VARCHAR(64) DEFAULT NULL COMMENT '第三方交易号',
    `amount` DECIMAL(10, 2) NOT NULL COMMENT '支付金额',
    `paymentMethod` TINYINT NOT NULL DEFAULT 3 COMMENT '支付方式: 1微信 2支付宝 3模拟支付',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0待支付 1已支付 2已退款',
    `paidAt` DATETIME DEFAULT NULL COMMENT '支付时间',
    `refundAt` DATETIME DEFAULT NULL COMMENT '退款时间',
    `refundReason` VARCHAR(255) DEFAULT NULL COMMENT '退款原因',
    `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDeleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_paymentNo` (`paymentNo`),
    KEY `idx_appointmentId` (`appointmentId`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '支付记录表';

-- -------------------------------------------
-- 7. 收藏表 (favorite)
-- -------------------------------------------
CREATE TABLE `favorite` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `userId` BIGINT NOT NULL COMMENT '用户ID',
    `targetType` TINYINT NOT NULL COMMENT '收藏类型: 1化妆师 2服务套餐',
    `targetId` BIGINT NOT NULL COMMENT '目标ID',
    `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `isDeleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_target` (
        `userId`,
        `targetType`,
        `targetId`
    ),
    KEY `idx_userId` (`userId`),
    KEY `idx_targetType` (`targetType`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '收藏表';

-- -------------------------------------------
-- 8. 评价表 (review)
-- -------------------------------------------
CREATE TABLE `review` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `appointmentId` BIGINT NOT NULL COMMENT '预约订单ID',
    `userId` BIGINT NOT NULL COMMENT '用户ID',
    `artistId` BIGINT NOT NULL COMMENT '化妆师ID',
    `rating` TINYINT NOT NULL COMMENT '评分(1-5)',
    `content` TEXT COMMENT '评价内容',
    `images` TEXT COMMENT '评价图片(JSON数组)',
    `reply` TEXT COMMENT '化妆师回复',
    `replyTime` DATETIME DEFAULT NULL COMMENT '回复时间',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0隐藏 1显示',
    `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDeleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_appointmentId` (`appointmentId`),
    KEY `idx_userId` (`userId`),
    KEY `idx_artistId` (`artistId`),
    KEY `idx_rating` (`rating`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '评价表';

-- -------------------------------------------
-- 9. 化妆师入驻申请表 (artistApplication)
-- -------------------------------------------
CREATE TABLE `artistApplication` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `userId` BIGINT NOT NULL COMMENT '申请用户ID',
    `realName` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `idCard` VARCHAR(18) DEFAULT NULL COMMENT '身份证号',
    `phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
    `experienceYears` INT DEFAULT 0 COMMENT '从业年限',
    `specialties` VARCHAR(255) DEFAULT NULL COMMENT '擅长领域',
    `certificateImages` TEXT COMMENT '资质证书图片(JSON数组)',
    `portfolioImages` TEXT COMMENT '作品集图片(JSON数组)',
    `introduction` TEXT COMMENT '个人介绍',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0待审核 1通过 2拒绝',
    `rejectReason` VARCHAR(255) DEFAULT NULL COMMENT '拒绝原因',
    `reviewedBy` BIGINT DEFAULT NULL COMMENT '审核人ID',
    `reviewedAt` DATETIME DEFAULT NULL COMMENT '审核时间',
    `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDeleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_userId` (`userId`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '化妆师入驻申请表';

-- -------------------------------------------
-- 10. 轮播图表 (banner)
-- -------------------------------------------
CREATE TABLE `banner` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title` VARCHAR(100) DEFAULT NULL COMMENT '标题',
    `imageUrl` VARCHAR(255) NOT NULL COMMENT '图片地址',
    `linkUrl` VARCHAR(255) DEFAULT NULL COMMENT '跳转链接',
    `sortOrder` INT DEFAULT 0 COMMENT '排序权重',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDeleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_sortOrder` (`sortOrder`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '轮播图表';

-- -------------------------------------------
-- 11. 系统设置表 (systemSetting)
-- -------------------------------------------
CREATE TABLE `systemSetting` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `settingKey` VARCHAR(50) NOT NULL COMMENT '设置键',
    `settingValue` TEXT COMMENT '设置值',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '说明',
    `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_settingKey` (`settingKey`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统设置表';

-- -------------------------------------------
-- 初始化数据: 管理员账号
-- 密码: admin123 (需要在代码中用加密后的密码替换)
-- -------------------------------------------
INSERT INTO
    `user` (
        `username`,
        `password`,
        `nickname`,
        `role`,
        `status`
    )
VALUES (
        'admin',
        '123456',
        '系统管理员',
        2,
        1
    );

-- -------------------------------------------
-- 初始化数据: 服务分类
-- -------------------------------------------
INSERT INTO
    `serviceCategory` (
        `name`,
        `icon`,
        `sortOrder`,
        `status`
    )
VALUES ('新娘妆', NULL, 1, 1),
    ('日常妆', NULL, 2, 1),
    ('晚宴妆', NULL, 3, 1),
    ('舞台妆', NULL, 4, 1),
    ('写真妆', NULL, 5, 1),
    ('特效妆', NULL, 6, 1);
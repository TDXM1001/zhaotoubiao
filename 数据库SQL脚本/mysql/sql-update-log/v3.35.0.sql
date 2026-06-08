-- =========================================================
-- v3.35.0 招投标 P1 MVP-1 待开标前闭环增量脚本
-- 目标：新增招标文件版本、投标主记录、投标版本、业务附件关联表，以及内部主系统菜单和功能点
-- 说明：
--   1. 新模块只承载 P1 待开标前闭环，不实现开标、评标、定标、归档业务
--   2. 状态类字段继续由代码枚举维护，不下发可运营字典
--   3. 文件本体继续复用现有文件模块，本脚本只新增招投标业务对象和文件的关联关系
--   4. 菜单继续挂在正式一级菜单 `招投标管理` 下，组件路径按 `src/views/system/bid/**` 组织
-- 编码：utf8mb4
-- 菜单 ID 规划：760-775
-- 字典编码规划：
--   BID_TENDER_VERSION_TYPE
--   BID_ATTACHMENT_FILE_CATEGORY
-- =========================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

START TRANSACTION;

-- ----------------------------
-- 1. 业务表：招标文件版本
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_bid_tender_version` (
  `tender_version_id` bigint NOT NULL AUTO_INCREMENT COMMENT '招标文件版本ID',
  `project_id` bigint NOT NULL COMMENT '所属项目ID',
  `lot_id` bigint NOT NULL COMMENT '所属标段ID',
  `version_no` int NOT NULL COMMENT '版本号',
  `version_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '版本类型，取值于字典 BID_TENDER_VERSION_TYPE',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'DRAFT' COMMENT '招标文件状态，代码枚举维护',
  `current_flag` tinyint(1) DEFAULT NULL COMMENT '是否当前有效版本 1-是 NULL-否；非当前版本必须保持 NULL',
  `parent_version_id` bigint DEFAULT NULL COMMENT '父版本ID，用于澄清或更正版本链',
  `effective_time` datetime DEFAULT NULL COMMENT '生效时间',
  `publish_user_id` bigint DEFAULT NULL COMMENT '发布人',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `summary` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '版本摘要',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `create_user_id` bigint NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`tender_version_id`) USING BTREE,
  UNIQUE KEY `uk_lot_version_no` (`lot_id`, `version_no`) USING BTREE,
  UNIQUE KEY `uk_lot_current_active` (`lot_id`, `current_flag`, `deleted_flag`) USING BTREE,
  KEY `idx_project_lot_status` (`project_id`, `lot_id`, `status`) USING BTREE,
  KEY `idx_parent_version_id` (`parent_version_id`) USING BTREE,
  KEY `idx_publish_time` (`publish_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='招投标招标文件版本表' ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- 2. 业务表：投标主记录
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_bid_submission` (
  `submission_id` bigint NOT NULL AUTO_INCREMENT COMMENT '投标ID',
  `project_id` bigint NOT NULL COMMENT '所属项目ID',
  `lot_id` bigint NOT NULL COMMENT '所属标段ID',
  `registration_id` bigint NOT NULL COMMENT '报名ID',
  `supplier_enterprise_id` bigint DEFAULT NULL COMMENT '供应商企业ID，预留正式供应商主体关联',
  `supplier_name_snapshot` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '供应商名称快照',
  `supplier_credit_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '统一社会信用代码',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'QUALIFIED' COMMENT '投标状态，代码枚举维护',
  `latest_version_no` int NOT NULL DEFAULT 0 COMMENT '最新投标版本号',
  `latest_submit_time` datetime DEFAULT NULL COMMENT '最近提交时间',
  `receipt_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '投标回执号',
  `withdraw_time` datetime DEFAULT NULL COMMENT '撤回时间',
  `withdraw_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '撤回原因',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `create_user_id` bigint NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`submission_id`) USING BTREE,
  UNIQUE KEY `uk_lot_supplier_credit` (`lot_id`, `supplier_credit_code`) USING BTREE,
  UNIQUE KEY `uk_registration_id` (`registration_id`) USING BTREE,
  KEY `idx_project_lot_status` (`project_id`, `lot_id`, `status`) USING BTREE,
  KEY `idx_supplier_enterprise_id` (`supplier_enterprise_id`) USING BTREE,
  KEY `idx_latest_submit_time` (`latest_submit_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='招投标投标主记录表' ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- 3. 业务表：投标版本
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_bid_submission_version` (
  `submission_version_id` bigint NOT NULL AUTO_INCREMENT COMMENT '投标版本ID',
  `submission_id` bigint NOT NULL COMMENT '投标主记录ID',
  `version_no` int NOT NULL COMMENT '版本号',
  `receipt_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '本次提交回执号',
  `submit_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `submit_user_id` bigint DEFAULT NULL COMMENT '提交人',
  `price_amount` decimal(18, 2) DEFAULT NULL COMMENT '投标报价金额',
  `contact_name` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系人电话',
  `file_manifest_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '投标文件清单JSON快照',
  `is_effective` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否有效版本 0-否 1-是',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
  `create_user_id` bigint NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`submission_version_id`) USING BTREE,
  UNIQUE KEY `uk_submission_version_no` (`submission_id`, `version_no`) USING BTREE,
  KEY `idx_submission_effective` (`submission_id`, `is_effective`) USING BTREE,
  KEY `idx_submit_time` (`submit_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='招投标投标版本表' ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- 4. 业务表：招投标附件关联
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_bid_attachment` (
  `attachment_id` bigint NOT NULL AUTO_INCREMENT COMMENT '附件关联ID',
  `business_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '业务类型，例如 TENDER_VERSION、SUBMISSION_VERSION',
  `business_id` bigint NOT NULL COMMENT '业务对象ID',
  `project_id` bigint DEFAULT NULL COMMENT '项目ID',
  `lot_id` bigint DEFAULT NULL COMMENT '标段ID',
  `file_id` bigint NOT NULL COMMENT '文件ID，引用现有文件模块',
  `file_category` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件分类，取值于字典 BID_ATTACHMENT_FILE_CATEGORY',
  `version_no` int DEFAULT NULL COMMENT '业务版本号',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `is_main` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否主文件 0-否 1-是',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
  `create_user_id` bigint NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`attachment_id`) USING BTREE,
  KEY `idx_business_type_id` (`business_type`, `business_id`) USING BTREE,
  KEY `idx_business_category_sort` (`business_type`, `business_id`, `file_category`, `deleted_flag`, `sort_no`) USING BTREE,
  KEY `idx_project_lot` (`project_id`, `lot_id`) USING BTREE,
  KEY `idx_file_id` (`file_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='招投标业务附件关联表' ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- 5. 业务字典定义
-- ----------------------------
INSERT INTO `t_dict` (`dict_name`, `dict_code`, `remark`, `disabled_flag`)
VALUES ('招标文件版本类型', 'BID_TENDER_VERSION_TYPE', '招标文件、公告、澄清和更正版本类型', 0)
ON DUPLICATE KEY UPDATE
  `dict_name` = VALUES(`dict_name`),
  `remark` = VALUES(`remark`),
  `disabled_flag` = VALUES(`disabled_flag`);

INSERT INTO `t_dict` (`dict_name`, `dict_code`, `remark`, `disabled_flag`)
VALUES ('招投标附件分类', 'BID_ATTACHMENT_FILE_CATEGORY', '招投标业务附件分类，文件本体仍由文件模块维护', 0)
ON DUPLICATE KEY UPDATE
  `dict_name` = VALUES(`dict_name`),
  `remark` = VALUES(`remark`),
  `disabled_flag` = VALUES(`disabled_flag`);

-- ----------------------------
-- 6. 招标文件版本类型字典项
-- ----------------------------
INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, 'TENDER_MAIN', '招标文件', 'primary', '招标文件主版本', 400, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'BID_TENDER_VERSION_TYPE'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = 'TENDER_MAIN'
  );

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, 'ANNOUNCEMENT', '公告', 'success', '公告版本', 300, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'BID_TENDER_VERSION_TYPE'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = 'ANNOUNCEMENT'
  );

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, 'CLARIFICATION', '澄清', 'warning', '澄清版本', 200, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'BID_TENDER_VERSION_TYPE'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = 'CLARIFICATION'
  );

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, 'CORRECTION', '更正', 'danger', '更正版本', 100, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'BID_TENDER_VERSION_TYPE'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = 'CORRECTION'
  );

-- ----------------------------
-- 7. 招投标附件分类字典项
-- ----------------------------
INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, 'TENDER_MAIN', '招标文件正文', 'primary', '招标文件版本主附件', 600, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'BID_ATTACHMENT_FILE_CATEGORY'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = 'TENDER_MAIN'
  );

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, 'TENDER_ANNOUNCEMENT', '招标公告', 'success', '招标公告附件', 500, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'BID_ATTACHMENT_FILE_CATEGORY'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = 'TENDER_ANNOUNCEMENT'
  );

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, 'TENDER_CLARIFICATION', '澄清文件', 'warning', '澄清或更正附件', 400, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'BID_ATTACHMENT_FILE_CATEGORY'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = 'TENDER_CLARIFICATION'
  );

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, 'SUBMISSION_MAIN', '投标文件正文', 'primary', '投标提交主附件', 300, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'BID_ATTACHMENT_FILE_CATEGORY'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = 'SUBMISSION_MAIN'
  );

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, 'SUBMISSION_PRICE', '报价文件', 'danger', '投标报价附件', 200, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'BID_ATTACHMENT_FILE_CATEGORY'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = 'SUBMISSION_PRICE'
  );

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, 'OTHER', '其他附件', 'info', '其他招投标业务附件', 100, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'BID_ATTACHMENT_FILE_CATEGORY'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = 'OTHER'
  );

-- ----------------------------
-- 8. 主系统菜单、隐藏页面与功能点
-- ----------------------------
INSERT INTO `t_menu`
(`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `update_user_id`)
VALUES
(760, '招标文件', 2, 731, 4, '/system/bid/tender/list', '/system/bid/tender/tender-list.vue', NULL, NULL, NULL, 'lucide:file-text', NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(761, '招标文件新增', 2, 760, 11, '/system/bid/tender/create', '/system/bid/tender/tender-form.vue', NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, 0, 0, 0, 1, 1),
(762, '招标文件详情', 2, 760, 12, '/system/bid/tender/detail', '/system/bid/tender/tender-detail.vue', NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, 0, 0, 0, 1, 1),
(763, '查询招标文件', 3, 760, 1, NULL, NULL, 1, 'bid:tender:query', 'bid:tender:query', NULL, 760, 0, NULL, 0, 1, 0, 0, 1, 1),
(764, '新增招标文件', 3, 760, 2, NULL, NULL, 1, 'bid:tender:create', 'bid:tender:create', NULL, 760, 0, NULL, 0, 1, 0, 0, 1, 1),
(765, '编辑招标文件', 3, 760, 3, NULL, NULL, 1, 'bid:tender:update', 'bid:tender:update', NULL, 760, 0, NULL, 0, 1, 0, 0, 1, 1),
(766, '发布招标文件', 3, 760, 4, NULL, NULL, 1, 'bid:tender:publish-tender', 'bid:tender:publish-tender', NULL, 760, 0, NULL, 0, 1, 0, 0, 1, 1),
(767, '澄清招标文件', 3, 760, 5, NULL, NULL, 1, 'bid:tender:clarify-tender', 'bid:tender:clarify-tender', NULL, 760, 0, NULL, 0, 1, 0, 0, 1, 1),
(768, '撤回招标文件', 3, 760, 6, NULL, NULL, 1, 'bid:tender:withdraw-tender', 'bid:tender:withdraw-tender', NULL, 760, 0, NULL, 0, 1, 0, 0, 1, 1),
(769, '投标管理', 2, 731, 5, '/system/bid/submission/list', '/system/bid/submission/submission-list.vue', NULL, NULL, NULL, 'lucide:send', NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(770, '投标详情', 2, 769, 11, '/system/bid/submission/detail', '/system/bid/submission/submission-detail.vue', NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, 0, 0, 0, 1, 1),
(771, '查询投标', 3, 769, 1, NULL, NULL, 1, 'bid:submission:query', 'bid:submission:query', NULL, 769, 0, NULL, 0, 1, 0, 0, 1, 1),
(772, '创建投标记录', 3, 769, 2, NULL, NULL, 1, 'bid:submission:create', 'bid:submission:create', NULL, 769, 0, NULL, 0, 1, 0, 0, 1, 1),
(773, '提交投标', 3, 769, 3, NULL, NULL, 1, 'bid:submission:submit-bid', 'bid:submission:submit-bid', NULL, 769, 0, NULL, 0, 1, 0, 0, 1, 1),
(774, '撤回投标', 3, 769, 4, NULL, NULL, 1, 'bid:submission:withdraw-bid', 'bid:submission:withdraw-bid', NULL, 769, 0, NULL, 0, 1, 0, 0, 1, 1),
(775, '下载投标文件', 3, 769, 5, NULL, NULL, 1, 'bid:submission:download', 'bid:submission:download', NULL, 769, 0, NULL, 0, 1, 0, 0, 1, 1),
(776, '供应商门户', 2, 731, 6, '/bid-portal/project/list', '/bid-portal/project/portal-project-list.vue', NULL, NULL, NULL, 'lucide:building-2', NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(777, '门户项目详情', 2, 776, 11, '/bid-portal/project/detail', '/bid-portal/project/portal-project-detail.vue', NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, 0, 0, 0, 1, 1),
(778, '门户投标表单', 2, 776, 12, '/bid-portal/submission/form', '/bid-portal/submission/portal-submission-form.vue', NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, 0, 0, 0, 1, 1)
ON DUPLICATE KEY UPDATE
  `menu_name` = VALUES(`menu_name`),
  `menu_type` = VALUES(`menu_type`),
  `parent_id` = VALUES(`parent_id`),
  `sort` = VALUES(`sort`),
  `path` = VALUES(`path`),
  `component` = VALUES(`component`),
  `perms_type` = VALUES(`perms_type`),
  `api_perms` = VALUES(`api_perms`),
  `web_perms` = VALUES(`web_perms`),
  `icon` = VALUES(`icon`),
  `context_menu_id` = VALUES(`context_menu_id`),
  `frame_flag` = VALUES(`frame_flag`),
  `frame_url` = VALUES(`frame_url`),
  `cache_flag` = VALUES(`cache_flag`),
  `visible_flag` = VALUES(`visible_flag`),
  `disabled_flag` = VALUES(`disabled_flag`),
  `deleted_flag` = VALUES(`deleted_flag`),
  `create_user_id` = VALUES(`create_user_id`),
  `update_user_id` = VALUES(`update_user_id`);

-- ----------------------------
-- 9. 默认管理员角色授权
-- ----------------------------
INSERT INTO `t_role_menu` (`role_id`, `menu_id`)
SELECT 1, tmp.`menu_id`
FROM (
    SELECT 760 AS `menu_id`
    UNION ALL SELECT 761
    UNION ALL SELECT 762
    UNION ALL SELECT 763
    UNION ALL SELECT 764
    UNION ALL SELECT 765
    UNION ALL SELECT 766
    UNION ALL SELECT 767
    UNION ALL SELECT 768
    UNION ALL SELECT 769
    UNION ALL SELECT 770
    UNION ALL SELECT 771
    UNION ALL SELECT 772
    UNION ALL SELECT 773
    UNION ALL SELECT 774
    UNION ALL SELECT 775
    UNION ALL SELECT 776
    UNION ALL SELECT 777
    UNION ALL SELECT 778
) tmp
WHERE NOT EXISTS (
    SELECT 1
    FROM `t_role_menu` rm
    WHERE rm.`role_id` = 1
      AND rm.`menu_id` = tmp.`menu_id`
);

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================================
-- v3.34.1 招投标供应商报名管理增量脚本
-- 目标：新增供应商报名表、报名方式字典、正式菜单、功能点和管理员授权
-- 说明：
--   1. 供应商主体一期不依赖 Demo 企业模块，报名表先保存企业名称、统一社会信用代码和联系人快照
--   2. 菜单继续挂在正式一级菜单 `招投标管理` 下，不扩展 Demo、OA、Goods、Category 目录
--   3. 新增/详情页作为隐藏二级菜单，便于复杂页面保持主系统菜单高亮
-- 编码：utf8mb4
-- 菜单 ID 规划：752-759
-- 字典编码规划：
--   BID_REGISTRATION_TYPE
-- =========================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

START TRANSACTION;

-- ----------------------------
-- 1. 业务表：供应商报名
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_bid_registration` (
  `registration_id` bigint NOT NULL AUTO_INCREMENT COMMENT '报名ID',
  `project_id` bigint NOT NULL COMMENT '所属项目ID',
  `lot_id` bigint NOT NULL COMMENT '所属标段ID',
  `supplier_enterprise_id` bigint DEFAULT NULL COMMENT '供应商企业ID，预留正式供应商主体关联',
  `supplier_name_snapshot` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '供应商名称快照',
  `supplier_credit_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '统一社会信用代码',
  `contact_name` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系人电话',
  `contact_email` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系人邮箱',
  `registration_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '报名方式，取值于字典 BID_REGISTRATION_TYPE',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'SUBMITTED' COMMENT '报名状态，代码枚举维护',
  `submit_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报名提交时间',
  `qualified_result` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '资格审核结果',
  `qualified_time` datetime DEFAULT NULL COMMENT '资格审核时间',
  `qualified_by` bigint DEFAULT NULL COMMENT '资格审核人',
  `reject_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '驳回原因',
  `cancel_time` datetime DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '取消原因',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `create_user_id` bigint NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`registration_id`) USING BTREE,
  UNIQUE KEY `uk_lot_supplier_credit` (`lot_id`, `supplier_credit_code`) USING BTREE,
  KEY `idx_project_lot_status` (`project_id`, `lot_id`, `status`) USING BTREE,
  KEY `idx_supplier_enterprise_id` (`supplier_enterprise_id`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='招投标供应商报名表' ROW_FORMAT=DYNAMIC;

-- 已执行过早期 v3.34.1 的开发库通过动态 SQL 补齐新增字段。
SET @add_registration_start_time = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `t_bid_lot` ADD COLUMN `registration_start_time` datetime DEFAULT NULL COMMENT ''报名开始时间'' AFTER `budget_amount`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_bid_lot'
    AND COLUMN_NAME = 'registration_start_time'
);
PREPARE stmt FROM @add_registration_start_time;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_registration_end_time = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `t_bid_lot` ADD COLUMN `registration_end_time` datetime DEFAULT NULL COMMENT ''报名截止时间'' AFTER `registration_start_time`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_bid_lot'
    AND COLUMN_NAME = 'registration_end_time'
);
PREPARE stmt FROM @add_registration_end_time;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_registration_submit_time = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `t_bid_registration` ADD COLUMN `submit_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''报名提交时间'' AFTER `status`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_bid_registration'
    AND COLUMN_NAME = 'submit_time'
);
PREPARE stmt FROM @add_registration_submit_time;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_registration_cancel_time = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `t_bid_registration` ADD COLUMN `cancel_time` datetime DEFAULT NULL COMMENT ''取消时间'' AFTER `reject_reason`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_bid_registration'
    AND COLUMN_NAME = 'cancel_time'
);
PREPARE stmt FROM @add_registration_cancel_time;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_registration_cancel_reason = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `t_bid_registration` ADD COLUMN `cancel_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT ''取消原因'' AFTER `cancel_time`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_bid_registration'
    AND COLUMN_NAME = 'cancel_reason'
);
PREPARE stmt FROM @add_registration_cancel_reason;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------
-- 2. 报名方式字典
-- ----------------------------
INSERT INTO `t_dict` (`dict_name`, `dict_code`, `remark`, `disabled_flag`)
VALUES ('报名方式', 'BID_REGISTRATION_TYPE', '供应商报名方式，供报名管理使用', 0)
ON DUPLICATE KEY UPDATE
  `dict_name` = VALUES(`dict_name`),
  `remark` = VALUES(`remark`),
  `disabled_flag` = VALUES(`disabled_flag`);

UPDATE `t_dict_data` dd
INNER JOIN `t_dict` d ON d.`dict_id` = dd.`dict_id`
SET dd.`data_value` = 'SELF_REGISTER',
    dd.`data_label` = '自主报名',
    dd.`data_style` = 'primary',
    dd.`remark` = '供应商自主报名',
    dd.`sort_order` = 300,
    dd.`disabled_flag` = 0
WHERE d.`dict_code` = 'BID_REGISTRATION_TYPE'
  AND dd.`data_value` = 'PUBLIC'
  AND NOT EXISTS (
      SELECT 1
      FROM (
          SELECT dd2.`dict_id`, dd2.`data_value`
          FROM `t_dict_data` dd2
      ) existing_data
      WHERE existing_data.`dict_id` = d.`dict_id`
        AND existing_data.`data_value` = 'SELF_REGISTER'
  );

DELETE dd
FROM `t_dict_data` dd
INNER JOIN `t_dict` d ON d.`dict_id` = dd.`dict_id`
WHERE d.`dict_code` = 'BID_REGISTRATION_TYPE'
  AND dd.`data_value` = 'PUBLIC';

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, 'SELF_REGISTER', '自主报名', 'primary', '供应商自主报名', 300, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'BID_REGISTRATION_TYPE'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = 'SELF_REGISTER'
  );

UPDATE `t_dict_data` dd
INNER JOIN `t_dict` d ON d.`dict_id` = dd.`dict_id`
SET dd.`data_label` = '邀请确认',
    dd.`data_style` = 'success',
    dd.`remark` = '招标人或代理机构邀请确认',
    dd.`sort_order` = 200,
    dd.`disabled_flag` = 0
WHERE d.`dict_code` = 'BID_REGISTRATION_TYPE'
  AND dd.`data_value` = 'INVITED';

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, 'INVITED', '邀请确认', 'success', '招标人或代理机构邀请确认', 200, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'BID_REGISTRATION_TYPE'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = 'INVITED'
  );

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, 'MANUAL_ENTRY', '后台代录', 'warning', '内部经办人员后台代录报名', 100, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'BID_REGISTRATION_TYPE'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = 'MANUAL_ENTRY'
  );

-- ----------------------------
-- 3. 正式菜单与隐藏页面
-- ----------------------------
INSERT INTO `t_menu`
(`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `update_user_id`)
VALUES
(752, '供应商报名', 2, 731, 3, '/system/bid/registration/list', '/system/bid/registration/registration-list.vue', NULL, NULL, NULL, 'lucide:clipboard-check', NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(753, '供应商报名新增', 2, 752, 11, '/system/bid/registration/create', '/system/bid/registration/registration-form.vue', NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, 0, 0, 0, 1, 1),
(754, '供应商报名详情', 2, 752, 12, '/system/bid/registration/detail', '/system/bid/registration/registration-detail.vue', NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, 0, 0, 0, 1, 1),
(755, '查询报名', 3, 752, 1, NULL, NULL, 1, 'bid:registration:query', 'bid:registration:query', NULL, 752, 0, NULL, 0, 1, 0, 0, 1, 1),
(756, '新增报名', 3, 752, 2, NULL, NULL, 1, 'bid:registration:create', 'bid:registration:create', NULL, 752, 0, NULL, 0, 1, 0, 0, 1, 1),
(757, '报名通过', 3, 752, 3, NULL, NULL, 1, 'bid:registration:approve', 'bid:registration:approve', NULL, 752, 0, NULL, 0, 1, 0, 0, 1, 1),
(758, '报名驳回', 3, 752, 4, NULL, NULL, 1, 'bid:registration:reject', 'bid:registration:reject', NULL, 752, 0, NULL, 0, 1, 0, 0, 1, 1),
(759, '取消报名', 3, 752, 5, NULL, NULL, 1, 'bid:registration:cancel', 'bid:registration:cancel', NULL, 752, 0, NULL, 0, 1, 0, 0, 1, 1)
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
-- 4. 默认管理员角色授权
-- ----------------------------
INSERT INTO `t_role_menu` (`role_id`, `menu_id`)
SELECT 1, tmp.`menu_id`
FROM (
    SELECT 752 AS `menu_id`
    UNION ALL SELECT 753
    UNION ALL SELECT 754
    UNION ALL SELECT 755
    UNION ALL SELECT 756
    UNION ALL SELECT 757
    UNION ALL SELECT 758
    UNION ALL SELECT 759
) tmp
WHERE NOT EXISTS (
    SELECT 1
    FROM `t_role_menu` rm
    WHERE rm.`role_id` = 1
      AND rm.`menu_id` = tmp.`menu_id`
);

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;

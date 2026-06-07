-- =========================================================
-- v3.34.0 招投标项目/标段管理增量脚本
-- 目标：落首批招标项目管理、标段管理所需业务表、业务字典、主系统菜单、功能点和管理员授权
-- 说明：
--   1. 数据库表沿用主库 `t_` 前缀命名，对应设计文档中的 `bid_project / bid_lot / bid_project_member / bid_workflow_history`
--   2. 菜单按主系统模块处理，新增一级目录 `招投标管理`，不挂到 Demo、OA、Goods、Category 目录
--   3. 组件路径按 `system` 侧组织，支持项目/标段复杂新增、编辑、详情的隐藏二级菜单
--   4. 本脚本包含 DDL，MySQL 执行建表语句时会隐式提交，执行前请先完成库备份
-- 编码：utf8mb4
-- 菜单 ID 规划：731-751
-- 字典编码规划：
--   BID_PROJECT_TYPE
--   BID_PROCUREMENT_MODE
--   BID_EVALUATION_MODE
--   BID_AWARD_MODE
--   BID_PROJECT_MEMBER_ROLE
-- =========================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

START TRANSACTION;

-- ----------------------------
-- 1. 业务表：招标项目
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_bid_project` (
  `project_id` bigint NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  `project_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '项目编号',
  `project_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '项目名称',
  `project_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '项目类型，取值于字典 BID_PROJECT_TYPE',
  `procurement_mode` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '采购方式，取值于字典 BID_PROCUREMENT_MODE',
  `owner_org_id` bigint NOT NULL COMMENT '招标人归属组织ID',
  `agent_org_id` bigint DEFAULT NULL COMMENT '代理机构归属组织ID',
  `manager_employee_id` bigint DEFAULT NULL COMMENT '项目负责人员工ID',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'DRAFT' COMMENT '项目状态，代码枚举维护',
  `budget_amount` decimal(18, 2) DEFAULT NULL COMMENT '项目预算金额',
  `publish_time` datetime DEFAULT NULL COMMENT '项目发布时间',
  `award_time` datetime DEFAULT NULL COMMENT '定标完成时间',
  `archive_time` datetime DEFAULT NULL COMMENT '归档完成时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `create_user_id` bigint NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`project_id`) USING BTREE,
  UNIQUE KEY `uk_project_code` (`project_code`) USING BTREE,
  KEY `idx_owner_org_status` (`owner_org_id`, `status`) USING BTREE,
  KEY `idx_agent_org_status` (`agent_org_id`, `status`) USING BTREE,
  KEY `idx_manager_employee_id` (`manager_employee_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='招标项目表' ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- 2. 业务表：标段
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_bid_lot` (
  `lot_id` bigint NOT NULL AUTO_INCREMENT COMMENT '标段ID',
  `project_id` bigint NOT NULL COMMENT '所属项目ID',
  `lot_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标段编号',
  `lot_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标段名称',
  `lot_no` int NOT NULL COMMENT '标段序号',
  `lot_scope` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '标段范围说明',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'DRAFT' COMMENT '标段状态，代码枚举维护',
  `budget_amount` decimal(18, 2) DEFAULT NULL COMMENT '标段预算金额',
  `bid_start_time` datetime DEFAULT NULL COMMENT '投标开始时间',
  `bid_end_time` datetime DEFAULT NULL COMMENT '投标截止时间',
  `opening_time` datetime DEFAULT NULL COMMENT '开标时间',
  `evaluation_mode` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '评标方式，取值于字典 BID_EVALUATION_MODE',
  `award_mode` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '定标方式，取值于字典 BID_AWARD_MODE',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `create_user_id` bigint NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`lot_id`) USING BTREE,
  UNIQUE KEY `uk_project_lot_code` (`project_id`, `lot_code`) USING BTREE,
  UNIQUE KEY `uk_project_lot_no` (`project_id`, `lot_no`) USING BTREE,
  KEY `idx_project_id` (`project_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_bid_end_time` (`bid_end_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='招标标段表' ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- 3. 业务表：项目成员
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_bid_project_member` (
  `project_member_id` bigint NOT NULL AUTO_INCREMENT COMMENT '项目成员ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `member_role` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '成员角色，取值于字典 BID_PROJECT_MEMBER_ROLE',
  `is_owner` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否主负责人 0-否 1-是',
  `join_time` datetime DEFAULT NULL COMMENT '加入时间',
  `leave_time` datetime DEFAULT NULL COMMENT '离开时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
  `create_user_id` bigint NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`project_member_id`) USING BTREE,
  UNIQUE KEY `uk_project_employee_role_active` (`project_id`, `employee_id`, `member_role`, `deleted_flag`) USING BTREE,
  KEY `idx_employee_id` (`employee_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='招标项目成员表' ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- 4. 业务表：流程历史
-- 首批项目/标段状态动作统一落这张历史表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_bid_workflow_history` (
  `history_id` bigint NOT NULL AUTO_INCREMENT COMMENT '流程历史ID',
  `business_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '业务类型，例如 PROJECT、LOT',
  `business_id` bigint NOT NULL COMMENT '业务主键ID',
  `project_id` bigint DEFAULT NULL COMMENT '项目ID',
  `lot_id` bigint DEFAULT NULL COMMENT '标段ID',
  `from_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '原状态',
  `to_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '目标状态',
  `action_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '动作编码',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '操作人姓名快照',
  `operator_side` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '操作端 OWNER、AGENT、SUPPLIER、SYSTEM',
  `operate_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '操作备注',
  `snapshot_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '动作快照JSON',
  `operate_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`history_id`) USING BTREE,
  KEY `idx_business_type_id_time` (`business_type`, `business_id`, `operate_time`) USING BTREE,
  KEY `idx_project_id` (`project_id`) USING BTREE,
  KEY `idx_lot_id` (`lot_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='招投标流程历史表' ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- 5. 业务字典定义
-- 状态类枚举按代码固化，不在此处下发可运营字典
-- ----------------------------
INSERT INTO `t_dict` (`dict_name`, `dict_code`, `remark`, `disabled_flag`)
VALUES ('招标项目类型', 'BID_PROJECT_TYPE', '招标项目类型，供项目建档使用', 0)
ON DUPLICATE KEY UPDATE
  `dict_name` = VALUES(`dict_name`),
  `remark` = VALUES(`remark`),
  `disabled_flag` = VALUES(`disabled_flag`);

INSERT INTO `t_dict` (`dict_name`, `dict_code`, `remark`, `disabled_flag`)
VALUES ('采购方式', 'BID_PROCUREMENT_MODE', '采购方式，供项目建档使用', 0)
ON DUPLICATE KEY UPDATE
  `dict_name` = VALUES(`dict_name`),
  `remark` = VALUES(`remark`),
  `disabled_flag` = VALUES(`disabled_flag`);

INSERT INTO `t_dict` (`dict_name`, `dict_code`, `remark`, `disabled_flag`)
VALUES ('评标方式', 'BID_EVALUATION_MODE', '评标方式，供标段维护使用', 0)
ON DUPLICATE KEY UPDATE
  `dict_name` = VALUES(`dict_name`),
  `remark` = VALUES(`remark`),
  `disabled_flag` = VALUES(`disabled_flag`);

INSERT INTO `t_dict` (`dict_name`, `dict_code`, `remark`, `disabled_flag`)
VALUES ('定标方式', 'BID_AWARD_MODE', '定标方式，供标段维护使用', 0)
ON DUPLICATE KEY UPDATE
  `dict_name` = VALUES(`dict_name`),
  `remark` = VALUES(`remark`),
  `disabled_flag` = VALUES(`disabled_flag`);

INSERT INTO `t_dict` (`dict_name`, `dict_code`, `remark`, `disabled_flag`)
VALUES ('项目成员角色', 'BID_PROJECT_MEMBER_ROLE', '项目成员角色，供项目成员维护使用', 0)
ON DUPLICATE KEY UPDATE
  `dict_name` = VALUES(`dict_name`),
  `remark` = VALUES(`remark`),
  `disabled_flag` = VALUES(`disabled_flag`);

-- ----------------------------
-- 6. 招标项目类型字典项
-- useDictOptions 当前按 sort_order 倒序渲染，这里按期望显示顺序设置排序值
-- ----------------------------
INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, 'GOODS', '货物类', 'primary', '招标项目类型：货物类', 400, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'BID_PROJECT_TYPE'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = 'GOODS'
  );

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, 'SERVICE', '服务类', 'success', '招标项目类型：服务类', 300, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'BID_PROJECT_TYPE'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = 'SERVICE'
  );

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, 'ENGINEERING', '工程类', 'warning', '招标项目类型：工程类', 200, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'BID_PROJECT_TYPE'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = 'ENGINEERING'
  );

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, 'OTHER', '其他', 'info', '招标项目类型：其他', 100, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'BID_PROJECT_TYPE'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = 'OTHER'
  );

-- ----------------------------
-- 7. 采购方式字典项
-- ----------------------------
INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, 'PUBLIC_BIDDING', '公开招标', 'primary', '采购方式：公开招标', 500, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'BID_PROCUREMENT_MODE'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = 'PUBLIC_BIDDING'
  );

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, 'INVITED_BIDDING', '邀请招标', 'success', '采购方式：邀请招标', 400, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'BID_PROCUREMENT_MODE'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = 'INVITED_BIDDING'
  );

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, 'COMPETITIVE_NEGOTIATION', '竞争性谈判', 'warning', '采购方式：竞争性谈判', 300, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'BID_PROCUREMENT_MODE'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = 'COMPETITIVE_NEGOTIATION'
  );

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, 'INQUIRY', '询价采购', 'info', '采购方式：询价采购', 200, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'BID_PROCUREMENT_MODE'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = 'INQUIRY'
  );

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, 'SINGLE_SOURCE', '单一来源', 'danger', '采购方式：单一来源', 100, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'BID_PROCUREMENT_MODE'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = 'SINGLE_SOURCE'
  );

-- ----------------------------
-- 8. 评标方式字典项
-- ----------------------------
INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, 'COMPREHENSIVE_SCORE', '综合评分法', 'primary', '评标方式：综合评分法', 300, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'BID_EVALUATION_MODE'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = 'COMPREHENSIVE_SCORE'
  );

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, 'LOWEST_PRICE', '最低评标价法', 'success', '评标方式：最低评标价法', 200, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'BID_EVALUATION_MODE'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = 'LOWEST_PRICE'
  );

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, 'REASONABLE_LOW_PRICE', '合理低价法', 'warning', '评标方式：合理低价法', 100, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'BID_EVALUATION_MODE'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = 'REASONABLE_LOW_PRICE'
  );

-- ----------------------------
-- 9. 定标方式字典项
-- ----------------------------
INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, 'CONFIRM_FIRST', '确认第一中标候选人', 'primary', '定标方式：确认第一中标候选人', 200, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'BID_AWARD_MODE'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = 'CONFIRM_FIRST'
  );

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, 'AUTHORIZE_FIRST', '授权第一中标候选人', 'success', '定标方式：授权第一中标候选人', 100, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'BID_AWARD_MODE'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = 'AUTHORIZE_FIRST'
  );

-- ----------------------------
-- 10. 项目成员角色字典项
-- ----------------------------
INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, 'OWNER', '招标人负责人', 'primary', '项目成员角色：招标人负责人', 500, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'BID_PROJECT_MEMBER_ROLE'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = 'OWNER'
  );

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, 'APPROVER', '招标人审批人', 'success', '项目成员角色：招标人审批人', 400, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'BID_PROJECT_MEMBER_ROLE'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = 'APPROVER'
  );

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, 'AGENT_OPERATOR', '代理经办人', 'warning', '项目成员角色：代理经办人', 300, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'BID_PROJECT_MEMBER_ROLE'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = 'AGENT_OPERATOR'
  );

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, 'AGENT_MANAGER', '代理负责人', 'info', '项目成员角色：代理负责人', 200, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'BID_PROJECT_MEMBER_ROLE'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = 'AGENT_MANAGER'
  );

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, 'ARCHIVIST', '归档管理员', 'default', '项目成员角色：归档管理员', 100, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'BID_PROJECT_MEMBER_ROLE'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = 'ARCHIVIST'
  );

-- ----------------------------
-- 11. 主系统菜单与隐藏编辑页
-- 组件路径按当前前端 `src/views/system/bid/**` 组织
-- ----------------------------
INSERT INTO `t_menu`
(`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `update_user_id`)
VALUES
(731, '招投标管理', 1, 0, 10, '/system/bid', NULL, NULL, NULL, NULL, 'lucide:briefcase-business', NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(732, '招标项目', 2, 731, 1, '/system/bid/project/list', '/system/bid/project/project-list.vue', NULL, NULL, NULL, 'lucide:files', NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(733, '招标项目新增', 2, 732, 11, '/system/bid/project/create', '/system/bid/project/project-form.vue', NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, 0, 0, 0, 1, 1),
(734, '招标项目编辑', 2, 732, 12, '/system/bid/project/edit', '/system/bid/project/project-form.vue', NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, 0, 0, 0, 1, 1),
(735, '招标项目详情', 2, 732, 13, '/system/bid/project/detail', '/system/bid/project/project-detail.vue', NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, 0, 0, 0, 1, 1),
(736, '标段管理', 2, 731, 2, '/system/bid/lot/list', '/system/bid/lot/lot-list.vue', NULL, NULL, NULL, 'lucide:git-branch-plus', NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(737, '标段新增', 2, 736, 11, '/system/bid/lot/create', '/system/bid/lot/lot-form.vue', NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, 0, 0, 0, 1, 1),
(738, '标段编辑', 2, 736, 12, '/system/bid/lot/edit', '/system/bid/lot/lot-form.vue', NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, 0, 0, 0, 1, 1),
(739, '标段详情', 2, 736, 13, '/system/bid/lot/detail', '/system/bid/lot/lot-detail.vue', NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, 0, 0, 0, 1, 1),
(740, '查询项目', 3, 732, 1, NULL, NULL, 1, 'bid:project:query', 'bid:project:query', NULL, 732, 0, NULL, 0, 1, 0, 0, 1, 1),
(741, '新增项目', 3, 732, 2, NULL, NULL, 1, 'bid:project:create', 'bid:project:create', NULL, 732, 0, NULL, 0, 1, 0, 0, 1, 1),
(742, '编辑项目', 3, 732, 3, NULL, NULL, 1, 'bid:project:update', 'bid:project:update', NULL, 732, 0, NULL, 0, 1, 0, 0, 1, 1),
(743, '提交计划', 3, 732, 4, NULL, NULL, 1, 'bid:project:submit-plan', 'bid:project:submit-plan', NULL, 732, 0, NULL, 0, 1, 0, 0, 1, 1),
(744, '发布项目', 3, 732, 5, NULL, NULL, 1, 'bid:project:publish', 'bid:project:publish', NULL, 732, 0, NULL, 0, 1, 0, 0, 1, 1),
(745, '归档项目', 3, 732, 6, NULL, NULL, 1, 'bid:project:archive', 'bid:project:archive', NULL, 732, 0, NULL, 0, 1, 0, 0, 1, 1),
(746, '作废项目', 3, 732, 7, NULL, NULL, 1, 'bid:project:cancel', 'bid:project:cancel', NULL, 732, 0, NULL, 0, 1, 0, 0, 1, 1),
(747, '查询标段', 3, 736, 1, NULL, NULL, 1, 'bid:lot:query', 'bid:lot:query', NULL, 736, 0, NULL, 0, 1, 0, 0, 1, 1),
(748, '新增标段', 3, 736, 2, NULL, NULL, 1, 'bid:lot:create', 'bid:lot:create', NULL, 736, 0, NULL, 0, 1, 0, 0, 1, 1),
(749, '编辑标段', 3, 736, 3, NULL, NULL, 1, 'bid:lot:update', 'bid:lot:update', NULL, 736, 0, NULL, 0, 1, 0, 0, 1, 1),
(750, '关闭投标', 3, 736, 4, NULL, NULL, 1, 'bid:lot:close-bid', 'bid:lot:close-bid', NULL, 736, 0, NULL, 0, 1, 0, 0, 1, 1),
(751, '废止标段', 3, 736, 5, NULL, NULL, 1, 'bid:lot:void', 'bid:lot:void', NULL, 736, 0, NULL, 0, 1, 0, 0, 1, 1)
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
-- 12. 默认管理员角色授权
-- ----------------------------
INSERT INTO `t_role_menu` (`role_id`, `menu_id`)
SELECT 1, tmp.`menu_id`
FROM (
    SELECT 731 AS `menu_id`
    UNION ALL SELECT 732
    UNION ALL SELECT 733
    UNION ALL SELECT 734
    UNION ALL SELECT 735
    UNION ALL SELECT 736
    UNION ALL SELECT 737
    UNION ALL SELECT 738
    UNION ALL SELECT 739
    UNION ALL SELECT 740
    UNION ALL SELECT 741
    UNION ALL SELECT 742
    UNION ALL SELECT 743
    UNION ALL SELECT 744
    UNION ALL SELECT 745
    UNION ALL SELECT 746
    UNION ALL SELECT 747
    UNION ALL SELECT 748
    UNION ALL SELECT 749
    UNION ALL SELECT 750
    UNION ALL SELECT 751
) tmp
WHERE NOT EXISTS (
    SELECT 1
    FROM `t_role_menu` rm
    WHERE rm.`role_id` = 1
      AND rm.`menu_id` = tmp.`menu_id`
);

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;

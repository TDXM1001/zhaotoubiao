-- =========================================================
-- v3.36.0 招投标 P2 开评定标最小闭环增量脚本
-- 目标：新增开标、评标、定标业务表，以及内部主系统菜单和功能点
-- 说明：
--   1. 本脚本承接 v3.34/v3.35 的项目、标段、报名、招标文件、投标基础表
--   2. 状态类字段继续由代码枚举维护，不下发可运营字典
--   3. 结果公示通过定标确认时间和公示时间驱动，门户仅查询本人参与标段结果
-- 编码：utf8mb4
-- 菜单 ID 规划：779-803
-- =========================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

START TRANSACTION;

-- ----------------------------
-- 1. 业务表：开标主记录
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_bid_opening` (
  `opening_id` bigint NOT NULL AUTO_INCREMENT COMMENT '开标ID',
  `project_id` bigint NOT NULL COMMENT '所属项目ID',
  `lot_id` bigint NOT NULL COMMENT '所属标段ID',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'PENDING' COMMENT '开标状态，代码枚举维护',
  `opening_time` datetime DEFAULT NULL COMMENT '开标时间',
  `opening_place` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '开标地点',
  `host_employee_id` bigint DEFAULT NULL COMMENT '主持人员工ID',
  `recorder_employee_id` bigint DEFAULT NULL COMMENT '记录人员工ID',
  `summary` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '开标摘要',
  `abnormal_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否异常 0-否 1-是',
  `abnormal_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '异常原因',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `create_user_id` bigint NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`opening_id`) USING BTREE,
  KEY `idx_project_lot_status` (`project_id`, `lot_id`, `status`) USING BTREE,
  KEY `idx_lot_id` (`lot_id`) USING BTREE,
  KEY `idx_opening_time` (`opening_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='招投标开标主记录表' ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- 2. 业务表：开标明细
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_bid_opening_item` (
  `opening_item_id` bigint NOT NULL AUTO_INCREMENT COMMENT '开标明细ID',
  `opening_id` bigint NOT NULL COMMENT '开标ID',
  `submission_id` bigint NOT NULL COMMENT '投标ID',
  `submission_version_id` bigint NOT NULL COMMENT '投标版本ID',
  `supplier_enterprise_id` bigint DEFAULT NULL COMMENT '供应商企业ID',
  `supplier_name_snapshot` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '供应商名称快照',
  `supplier_credit_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '统一社会信用代码',
  `quoted_price` decimal(18, 2) DEFAULT NULL COMMENT '开标报价金额',
  `document_check_result` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '文件检查结果',
  `open_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '开标备注',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`opening_item_id`) USING BTREE,
  UNIQUE KEY `uk_opening_submission` (`opening_id`, `submission_id`) USING BTREE,
  KEY `idx_opening_id` (`opening_id`) USING BTREE,
  KEY `idx_submission_id` (`submission_id`) USING BTREE,
  KEY `idx_supplier_credit_code` (`supplier_credit_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='招投标开标明细表' ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- 3. 业务表：评标主记录
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_bid_evaluation` (
  `evaluation_id` bigint NOT NULL AUTO_INCREMENT COMMENT '评标ID',
  `project_id` bigint NOT NULL COMMENT '所属项目ID',
  `lot_id` bigint NOT NULL COMMENT '所属标段ID',
  `opening_id` bigint NOT NULL COMMENT '开标ID',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'PENDING' COMMENT '评标状态，代码枚举维护',
  `evaluation_mode` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '评标方式',
  `start_time` datetime DEFAULT NULL COMMENT '评标开始时间',
  `finalize_time` datetime DEFAULT NULL COMMENT '评标定稿时间',
  `final_summary` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '评标摘要',
  `rollback_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '回退原因',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `create_user_id` bigint NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`evaluation_id`) USING BTREE,
  KEY `idx_project_lot_status` (`project_id`, `lot_id`, `status`) USING BTREE,
  KEY `idx_lot_id` (`lot_id`) USING BTREE,
  KEY `idx_opening_id` (`opening_id`) USING BTREE,
  KEY `idx_finalize_time` (`finalize_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='招投标评标主记录表' ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- 4. 业务表：评标明细
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_bid_evaluation_item` (
  `evaluation_item_id` bigint NOT NULL AUTO_INCREMENT COMMENT '评标明细ID',
  `evaluation_id` bigint NOT NULL COMMENT '评标ID',
  `opening_item_id` bigint NOT NULL COMMENT '开标明细ID',
  `submission_id` bigint NOT NULL COMMENT '投标ID',
  `supplier_name_snapshot` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '供应商名称快照',
  `quoted_price` decimal(18, 2) DEFAULT NULL COMMENT '开标报价金额',
  `total_score` decimal(10, 2) DEFAULT NULL COMMENT '总分',
  `ranking_no` int DEFAULT NULL COMMENT '排名',
  `recommend_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否推荐中标 0-否 1-是',
  `evaluation_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '评标意见',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`evaluation_item_id`) USING BTREE,
  UNIQUE KEY `uk_evaluation_opening_item` (`evaluation_id`, `opening_item_id`) USING BTREE,
  KEY `idx_evaluation_id` (`evaluation_id`) USING BTREE,
  KEY `idx_submission_id` (`submission_id`) USING BTREE,
  KEY `idx_ranking_no` (`ranking_no`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='招投标评标明细表' ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- 5. 业务表：定标主记录
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_bid_award` (
  `award_id` bigint NOT NULL AUTO_INCREMENT COMMENT '定标ID',
  `project_id` bigint NOT NULL COMMENT '所属项目ID',
  `lot_id` bigint NOT NULL COMMENT '所属标段ID',
  `evaluation_id` bigint NOT NULL COMMENT '评标ID',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'PENDING' COMMENT '定标状态，代码枚举维护',
  `winning_submission_id` bigint NOT NULL COMMENT '中标投标ID',
  `winner_enterprise_id` bigint DEFAULT NULL COMMENT '中标供应商企业ID',
  `winner_name_snapshot` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '中标供应商名称快照',
  `winner_credit_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '中标供应商统一社会信用代码',
  `award_amount` decimal(18, 2) DEFAULT NULL COMMENT '中标金额',
  `confirm_user_id` bigint DEFAULT NULL COMMENT '确认人',
  `confirm_time` datetime DEFAULT NULL COMMENT '确认时间',
  `public_notice_time` datetime DEFAULT NULL COMMENT '结果公示时间',
  `rollback_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '回退原因',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `create_user_id` bigint NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`award_id`) USING BTREE,
  KEY `idx_project_lot_status` (`project_id`, `lot_id`, `status`) USING BTREE,
  KEY `idx_lot_id` (`lot_id`) USING BTREE,
  KEY `idx_evaluation_id` (`evaluation_id`) USING BTREE,
  KEY `idx_winner_credit_code` (`winner_credit_code`) USING BTREE,
  KEY `idx_public_notice_time` (`public_notice_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='招投标定标主记录表' ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- 6. 主系统菜单、隐藏页面与功能点
-- ----------------------------
INSERT INTO `t_menu`
(`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `update_user_id`)
VALUES
(779, '开标管理', 2, 731, 7, '/system/bid/opening/list', '/system/bid/opening/opening-list.vue', NULL, NULL, NULL, 'lucide:clipboard-list', NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(780, '创建开标安排', 2, 779, 11, '/system/bid/opening/create', '/system/bid/opening/opening-form.vue', NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, 0, 0, 0, 1, 1),
(781, '开标详情', 2, 779, 12, '/system/bid/opening/detail', '/system/bid/opening/opening-detail.vue', NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, 0, 0, 0, 1, 1),
(782, '查询开标', 3, 779, 1, NULL, NULL, 1, 'bid:opening:query', 'bid:opening:query', NULL, 779, 0, NULL, 0, 1, 0, 0, 1, 1),
(783, '创建开标安排', 3, 779, 2, NULL, NULL, 1, 'bid:opening:create', 'bid:opening:create', NULL, 779, 0, NULL, 0, 1, 0, 0, 1, 1),
(784, '开始开标', 3, 779, 3, NULL, NULL, 1, 'bid:opening:start-opening', 'bid:opening:start-opening', NULL, 779, 0, NULL, 0, 1, 0, 0, 1, 1),
(785, '完成开标', 3, 779, 4, NULL, NULL, 1, 'bid:opening:complete-opening', 'bid:opening:complete-opening', NULL, 779, 0, NULL, 0, 1, 0, 0, 1, 1),
(786, '异常关闭开标', 3, 779, 5, NULL, NULL, 1, 'bid:opening:abnormal-close-opening', 'bid:opening:abnormal-close-opening', NULL, 779, 0, NULL, 0, 1, 0, 0, 1, 1),
(787, '评标管理', 2, 731, 8, '/system/bid/evaluation/list', '/system/bid/evaluation/evaluation-list.vue', NULL, NULL, NULL, 'lucide:scale', NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(788, '创建评标记录', 2, 787, 11, '/system/bid/evaluation/create', '/system/bid/evaluation/evaluation-form.vue', NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, 0, 0, 0, 1, 1),
(789, '评标详情', 2, 787, 12, '/system/bid/evaluation/detail', '/system/bid/evaluation/evaluation-detail.vue', NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, 0, 0, 0, 1, 1),
(790, '查询评标', 3, 787, 1, NULL, NULL, 1, 'bid:evaluation:query', 'bid:evaluation:query', NULL, 787, 0, NULL, 0, 1, 0, 0, 1, 1),
(791, '创建评标记录', 3, 787, 2, NULL, NULL, 1, 'bid:evaluation:create', 'bid:evaluation:create', NULL, 787, 0, NULL, 0, 1, 0, 0, 1, 1),
(792, '开始评标', 3, 787, 3, NULL, NULL, 1, 'bid:evaluation:start-evaluation', 'bid:evaluation:start-evaluation', NULL, 787, 0, NULL, 0, 1, 0, 0, 1, 1),
(793, '评标定稿', 3, 787, 4, NULL, NULL, 1, 'bid:evaluation:finalize-evaluation', 'bid:evaluation:finalize-evaluation', NULL, 787, 0, NULL, 0, 1, 0, 0, 1, 1),
(794, '回退评标', 3, 787, 5, NULL, NULL, 1, 'bid:evaluation:rollback-evaluation', 'bid:evaluation:rollback-evaluation', NULL, 787, 0, NULL, 0, 1, 0, 0, 1, 1),
(795, '定标管理', 2, 731, 9, '/system/bid/award/list', '/system/bid/award/award-list.vue', NULL, NULL, NULL, 'lucide:trophy', NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(796, '创建定标记录', 2, 795, 11, '/system/bid/award/create', '/system/bid/award/award-form.vue', NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, 0, 0, 0, 1, 1),
(797, '定标详情', 2, 795, 12, '/system/bid/award/detail', '/system/bid/award/award-detail.vue', NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, 0, 0, 0, 1, 1),
(798, '查询定标', 3, 795, 1, NULL, NULL, 1, 'bid:award:query', 'bid:award:query', NULL, 795, 0, NULL, 0, 1, 0, 0, 1, 1),
(799, '创建定标记录', 3, 795, 2, NULL, NULL, 1, 'bid:award:create', 'bid:award:create', NULL, 795, 0, NULL, 0, 1, 0, 0, 1, 1),
(800, '确认定标', 3, 795, 3, NULL, NULL, 1, 'bid:award:confirm-award', 'bid:award:confirm-award', NULL, 795, 0, NULL, 0, 1, 0, 0, 1, 1),
(801, '回退定标', 3, 795, 4, NULL, NULL, 1, 'bid:award:rollback-award', 'bid:award:rollback-award', NULL, 795, 0, NULL, 0, 1, 0, 0, 1, 1),
(802, '取消定标', 3, 795, 5, NULL, NULL, 1, 'bid:award:cancel-award', 'bid:award:cancel-award', NULL, 795, 0, NULL, 0, 1, 0, 0, 1, 1),
(803, '门户标段结果', 2, 776, 13, '/bid-portal/lots/result', '/bid-portal/result/portal-result.vue', NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, 0, 0, 0, 1, 1)
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
-- 7. 默认管理员角色授权
-- ----------------------------
INSERT INTO `t_role_menu` (`role_id`, `menu_id`)
SELECT 1, tmp.`menu_id`
FROM (
    SELECT 779 AS `menu_id`
    UNION ALL SELECT 780
    UNION ALL SELECT 781
    UNION ALL SELECT 782
    UNION ALL SELECT 783
    UNION ALL SELECT 784
    UNION ALL SELECT 785
    UNION ALL SELECT 786
    UNION ALL SELECT 787
    UNION ALL SELECT 788
    UNION ALL SELECT 789
    UNION ALL SELECT 790
    UNION ALL SELECT 791
    UNION ALL SELECT 792
    UNION ALL SELECT 793
    UNION ALL SELECT 794
    UNION ALL SELECT 795
    UNION ALL SELECT 796
    UNION ALL SELECT 797
    UNION ALL SELECT 798
    UNION ALL SELECT 799
    UNION ALL SELECT 800
    UNION ALL SELECT 801
    UNION ALL SELECT 802
    UNION ALL SELECT 803
) tmp
WHERE NOT EXISTS (
    SELECT 1
    FROM `t_role_menu` rm
    WHERE rm.`role_id` = 1
      AND rm.`menu_id` = tmp.`menu_id`
);

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;

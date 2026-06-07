-- =========================================================
-- v3.32.0 增量脚本
-- 目标：补齐职务管理前端对接所需的按钮权限与管理员角色授权
-- 说明：当前后端契约未把 position_level 定义为字典字段，因此本脚本先不补字典数据
-- 编码：utf8mb4
-- =========================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

START TRANSACTION;

-- ----------------------------
-- 1. 职务管理按钮权限
-- ----------------------------
INSERT INTO `t_menu`
(`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `update_user_id`)
VALUES
(301, '查询职务', 3, 228, 1, NULL, NULL, 1, 'system:position:query', 'system:position:query', NULL, 228, 0, NULL, 1, 1, 0, 0, 1, 1),
(302, '新增职务', 3, 228, 2, NULL, NULL, 1, 'system:position:add', 'system:position:add', NULL, 228, 0, NULL, 1, 1, 0, 0, 1, 1),
(303, '编辑职务', 3, 228, 3, NULL, NULL, 1, 'system:position:update', 'system:position:update', NULL, 228, 0, NULL, 1, 1, 0, 0, 1, 1),
(304, '删除职务', 3, 228, 4, NULL, NULL, 1, 'system:position:delete', 'system:position:delete', NULL, 228, 0, NULL, 1, 1, 0, 0, 1, 1)
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
-- 2. 默认管理员角色补齐职务管理授权
-- ----------------------------
INSERT INTO `t_role_menu` (`role_id`, `menu_id`)
SELECT 1, tmp.`menu_id`
FROM (
    SELECT 228 AS `menu_id`
    UNION ALL SELECT 301
    UNION ALL SELECT 302
    UNION ALL SELECT 303
    UNION ALL SELECT 304
) tmp
WHERE NOT EXISTS (
    SELECT 1
    FROM `t_role_menu` rm
    WHERE rm.`role_id` = 1
      AND rm.`menu_id` = tmp.`menu_id`
);

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================================
-- v3.33.0 增量脚本
-- 目标：补齐文件管理、帮助文档、更新日志前端对接所需字典、权限与管理员授权
-- 说明：本次只补充前端真实使用到的字典与权限，不改动业务表结构
-- 编码：utf8mb4
-- =========================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

START TRANSACTION;

-- ----------------------------
-- 1. 补齐字典定义
-- ----------------------------
INSERT INTO `t_dict` (`dict_name`, `dict_code`, `remark`, `disabled_flag`)
SELECT '文件夹类型', 'FILE_FOLDER_TYPE', '文件管理模块使用的文件归类字典', 0
WHERE NOT EXISTS (
    SELECT 1
    FROM `t_dict`
    WHERE `dict_code` = 'FILE_FOLDER_TYPE'
);

INSERT INTO `t_dict` (`dict_name`, `dict_code`, `remark`, `disabled_flag`)
SELECT '更新日志类型', 'CHANGE_LOG_TYPE', '更新日志模块使用的类型字典', 0
WHERE NOT EXISTS (
    SELECT 1
    FROM `t_dict`
    WHERE `dict_code` = 'CHANGE_LOG_TYPE'
);

-- ----------------------------
-- 2. 补齐文件夹类型字典项
-- useDictOptions 当前按 sort_order 倒序渲染，这里按期望显示顺序设置排序值
-- ----------------------------
INSERT INTO `t_dict_data`
(`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT dict_info.`dict_id`, '1', '通用', 'primary', '文件夹类型：通用', 400, 0
FROM `t_dict` dict_info
WHERE dict_info.`dict_code` = 'FILE_FOLDER_TYPE'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` data_info
      WHERE data_info.`dict_id` = dict_info.`dict_id`
        AND data_info.`data_value` = '1'
  );

INSERT INTO `t_dict_data`
(`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT dict_info.`dict_id`, '2', '公告', 'warning', '文件夹类型：公告', 300, 0
FROM `t_dict` dict_info
WHERE dict_info.`dict_code` = 'FILE_FOLDER_TYPE'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` data_info
      WHERE data_info.`dict_id` = dict_info.`dict_id`
        AND data_info.`data_value` = '2'
  );

INSERT INTO `t_dict_data`
(`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT dict_info.`dict_id`, '3', '帮助中心', 'success', '文件夹类型：帮助中心', 200, 0
FROM `t_dict` dict_info
WHERE dict_info.`dict_code` = 'FILE_FOLDER_TYPE'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` data_info
      WHERE data_info.`dict_id` = dict_info.`dict_id`
        AND data_info.`data_value` = '3'
  );

INSERT INTO `t_dict_data`
(`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT dict_info.`dict_id`, '4', '意见反馈', 'info', '文件夹类型：意见反馈', 100, 0
FROM `t_dict` dict_info
WHERE dict_info.`dict_code` = 'FILE_FOLDER_TYPE'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` data_info
      WHERE data_info.`dict_id` = dict_info.`dict_id`
        AND data_info.`data_value` = '4'
  );

-- ----------------------------
-- 3. 补齐更新日志类型字典项
-- ----------------------------
INSERT INTO `t_dict_data`
(`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT dict_info.`dict_id`, '1', '重大更新', 'danger', '更新日志类型：重大更新', 300, 0
FROM `t_dict` dict_info
WHERE dict_info.`dict_code` = 'CHANGE_LOG_TYPE'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` data_info
      WHERE data_info.`dict_id` = dict_info.`dict_id`
        AND data_info.`data_value` = '1'
  );

INSERT INTO `t_dict_data`
(`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT dict_info.`dict_id`, '2', '功能更新', 'primary', '更新日志类型：功能更新', 200, 0
FROM `t_dict` dict_info
WHERE dict_info.`dict_code` = 'CHANGE_LOG_TYPE'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` data_info
      WHERE data_info.`dict_id` = dict_info.`dict_id`
        AND data_info.`data_value` = '2'
  );

INSERT INTO `t_dict_data`
(`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT dict_info.`dict_id`, '3', 'Bug修复', 'success', '更新日志类型：Bug修复', 100, 0
FROM `t_dict` dict_info
WHERE dict_info.`dict_code` = 'CHANGE_LOG_TYPE'
  AND NOT EXISTS (
      SELECT 1
      FROM `t_dict_data` data_info
      WHERE data_info.`dict_id` = dict_info.`dict_id`
        AND data_info.`data_value` = '3'
  );

-- ----------------------------
-- 4. 修正更新日志查询权限，保证前后端权限码一致
-- ----------------------------
UPDATE `t_menu`
SET `api_perms` = 'support:changeLog:query',
    `web_perms` = 'support:changeLog:query',
    `context_menu_id` = 152,
    `update_user_id` = 1
WHERE `menu_id` = 190;

-- ----------------------------
-- 5. 补齐帮助文档目录删除按钮权限
-- 后端当前未做权限注解，这里先补前端显隐权限码，便于统一权限模型
-- ----------------------------
INSERT INTO `t_menu`
(`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `update_user_id`)
VALUES
(305, '删除目录', 3, 147, 3, NULL, NULL, 1, 'support:helpDocCatalog:delete', 'support:helpDocCatalog:delete', NULL, 147, 0, NULL, 0, 1, 0, 0, 1, 1)
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
-- 6. 补齐管理员角色授权
-- ----------------------------
INSERT INTO `t_role_menu` (`role_id`, `menu_id`)
SELECT 1, tmp.`menu_id`
FROM (
    SELECT 147 AS `menu_id`
    UNION ALL SELECT 152
    UNION ALL SELECT 193
    UNION ALL SELECT 168
    UNION ALL SELECT 169
    UNION ALL SELECT 170
    UNION ALL SELECT 171
    UNION ALL SELECT 190
    UNION ALL SELECT 191
    UNION ALL SELECT 192
    UNION ALL SELECT 198
    UNION ALL SELECT 200
    UNION ALL SELECT 201
    UNION ALL SELECT 202
    UNION ALL SELECT 207
    UNION ALL SELECT 305
) tmp
WHERE NOT EXISTS (
    SELECT 1
    FROM `t_role_menu` role_menu
    WHERE role_menu.`role_id` = 1
      AND role_menu.`menu_id` = tmp.`menu_id`
);

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;

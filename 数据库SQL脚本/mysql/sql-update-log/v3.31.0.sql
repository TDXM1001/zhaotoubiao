-- =========================================================
-- v3.31.0 增量脚本
-- 目标：补齐前端对接所依赖的参数、字典、部门/员工/角色/菜单/登录相关权限码
-- 说明：脚本按幂等方式编写，可重复执行
-- 编码：utf8mb4
-- =========================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

START TRANSACTION;

-- ----------------------------
-- 1. 参数配置
-- ----------------------------
UPDATE `t_config`
SET `config_name` = '万能密码',
    `config_value` = '1024ok',
    `remark` = '一键登录调试用参数，保持与当前后端一致'
WHERE `config_key` = 'super_password';

INSERT INTO `t_config` (`config_name`, `config_key`, `config_value`, `remark`)
SELECT '万能密码', 'super_password', '1024ok', '一键登录调试用参数，保持与当前后端一致'
WHERE NOT EXISTS (
    SELECT 1
    FROM `t_config`
    WHERE `config_key` = 'super_password'
);

UPDATE `t_config`
SET `config_name` = '三级等保',
    `config_value` = '{\n\t"fileDetectFlag":true,\n\t"loginActiveTimeoutMinutes":30,\n\t"loginFailLockMinutes":30,\n\t"loginFailMaxTimes":3,\n\t"maxUploadFileSizeMb":30,\n\t"passwordComplexityEnabled":true,\n\t"regularChangePasswordMonths":3,\n\t"regularChangePasswordNotAllowRepeatTimes":3,\n\t"twoFactorLoginEnabled":false\n}',
    `remark` = '三级等保配置，供登录安全和运维页面使用'
WHERE `config_key` = 'level3_protect_config';

INSERT INTO `t_config` (`config_name`, `config_key`, `config_value`, `remark`)
SELECT '三级等保', 'level3_protect_config', '{\n\t"fileDetectFlag":true,\n\t"loginActiveTimeoutMinutes":30,\n\t"loginFailLockMinutes":30,\n\t"loginFailMaxTimes":3,\n\t"maxUploadFileSizeMb":30,\n\t"passwordComplexityEnabled":true,\n\t"regularChangePasswordMonths":3,\n\t"regularChangePasswordNotAllowRepeatTimes":3,\n\t"twoFactorLoginEnabled":false\n}', '三级等保配置，供登录安全和运维页面使用'
WHERE NOT EXISTS (
    SELECT 1
    FROM `t_config`
    WHERE `config_key` = 'level3_protect_config'
);

-- ----------------------------
-- 2. 数据字典
-- ----------------------------
INSERT INTO `t_dict` (`dict_name`, `dict_code`, `remark`, `disabled_flag`)
VALUES ('字典回显样式', 'DICT_DATA_STYLE', '字典项展示样式，替换前端写死的样式列表', 0)
ON DUPLICATE KEY UPDATE
  `dict_name` = VALUES(`dict_name`),
  `remark` = VALUES(`remark`),
  `disabled_flag` = VALUES(`disabled_flag`);

INSERT INTO `t_dict` (`dict_name`, `dict_code`, `remark`, `disabled_flag`)
VALUES ('性别', 'GENDER', '员工性别，供员工和登录人信息展示使用', 0)
ON DUPLICATE KEY UPDATE
  `dict_name` = VALUES(`dict_name`),
  `remark` = VALUES(`remark`),
  `disabled_flag` = VALUES(`disabled_flag`);

INSERT INTO `t_dict` (`dict_name`, `dict_code`, `remark`, `disabled_flag`)
VALUES ('菜单类型', 'MENU_TYPE', '目录、菜单、功能点三类菜单类型', 0)
ON DUPLICATE KEY UPDATE
  `dict_name` = VALUES(`dict_name`),
  `remark` = VALUES(`remark`),
  `disabled_flag` = VALUES(`disabled_flag`);

INSERT INTO `t_dict` (`dict_name`, `dict_code`, `remark`, `disabled_flag`)
VALUES ('权限类型', 'MENU_PERMS_TYPE', '当前系统可用的权限模式', 0)
ON DUPLICATE KEY UPDATE
  `dict_name` = VALUES(`dict_name`),
  `remark` = VALUES(`remark`),
  `disabled_flag` = VALUES(`disabled_flag`);

INSERT INTO `t_dict` (`dict_name`, `dict_code`, `remark`, `disabled_flag`)
VALUES ('登录结果', 'LOGIN_LOG_RESULT', '登录成功、失败、退出的结果字典', 0)
ON DUPLICATE KEY UPDATE
  `dict_name` = VALUES(`dict_name`),
  `remark` = VALUES(`remark`),
  `disabled_flag` = VALUES(`disabled_flag`);

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, 'default', '默认', 'default', '字典回显默认样式', 6, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'DICT_DATA_STYLE'
  AND NOT EXISTS (
      SELECT 1 FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = 'default'
  );

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, 'primary', '主要', 'primary', '字典回显主要样式', 5, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'DICT_DATA_STYLE'
  AND NOT EXISTS (
      SELECT 1 FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = 'primary'
  );

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, 'success', '成功', 'success', '字典回显成功样式', 4, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'DICT_DATA_STYLE'
  AND NOT EXISTS (
      SELECT 1 FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = 'success'
  );

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, 'info', '信息', 'info', '字典回显信息样式', 3, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'DICT_DATA_STYLE'
  AND NOT EXISTS (
      SELECT 1 FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = 'info'
  );

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, 'warning', '警告', 'warning', '字典回显警告样式', 2, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'DICT_DATA_STYLE'
  AND NOT EXISTS (
      SELECT 1 FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = 'warning'
  );

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, 'danger', '危险', 'danger', '字典回显危险样式', 1, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'DICT_DATA_STYLE'
  AND NOT EXISTS (
      SELECT 1 FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = 'danger'
  );

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, '0', '未知', '', '员工性别：未知', 3, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'GENDER'
  AND NOT EXISTS (
      SELECT 1 FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = '0'
  );

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, '1', '男', '', '员工性别：男', 2, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'GENDER'
  AND NOT EXISTS (
      SELECT 1 FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = '1'
  );

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, '2', '女', '', '员工性别：女', 1, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'GENDER'
  AND NOT EXISTS (
      SELECT 1 FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = '2'
  );

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, '1', '目录', '', '菜单类型：目录', 3, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'MENU_TYPE'
  AND NOT EXISTS (
      SELECT 1 FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = '1'
  );

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, '2', '菜单', '', '菜单类型：菜单', 2, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'MENU_TYPE'
  AND NOT EXISTS (
      SELECT 1 FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = '2'
  );

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, '3', '功能点', '', '菜单类型：功能点', 1, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'MENU_TYPE'
  AND NOT EXISTS (
      SELECT 1 FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = '3'
  );

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, '1', 'Sa-Token模式', '', '权限类型：Sa-Token模式', 1, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'MENU_PERMS_TYPE'
  AND NOT EXISTS (
      SELECT 1 FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = '1'
  );

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, '0', '登录成功', '', '登录日志：成功', 3, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'LOGIN_LOG_RESULT'
  AND NOT EXISTS (
      SELECT 1 FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = '0'
  );

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, '1', '登录失败', '', '登录日志：失败', 2, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'LOGIN_LOG_RESULT'
  AND NOT EXISTS (
      SELECT 1 FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = '1'
  );

INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT d.`dict_id`, '2', '退出登录', '', '登录日志：退出', 1, 0
FROM `t_dict` d
WHERE d.`dict_code` = 'LOGIN_LOG_RESULT'
  AND NOT EXISTS (
      SELECT 1 FROM `t_dict_data` dd
      WHERE dd.`dict_id` = d.`dict_id`
        AND dd.`data_value` = '2'
  );

-- ----------------------------
-- 3. 菜单与权限码
-- ----------------------------
INSERT INTO `t_menu`
(`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `update_user_id`)
VALUES
(45, '组织架构', 1, 0, 3, '/organization', NULL, NULL, NULL, NULL, 'UserSwitchOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(50, '系统设置', 1, 0, 6, '/setting', NULL, NULL, NULL, NULL, 'SettingOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(213, '网络安全', 1, 0, 5, NULL, NULL, 1, NULL, NULL, 'SafetyCertificateOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, 1),
(219, '部门管理', 2, 45, 1, '/organization/department', '/system/department/department-list.vue', 1, NULL, NULL, 'ApartmentOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(46, '员工管理', 2, 45, 3, '/organization/employee', '/system/employee/index.vue', NULL, NULL, NULL, 'AuditOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(76, '角色管理', 2, 45, 4, '/organization/role', '/system/role/index.vue', NULL, NULL, NULL, 'SlidersOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(26, '菜单管理', 2, 50, 1, '/menu/list', '/system/menu/menu-list.vue', NULL, NULL, NULL, 'CopyOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, 1),
(109, '参数配置', 2, 50, 3, '/config/config-list', '/support/config/config-list.vue', NULL, NULL, NULL, 'AntDesignOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(110, '数据字典', 2, 50, 4, '/setting/dict', '/support/dict/index.vue', NULL, NULL, NULL, 'BarcodeOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(81, '用户操作记录', 2, 213, 6, '/support/operate-log/operate-log-list', '/support/operate-log/operate-log-list.vue', NULL, NULL, NULL, 'VideoCameraOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(143, '登录登出记录', 2, 213, 5, '/support/login-log/login-log-list', '/support/login-log/login-log-list.vue', NULL, NULL, NULL, 'LoginOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(214, '登录失败锁定', 2, 213, 4, '/support/login-fail', '/support/login-fail/login-fail-list.vue', 1, NULL, NULL, 'LockOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, 1),
(86, '添加部门', 3, 219, 1, NULL, NULL, 1, 'system:department:add', 'system:department:add', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(87, '修改部门', 3, 219, 2, NULL, NULL, 1, 'system:department:update', 'system:department:update', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(88, '删除部门', 3, 219, 3, NULL, NULL, 1, 'system:department:delete', 'system:department:delete', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(91, '添加员工', 3, 46, NULL, NULL, NULL, 1, 'system:employee:add', 'system:employee:add', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(92, '编辑员工', 3, 46, NULL, NULL, NULL, 1, 'system:employee:update', 'system:employee:update', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(93, '启用/禁用员工', 3, 46, NULL, NULL, NULL, 1, 'system:employee:disabled', 'system:employee:disabled', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(94, '调整员工部门', 3, 46, NULL, NULL, NULL, 1, 'system:employee:department:update', 'system:employee:department:update', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(95, '重置密码', 3, 46, NULL, NULL, NULL, 1, 'system:employee:password:reset', 'system:employee:password:reset', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(96, '删除员工', 3, 46, NULL, NULL, NULL, 1, 'system:employee:delete', 'system:employee:delete', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(97, '添加角色', 3, 76, NULL, NULL, NULL, 1, 'system:role:add', 'system:role:add', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(98, '删除角色', 3, 76, NULL, NULL, NULL, 1, 'system:role:delete', 'system:role:delete', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(99, '编辑角色', 3, 76, NULL, NULL, NULL, 1, 'system:role:update', 'system:role:update', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(100, '更新数据范围', 3, 76, NULL, NULL, NULL, 1, 'system:role:dataScope:update', 'system:role:dataScope:update', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(101, '批量移除员工', 3, 76, NULL, NULL, NULL, 1, 'system:role:employee:batch:delete', 'system:role:employee:batch:delete', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(102, '移除员工', 3, 76, NULL, NULL, NULL, 1, 'system:role:employee:delete', 'system:role:employee:delete', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(103, '添加员工', 3, 76, NULL, NULL, NULL, 1, 'system:role:employee:add', 'system:role:employee:add', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(104, '修改权限', 3, 76, NULL, NULL, NULL, 1, 'system:role:menu:update', 'system:role:menu:update', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(40, '删除', 3, 26, NULL, NULL, NULL, 1, 'system:menu:batchDelete', 'system:menu:batchDelete', NULL, 26, 0, NULL, 0, 1, 0, 0, 1, 1),
(105, '添加', 3, 26, NULL, NULL, NULL, 1, 'system:menu:add', 'system:menu:add', NULL, 26, 0, NULL, 0, 1, 0, 0, 1, 1),
(106, '编辑', 3, 26, NULL, NULL, NULL, 1, 'system:menu:update', 'system:menu:update', NULL, 26, 0, NULL, 0, 1, 0, 0, 1, 1),
(159, '查询', 3, 110, NULL, NULL, NULL, 1, 'support:dict:query', 'support:dict:query', NULL, 110, 0, NULL, 0, 1, 0, 0, 1, 1),
(160, '添加', 3, 110, NULL, NULL, NULL, 1, 'support:dict:add', 'support:dict:add', NULL, 110, 0, NULL, 0, 1, 0, 0, 1, 1),
(161, '更新', 3, 110, NULL, NULL, NULL, 1, 'support:dict:update', 'support:dict:update', NULL, 110, 0, NULL, 0, 1, 0, 0, 1, 1),
(162, '删除', 3, 110, NULL, NULL, NULL, 1, 'support:dict:delete', 'support:dict:delete', NULL, 110, 0, NULL, 0, 1, 0, 0, 1, 1),
(163, '新建', 3, 109, NULL, NULL, NULL, 1, 'support:config:add', 'support:config:add', NULL, 109, 0, NULL, 0, 1, 0, 0, 1, 1),
(164, '编辑', 3, 109, NULL, NULL, NULL, 1, 'support:config:update', 'support:config:update', NULL, 109, 0, NULL, 0, 1, 0, 0, 1, 1),
(199, '查询', 3, 109, NULL, NULL, NULL, 1, 'support:config:query', 'support:config:query', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(203, '查询', 3, 143, NULL, NULL, NULL, 1, 'support:loginLog:query', 'support:loginLog:query', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(204, '查询', 3, 81, NULL, NULL, NULL, 1, 'support:operateLog:query', 'support:operateLog:query', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(205, '详情', 3, 81, NULL, NULL, NULL, 1, 'support:operateLog:detail', 'support:operateLog:detail', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, 1),
(252, '启用/禁用', 3, 110, NULL, NULL, NULL, 1, 'support:dict:updateDisabled', 'support:dict:updateDisabled', NULL, 110, 0, NULL, 0, 1, 0, 0, 1, 1),
(253, '查询字典数据', 3, 110, NULL, NULL, NULL, 1, 'support:dictData:query', 'support:dictData:query', NULL, 110, 0, NULL, 0, 1, 0, 0, 1, 1),
(254, '添加字典数据', 3, 110, NULL, NULL, NULL, 1, 'support:dictData:add', 'support:dictData:add', NULL, 110, 0, NULL, 0, 1, 0, 0, 1, 1),
(255, '更新字典数据', 3, 110, NULL, NULL, NULL, 1, 'support:dictData:update', 'support:dictData:update', NULL, 110, 0, NULL, 0, 1, 0, 0, 1, 1),
(256, '删除字典数据', 3, 110, NULL, NULL, NULL, 1, 'support:dictData:delete', 'support:dictData:delete', NULL, 110, 0, NULL, 0, 1, 0, 0, 1, 1),
(257, '启用/禁用字典数据', 3, 110, NULL, NULL, NULL, 1, 'support:dictData:updateDisabled', 'support:dictData:updateDisabled', NULL, 110, 0, NULL, 0, 1, 0, 0, 1, 1)
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
-- 4. 默认管理员角色的菜单权限
-- ----------------------------
INSERT INTO `t_role_menu` (`role_id`, `menu_id`)
SELECT 1, tmp.`menu_id`
FROM (
    SELECT 45 AS `menu_id`
    UNION ALL SELECT 50
    UNION ALL SELECT 213
    UNION ALL SELECT 219
    UNION ALL SELECT 46
    UNION ALL SELECT 76
    UNION ALL SELECT 26
    UNION ALL SELECT 109
    UNION ALL SELECT 110
    UNION ALL SELECT 81
    UNION ALL SELECT 143
    UNION ALL SELECT 214
    UNION ALL SELECT 86
    UNION ALL SELECT 87
    UNION ALL SELECT 88
    UNION ALL SELECT 91
    UNION ALL SELECT 92
    UNION ALL SELECT 93
    UNION ALL SELECT 94
    UNION ALL SELECT 95
    UNION ALL SELECT 96
    UNION ALL SELECT 97
    UNION ALL SELECT 98
    UNION ALL SELECT 99
    UNION ALL SELECT 100
    UNION ALL SELECT 101
    UNION ALL SELECT 102
    UNION ALL SELECT 103
    UNION ALL SELECT 104
    UNION ALL SELECT 40
    UNION ALL SELECT 105
    UNION ALL SELECT 106
    UNION ALL SELECT 159
    UNION ALL SELECT 160
    UNION ALL SELECT 161
    UNION ALL SELECT 162
    UNION ALL SELECT 163
    UNION ALL SELECT 164
    UNION ALL SELECT 199
    UNION ALL SELECT 203
    UNION ALL SELECT 204
    UNION ALL SELECT 205
    UNION ALL SELECT 252
    UNION ALL SELECT 253
    UNION ALL SELECT 254
    UNION ALL SELECT 255
    UNION ALL SELECT 256
    UNION ALL SELECT 257
) tmp
WHERE NOT EXISTS (
    SELECT 1
    FROM `t_role_menu` rm
    WHERE rm.`role_id` = 1
      AND rm.`menu_id` = tmp.`menu_id`
);

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- 5. 插入字典定义
-- ----------------------------
INSERT INTO `t_dict` (`dict_name`, `dict_code`, `remark`, `disabled_flag`) 
VALUES ('用户类型', 'USER_TYPE', '系统用户类型：员工、客户等', 0);

-- ----------------------------
-- 6. 插入字典项（1 为员工）
-- ----------------------------
INSERT INTO `t_dict_data` (`dict_id`, `data_value`, `data_label`, `data_style`, `remark`, `sort_order`, `disabled_flag`)
SELECT dict_id, '1', '员工', 'primary', '后台管理端员工', 1, 0 
FROM `t_dict` WHERE `dict_code` = 'USER_TYPE';

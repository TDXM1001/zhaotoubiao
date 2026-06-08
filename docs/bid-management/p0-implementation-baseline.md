# 招投标管理系统 P0 实施基线

## 目的

本文件冻结招投标域 P0 的实施口径，避免后续多 agent 并行开发时在包路径、菜单路径、API 风格、表名和状态动作矩阵上分叉。

本文件优先级高于早期设计文档中的路径建议；历史决策修订见 `../decisions/ADR-004-bid-p0-implementation-baseline.md`。

## 当前完成度

按一期总设计估算，当前已实现约 `25% - 35%`，仍有约 `65% - 75%` 未实现。

按 P1 MVP-1 范围估算，当前已实现约 `50% - 60%`。项目、标段、报名主线已经起步，但招标文件、投标提交、门户和附件版本能力仍未闭环。

## 已落地范围

- 后端模块：`project`、`lot`、`registration`、`common/workflow_history`
- 前端页面：项目管理、标段管理、报名管理
- 物理表：`t_bid_project`、`t_bid_lot`、`t_bid_project_member`、`t_bid_workflow_history`、`t_bid_registration`
- 增量 SQL：`数据库SQL脚本/mysql/sql-update-log/v3.34.0.sql`、`数据库SQL脚本/mysql/sql-update-log/v3.34.1.sql`
- 菜单与功能点：`招投标管理` 一级菜单下的项目、标段、报名页面与功能权限
- 基础流程：项目建档、标段维护、项目发布、标段截标、标段作废、报名审批、报名驳回、报名取消
- Canonical API：项目、标段、报名已补资源化端点，并保留旧接口兼容

## 未实现范围

- `bid-tender`：招标方案、招标文件版本、公告发布、澄清答疑
- `bid-party`：供应商关系、代理机构关系、外部主体快照
- `bid-submission`：投标主记录、投标文件版本、投标回执
- `bid-opening`：开标会、签到、唱标记录、开标异常
- `bid-evaluation`：评标委员会、评分模板、专家评分、汇总结果
- `bid-award`：定标确认、中标结果、结果公示
- `bid-archive`：归档包、归档查询、结果导出
- `bid-portal`：供应商轻量门户、门户准入、门户数据隔离
- 附件版本模型：招标文件版本、投标文件版本、归档附件版本
- 消息与待办：流程提醒、站内信、待办列表
- 统一错误码：`BidErrorCode` 区间和全域错误码注册
- Canonical API：招标文件、投标、开标、评标、定标、归档、门户等后续资源仍待实现

## 路径基线

后端：

```text
zhaotoubiao/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/bid/
zhaotoubiao/sa-admin/src/main/resources/mapper/system/bid/
```

前端：

```text
vue-vben-admin-main/apps/web-ele/src/api/system/bid.ts
vue-vben-admin-main/apps/web-ele/src/views/system/bid/
```

菜单：

```text
path      = /system/bid/{resource}/{page}
component = /system/bid/{resource}/{resource}-{page}.vue
```

HTTP：

```text
/bid/**
```

不得在 P0 新增第二套 `module/business/bid`、`src/api/bid`、`src/views/bid` 实现路径。

## API 基线

新模块统一使用资源化契约：

```text
POST /bid/{resources}/search
GET  /bid/{resources}/{id}
POST /bid/{resources}
PUT  /bid/{resources}/{id}
POST /bid/{resources}/{id}/actions/{kebab-action}
```

已落地旧接口保留为兼容适配层：

```text
/bid/project/queryPage
/bid/project/get/{projectId}
/bid/project/queryList
/bid/project/add
/bid/project/update
/bid/project/submitPlan
/bid/project/publish
/bid/project/cancel
/bid/lot/queryPage
/bid/lot/get/{lotId}
/bid/lot/queryByProjectId/{projectId}
/bid/lot/add
/bid/lot/update
/bid/lot/closeBid
/bid/lot/void
/bid/registration/queryPage
/bid/registration/get/{registrationId}
/bid/registration/add
/bid/registration/approve
/bid/registration/reject
/bid/registration/cancel
```

兼容接口不得承载新业务能力；后续新增 canonical 端点时必须复用同一 service，并补回归测试验证新旧返回一致。

## 数据库基线

新库初始化顺序：

```text
smart_admin_v3.sql
v3.34.0.sql
v3.34.1.sql
```

P0 验收必须确认：

- 5 张 `t_bid_*` 表存在
- `BID_PROJECT_TYPE`、`BID_PROCUREMENT_MODE`、`BID_EVALUATION_MODE`、`BID_AWARD_MODE`、`BID_PROJECT_MEMBER_ROLE`、`BID_REGISTRATION_TYPE` 字典存在
- 招投标菜单与功能点存在，菜单 ID 范围为 `731 - 759`

后续阶段表不得计入 P0 缺陷，包括：

- `t_bid_tender_version`
- `t_bid_submission`
- `t_bid_opening`
- `t_bid_evaluation`
- `t_bid_award`
- `t_bid_attachment`
- `t_bid_archive`

## 状态与动作基线

流程状态以 Java 枚举为主，不做数据库可运营码表。

项目状态：

| 状态 | 含义 |
| --- | --- |
| `DRAFT` | 草稿 |
| `PLANNED` | 已立项 / 已提交方案 |
| `PUBLISHED` | 已发布 |
| `CANCELLED` | 已取消 |
| `ARCHIVED` | 已归档 |

标段状态：

| 状态 | 含义 |
| --- | --- |
| `DRAFT` | 草稿 |
| `BIDDING` | 投标中 |
| `BID_CLOSED` | 已截标 |
| `VOIDED` | 已作废 |

报名状态：

| 状态 | 含义 |
| --- | --- |
| `SUBMITTED` | 已提交 |
| `QUALIFIED` | 资格通过 |
| `REJECTED` | 已驳回 |
| `CANCELLED` | 已取消 |

动作矩阵字段必须包括：

- `resource`
- `from_status`
- `to_status`
- `action_code`
- `allowedActions`
- `api_perm`
- `web_perm`
- `legacy_endpoint`
- `canonical_endpoint`

当前已知动作命名差异必须在矩阵中显式记录，例如 `bid:project:publish` 权限对应 `publish-project` 动作码。

已落地动作矩阵：

| resource | from_status | to_status | action_code | allowedActions | api_perm | web_perm | legacy_endpoint | canonical_endpoint |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| project | `DRAFT` | `PLANNED` | `submit-plan` | `submit-plan` | `bid:project:submit-plan` | `bid:project:submit-plan` | `POST /bid/project/submitPlan` | `POST /bid/projects/{projectId}/actions/submit-plan` |
| project | `PLANNED` | `PUBLISHED` | `publish-project` | `publish-project` | `bid:project:publish` | `bid:project:publish` | `POST /bid/project/publish` | `POST /bid/projects/{projectId}/actions/publish-project` |
| project | `DRAFT/PLANNED/PUBLISHED` | `CANCELLED` | `cancel-project` | `cancel-project` | `bid:project:cancel` | `bid:project:cancel` | `POST /bid/project/cancel` | `POST /bid/projects/{projectId}/actions/cancel-project` |
| lot | `BIDDING` | `BID_CLOSED` | `close-bid` | `close-bid` | `bid:lot:close-bid` | `bid:lot:close-bid` | `POST /bid/lot/closeBid` | `POST /bid/lots/{lotId}/actions/close-bid` |
| lot | `DRAFT/BIDDING` | `VOIDED` | `void-lot` | `void-lot` | `bid:lot:void` | `bid:lot:void` | `POST /bid/lot/void` | `POST /bid/lots/{lotId}/actions/void-lot` |
| registration | `SUBMITTED` | `QUALIFIED` | `approve-registration` | `approve-registration` | `bid:registration:approve` | `bid:registration:approve` | `POST /bid/registration/approve` | `POST /bid/registrations/{registrationId}/actions/approve-registration` |
| registration | `SUBMITTED` | `REJECTED` | `reject-registration` | `reject-registration` | `bid:registration:reject` | `bid:registration:reject` | `POST /bid/registration/reject` | `POST /bid/registrations/{registrationId}/actions/reject-registration` |
| registration | `SUBMITTED/QUALIFIED/REJECTED` | `CANCELLED` | `cancel-registration` | `cancel-registration` | `bid:registration:cancel` | `bid:registration:cancel` | `POST /bid/registration/cancel` | `POST /bid/registrations/{registrationId}/actions/cancel-registration` |

## P0 验收命令

后端编译：

```powershell
cd E:\my-project\zhaotoubiao\zhaotoubiao
mvn -q -pl sa-admin -am -DskipTests compile
```

后端最小测试：

```powershell
cd E:\my-project\zhaotoubiao\zhaotoubiao
mvn -q -pl sa-admin -am -Dtest=AdminApplicationTest test
```

前端类型检查：

```powershell
cd E:\my-project\zhaotoubiao\vue-vben-admin-main
pnpm -F @vben/web-ele run typecheck
```

动态菜单回归：

```powershell
cd E:\my-project\zhaotoubiao\vue-vben-admin-main
pnpm exec vitest run apps/web-ele/src/api/core/__tests__/smart-admin-menu.test.ts --dom
```

前端构建：

```powershell
cd E:\my-project\zhaotoubiao\vue-vben-admin-main
pnpm run build:ele
```

## 下一步切分

P0 冻结后，建议按以下互不重叠的工作流并行：

- 后端流：继续为招标文件、投标、开评定标等后续资源补 canonical controller，旧接口适配到同一 service
- 数据库流：设计 `t_bid_tender_version`、`t_bid_submission`、附件版本关系
- 前端流：按 `views/system/bid/{resource}` 新增招标文件、投标、门户页面壳
- 质量流：补动作矩阵、权限矩阵、菜单路径单测和 API 兼容回归

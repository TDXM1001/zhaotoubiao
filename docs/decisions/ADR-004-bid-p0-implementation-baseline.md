# ADR-004: 招投标 P0 实施基线冻结

## Status

Accepted

## Date

2026-06-08

## Context

ADR-001 决定沿用现有单体后端与独立前端，ADR-003 决定招投标域采用业务状态机与资源化动作接口。

首批落地实现已经形成事实基线：

- 后端代码位于 `sa-admin/src/main/java/net/lab1024/sa/admin/module/system/bid`
- MyBatis Mapper 位于 `sa-admin/src/main/resources/mapper/system/bid`
- 前端页面位于 `apps/web-ele/src/views/system/bid`
- 前端 API client 位于 `apps/web-ele/src/api/system/bid.ts`
- 菜单路径使用 `/system/bid/**`
- HTTP 接口命名空间使用 `/bid/**`
- 物理表使用 `t_bid_*`

旧设计文档中仍存在 `module/business/bid`、`src/api/bid`、`src/views/bid`、菜单 `bid/...` 等建议路径。如果继续保留多套口径，后续实现会在包路径、菜单组件路径、接口契约和权限码上分叉。

同时，当前项目、标段、报名模块已经使用 `/bid/project/queryPage`、`/bid/lot/add`、`/bid/registration/approve` 等旧式接口；直接删除会破坏已落地前后端联调面。

## Decision

### 实施路径

P0 起，招投标域实现路径冻结为当前已落地口径：

- 后端包：`net.lab1024.sa.admin.module.system.bid`
- Mapper 路径：`mapper/system/bid`
- 前端页面：`apps/web-ele/src/views/system/bid/**/*`
- 前端 API：`apps/web-ele/src/api/system/bid.ts`
- 内部主系统菜单路径：`/system/bid/{resource}/{page}`
- 内部主系统菜单组件：`/system/bid/{resource}/{resource}-{page}.vue`
- HTTP API 命名空间：`/bid/**`

旧文档中的 `module/business/bid`、`src/api/bid`、`src/views/bid` 仅作为历史建议，不再作为实施目标。ADR-001 的单体形态决定继续有效，其中业务域物理路径由本 ADR 修订。

### API 风格

ADR-003 的资源化契约继续作为目标契约，新模块统一采用：

```text
POST /bid/{resources}/search
GET  /bid/{resources}/{id}
POST /bid/{resources}
PUT  /bid/{resources}/{id}
POST /bid/{resources}/{id}/actions/{kebab-action}
```

已落地旧接口保留为兼容适配层，不再扩展新能力，后续迁移到同一 service 实现。

示例映射：

```text
POST /bid/project/queryPage      -> POST /bid/projects/search
GET  /bid/project/get/{id}       -> GET  /bid/projects/{id}
POST /bid/project/add            -> POST /bid/projects
POST /bid/project/update         -> PUT  /bid/projects/{id}
POST /bid/project/submitPlan     -> POST /bid/projects/{id}/actions/submit-plan
POST /bid/project/publish        -> POST /bid/projects/{id}/actions/publish-project
POST /bid/lot/closeBid           -> POST /bid/lots/{id}/actions/close-bid
POST /bid/registration/approve   -> POST /bid/registrations/{id}/actions/approve-registration
```

权限码、`allowedActions`、`t_bid_workflow_history.action_code` 必须写入同一动作矩阵。权限码保持现有体系，不为同一动作创建第二套权限。

### 数据库与状态码

物理表名冻结为 `t_bid_*`。P0 已落地表：

- `t_bid_project`
- `t_bid_lot`
- `t_bid_project_member`
- `t_bid_workflow_history`
- `t_bid_registration`

建库顺序冻结为：

```text
smart_admin_v3.sql -> v3.34.0.sql -> v3.34.1.sql
```

流程状态不新建数据库运营码表，采用 Java 枚举 + 文档码表 + `t_bid_workflow_history.action_code` 留痕。字典模块只用于项目类型、采购方式、评标方式、定标方式、报名方式、成员角色等非流程状态字段。

### 前端菜单

内部主系统页面继续放在 `views/system/bid`。菜单规则：

- `path` 使用 `/system/bid/{resource}/{page}`
- `component` 使用 `/system/bid/{resource}/{resource}-{page}.vue`
- `create`、`edit`、`detail` 作为列表页隐藏子页面，`activePath` 指向对应列表
- 后端菜单 `component` 不写 `src/views` 前缀
- 供应商门户后续单独冻结为 `/bid-portal/**` 或独立入口，不混入内部主系统 `/system/bid/**`

## Consequences

- 后续实现不再迁移首批代码到 `module/business/bid` 或 `views/bid`，避免无收益搬迁。
- 新模块必须按资源化契约设计接口；旧接口仅用于兼容已有页面。
- 文档、SQL、前端菜单和后端包路径有唯一 P0 基线，便于多 agent 并行开发。
- 后续若决定迁移包路径或 API 风格，必须另开 ADR，并提供兼容期、迁移清单和回归测试。

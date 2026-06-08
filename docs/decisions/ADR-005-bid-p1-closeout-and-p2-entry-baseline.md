# ADR-005: 招投标 P1 收口与 P2 进入基线

## Status

Proposed

## Date

2026-06-08

## Context

ADR-004 冻结了招投标 P0 的实施路径、API 风格、数据库和菜单基线。随后 P1 代码面已经明显推进：

- 后端已存在 `tender`、`submission`、`attachment`、`portal` 模块。
- 前端已存在招标文件、投标管理、供应商门户页面。
- `v3.35.0.sql` 已新增招标文件版本、投标、投标版本、附件关联和门户账号表。
- contract tests、前端菜单 / 路由测试、类型检查和浏览器页面可达性验证已通过。

但这些证据主要证明“接口契约存在、页面可打开、类型可编译”。当前仍缺少真实数据链路、业务附件关联、门户数据隔离和开标前冻结规则的验收。

同时，早期 P2 文档仍存在 `actions/start`、`actions/complete`、`actions/publish` 等泛化动作口径。P0/P1 已经实际采用资源专属 kebab action，例如 `publish-tender`、`submit-bid`、`withdraw-bid`。如果 P2 继续沿用旧口径，动作码、权限码、`allowedActions` 和 workflow history 会再次分叉。

## Decision

### P1 状态

P1 判定为“主要代码面已实现，但未验收收口”。进入 P2 前必须至少补齐：

- 待开标前真实业务数据闭环。
- 招投标业务附件关联入口。
- 门户供应商数据隔离回归。

P1 开工前开放问题按当前实现收口：

- 门户账号模型：采用 `t_bid_portal_account` 最小独立模型。
- 招标文件审批态：P1 不引入 `REVIEWING`，采用 `DRAFT -> ACTIVE`。
- 投标报价：使用 `t_bid_submission_version.price_amount` 结构化保存。
- 附件权限：由招投标业务接口层做二次归属校验。

### P2 范围

P2 第一轮只做最小开评定标闭环：

```text
BID_CLOSED 标段
  -> 开标冻结投标最新有效版本
  -> 最小评标汇总和推荐结果
  -> 定标确认中标供应商
  -> 门户本人结果摘要
```

P2 第一轮不包含：

- CA 签章、在线解密。
- 完整专家库、专家抽取、专家隔离评标环境。
- 复杂评分模板设计器。
- 监管平台对接。
- 归档包导出。

### P2 实施基线

P2 新模块继续沿用 ADR-004 路径：

- 后端：`sa-admin/src/main/java/net/lab1024/sa/admin/module/system/bid`
- Mapper：`sa-admin/src/main/resources/mapper/system/bid`
- 前端 API：`apps/web-ele/src/api/system/bid.ts`
- 内部页面：`apps/web-ele/src/views/system/bid`
- HTTP：`/bid/**`

P2 新模块只实现 canonical API，不新增 legacy `queryPage/add/update` 风格接口。

P2 建议新增 SQL：

```text
数据库SQL脚本/mysql/sql-update-log/v3.36.0.sql
```

P2 建议新增模块：

- `bid/opening`
- `bid/evaluation`
- `bid/award`

P2 动作命名必须使用资源专属 kebab action：

- `start-opening`
- `complete-opening`
- `abnormal-close-opening`
- `start-evaluation`
- `finalize-evaluation`
- `rollback-evaluation`
- `confirm-award`
- `rollback-award`
- `cancel-award`

不得使用 `start`、`complete`、`publish` 等泛化动作承载 P2 流程动作。

### 状态策略

流程状态继续以 Java 枚举 + 文档码表 + `t_bid_workflow_history.action_code` 为主，不新增流程状态字典。

当前代码中 P2 状态枚举已扩展：

- `BidProjectStatusEnum`：`OPENING_IN_PROGRESS`、`EVALUATING`、`AWARDED`
- `BidLotStatusEnum`：`OPENED`、`EVALUATING`、`AWARDED`

`BidSubmissionStatusEnum` 当前已有 `OPENED`。P2 第一轮先通过 opening / evaluation / award 表表达后续结果；若门户需要直接按投标状态展示结果，再增加 `EVALUATED`、`AWARDED`、`LOST`。

这不改变 P1 准入判断：状态枚举、`v3.36.0.sql` 和 opening / evaluation / award 模块已经有代码面痕迹，但进入 P2 主体前仍必须补齐 P1 真实链路、附件归属和管理端回归证据。

## Alternatives Considered

### 直接开始 P2 开标开发

- 优点：推进速度快。
- 缺点：P1 真实投标数据、附件版本和门户隔离未验收，开标模块缺少可信输入。
- 结论：拒绝。先补 P1 Ready For P2 checkpoint。

### P2 一次性实现完整专家评标

- 优点：更接近完整招投标业务。
- 缺点：专家库、评分模板、隔离环境和回避规则会显著扩大范围。
- 结论：拒绝。P2 第一轮只做最小评标汇总项。

### 沿用早期 `actions/start` / `actions/complete` 命名

- 优点：短。
- 缺点：与 P0/P1 已落地的资源专属 action code 不一致，权限码和 workflow history 容易分叉。
- 结论：拒绝。统一使用资源专属 kebab action。

## Consequences

- P1 不再被当作“待设计”任务，而是进入验收和加固阶段。
- P2 开工前有明确准入门槛，避免用未验证的 P1 数据推进开标。
- P2 API、权限码、`allowedActions`、workflow history 保持同一动作矩阵。
- 早期 P2 文档中的泛化动作命名需要以本文和 `p1-closeout-and-p2-next-step-design.md` 为准。
- 后续如果决定引入完整专家评标或独立评标隔离环境，应另开 ADR。

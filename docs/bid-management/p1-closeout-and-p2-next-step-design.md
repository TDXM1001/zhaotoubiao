# 招投标 P1 收口与 P2 下一步设计

## 状态

- 文档状态：`Draft v0.1`
- 日期：`2026-06-08`
- 输入基线：`p0-implementation-baseline.md`、`p1-mvp1-next-step-design.md`、`ADR-004-bid-p0-implementation-baseline.md`
- 当前判断：P1 主要代码面已经推进到“待开标前闭环”的实现阶段，下一步应先做 P1 验收收口，再进入 P2 开标 / 评标 / 定标最小切片。

## 目的

`p1-mvp1-next-step-design.md` 是 P1 开工前草案。当前仓库已经出现 P1 后续模块实现痕迹，继续把 Task 1 作为下一步会重复建设。

本文基于当前代码与验证结果，重新定义：

1. P1 还缺哪些验收证据。
2. 哪些问题必须在进入 P2 前收口。
3. P2 MVP-2 第一轮应该如何切片、分工和验收。

## 当前完成面

### 已完成或已冻结

- P0 路径与 API 基线已冻结：
  - 后端：`sa-admin/src/main/java/net/lab1024/sa/admin/module/system/bid`
  - Mapper：`sa-admin/src/main/resources/mapper/system/bid`
  - 前端 API：`apps/web-ele/src/api/system/bid.ts`
  - 内部页面：`apps/web-ele/src/views/system/bid`
  - 门户页面：`apps/web-ele/src/views/bid-portal`
  - HTTP：`/bid/**`
- P0 主线已落地：
  - 项目、标段、报名、流程历史
  - 项目 / 标段 / 报名 canonical API 和 legacy 兼容接口
- P1 代码面已落地：
  - 后端模块：`tender`、`submission`、`attachment`、`portal`
  - 前端页面：招标文件、投标管理、供应商门户项目列表 / 详情 / 投标表单
  - SQL：`v3.35.0.sql`
  - 数据表：`t_bid_tender_version`、`t_bid_submission`、`t_bid_submission_version`、`t_bid_attachment`、`t_bid_portal_account`
- P1 验证已覆盖：
  - 后端 canonical / portal / attachment / enum contract tests
  - 前端动态菜单、门户公共路由、bid helper 单测
  - `@vben/web-ele` 类型检查
  - 浏览器页面可达性验证，相关页面非 404 且无 console/page error

本次确认通过的命令：

```powershell
cd E:\my-project\zhaotoubiao\zhaotoubiao
mvn -q -pl sa-admin -am "-Dtest=BidCanonicalEndpointTest,BidAttachmentContractTest,BidPortalContractTest,BidDomainEnumTest" "-Dsurefire.failIfNoSpecifiedTests=false" test

cd E:\my-project\zhaotoubiao\vue-vben-admin-main
pnpm exec vitest run apps/web-ele/src/api/core/__tests__/smart-admin-menu.test.ts apps/web-ele/src/router/routes/__tests__/public-bid-portal-routes.test.ts apps/web-ele/src/views/system/bid/shared/__tests__/bid-helper.test.ts --dom
pnpm -F @vben/web-ele run typecheck
```

### 未收口证据

- 真实业务数据闭环未证明：浏览器报告显示页面可打开，但多数列表为空、详情为“不存在或已删除”。
- 附件上传到业务对象的关联入口不足：当前能查附件摘要、下载、预览，但门户表单仍主要通过 `fileManifestJson` 文本域表达文件清单。
- 门户隔离缺少数据级回归：已有 contract test 约束 VO 不暴露 `allowedActions`，但还缺两家供应商互不可见投标数据的真实用例。
- 状态枚举还停在 P1：
  - `BidProjectStatusEnum` 尚无 `OPENING_IN_PROGRESS`、`EVALUATING`、`AWARDED`
  - `BidLotStatusEnum` 尚无 `OPENED`、`EVALUATING`、`AWARDED`
  - `BidSubmissionStatusEnum` 已有 `OPENED`，但尚未承接评标 / 定标结果
- 旧文档仍存在早期动作口径，如 `actions/start`、`actions/complete`、`actions/publish`，与 P0/P1 已冻结的 kebab action 命名不一致。
- 统一错误码、消息 / 待办、完整端到端回归仍未覆盖。

## 下一步原则

1. 先做 P1 验收收口，再启动 P2 主体。
2. P2 第一轮只做“开标 -> 最小评标汇总 -> 定标确认”，不做 CA、在线解密、复杂专家库、专家隔离环境、监管对接。
3. 新 P2 模块继续只做 canonical API，不新增 legacy `queryPage/add/update` 风格接口。
4. 所有动作使用资源专属 kebab action code，例如 `start-opening`，不使用早期泛化的 `start`。
5. 状态迁移继续以 Java 枚举 + workflow history 为主，不新增流程状态字典。
6. 前端按钮只读 `allowedActions` 与权限码，不自行推断流程状态。

## P1 收口任务

### Task C1：跑通待开标前真实数据闭环

**描述：** 用测试库或本地库构造一条完整业务链路：项目 -> 标段 -> 报名 -> 资格通过 -> 招标文件发布 -> 门户可见 -> 供应商创建投标 -> 提交 / 重提 / 撤回。

**验收标准：**

- 项目发布后至少一个标段进入 `BIDDING`。
- `ACTIVE` 招标文件能被门户项目详情读取。
- `QUALIFIED` 报名能创建投标主记录。
- 投标重复提交生成新 `submission_version`，不覆盖历史版本。
- 撤回后历史版本仍可追溯。

**验证：**

```powershell
cd E:\my-project\zhaotoubiao\zhaotoubiao
mvn -q -pl sa-admin -am -DskipTests compile
```

并补充一组 service 或 controller 层业务回归，覆盖状态流转和版本递增。

**依赖：** 当前 P1 实现

### Task C2：补齐业务附件关联入口

**描述：** 将现有文件模块上传结果与 `t_bid_attachment` 形成业务关联，避免投标文件长期停留在 `fileManifestJson` 文本快照。

**验收标准：**

- tender / submission 能通过业务接口关联已有 `file_id`。
- tender / submission 详情返回附件摘要。
- 下载 / 预览必须经过招投标业务对象归属校验。
- 删除业务附件关联不物理删除文件本体。

**验证：**

- 单测覆盖 `TENDER_VERSION` 和 `SUBMISSION_VERSION` 两类 business type。
- 浏览器检查招标文件详情、投标详情出现真实附件列表。

**依赖：** Task C1

### Task C3：补门户数据隔离回归

**描述：** 用两个供应商账号 / 信用代码构造报名和投标，验证门户只返回本人数据。

**验收标准：**

- 供应商 A 不可查询供应商 B 的报名和投标详情。
- 门户接口不返回内部审批动作、内部联系人、其他供应商报价。
- 未登录门户账号不能提交报名或投标。

**验证：**

```powershell
cd E:\my-project\zhaotoubiao\zhaotoubiao
mvn -q -pl sa-admin -am "-Dtest=BidPortalContractTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

并新增数据隔离用例。

**依赖：** Task C1

### Checkpoint：P1 Ready For P2

- 待开标前真实闭环通过。
- 业务附件能关联、展示、下载、预览。
- 门户数据隔离有自动化回归。
- P1 草案中的开放问题已回写：
  - 门户账号模型：P1 已采用 `t_bid_portal_account` 最小独立模型。
  - 招标文件审批态：P1 暂不引入 `REVIEWING`，采用 `DRAFT -> ACTIVE`。
  - 投标报价：P1 已结构化进入 `t_bid_submission_version.price_amount`。
  - 附件权限：招投标业务接口层二次校验。

## P2 MVP-2 范围

### 本轮包含

- `bid-opening`
  - 开标主记录
  - 开标明细
  - 从投标最新有效版本冻结到开标明细
  - 正常完成 / 异常关闭
- `bid-evaluation`
  - 最小评标主记录
  - 最小评分 / 排名 / 推荐结果
  - 汇总结论
- `bid-award`
  - 定标确认
  - 中标供应商快照
  - 结果可查询
- 供应商门户结果最小查询
  - 仅在定标确认后返回本供应商是否中标
  - 不返回内部评分细节

### 本轮不包含

- CA 签章和在线解密。
- 完整专家库、专家抽取、回避规则、专家隔离评标环境。
- 复杂评分模板设计器。
- 监管平台对接。
- 归档包导出。
- 合同履约和付款结算。

## P2 数据库设计切片

建议新增 SQL：`数据库SQL脚本/mysql/sql-update-log/v3.36.0.sql`

### `t_bid_opening`

核心字段：

- `opening_id`
- `project_id`
- `lot_id`
- `status`
- `opening_time`
- `opening_place`
- `host_employee_id`
- `recorder_employee_id`
- `summary`
- `abnormal_flag`
- `abnormal_reason`
- `deleted_flag`
- `version`
- `create_user_id`
- `create_time`
- `update_user_id`
- `update_time`

核心索引：

- `uk_lot_id`
- `idx_project_status`
- `idx_opening_time`

### `t_bid_opening_item`

核心字段：

- `opening_item_id`
- `opening_id`
- `submission_id`
- `submission_version_id`
- `supplier_enterprise_id`
- `supplier_name_snapshot`
- `supplier_credit_code`
- `quoted_price`
- `document_check_result`
- `open_comment`
- `sort_no`
- `deleted_flag`
- `create_time`

核心索引：

- `uk_opening_submission`
- `idx_submission_id`
- `idx_opening_sort`

### `t_bid_evaluation`

核心字段：

- `evaluation_id`
- `project_id`
- `lot_id`
- `opening_id`
- `status`
- `evaluation_mode`
- `start_time`
- `finalize_time`
- `final_summary`
- `rollback_reason`
- `deleted_flag`
- `version`
- `create_user_id`
- `create_time`
- `update_user_id`
- `update_time`

核心索引：

- `uk_lot_id`
- `idx_opening_id`
- `idx_project_status`

### `t_bid_evaluation_item`

P2 第一轮先用汇总项承载最小评标结果，不先引入完整专家评分表。

核心字段：

- `evaluation_item_id`
- `evaluation_id`
- `opening_item_id`
- `submission_id`
- `supplier_name_snapshot`
- `quoted_price`
- `total_score`
- `ranking_no`
- `recommend_flag`
- `evaluation_comment`
- `deleted_flag`
- `create_time`
- `update_time`

核心索引：

- `uk_evaluation_submission`
- `idx_evaluation_ranking`
- `idx_recommend_flag`

### `t_bid_award`

核心字段：

- `award_id`
- `project_id`
- `lot_id`
- `evaluation_id`
- `status`
- `winning_submission_id`
- `winner_enterprise_id`
- `winner_name_snapshot`
- `winner_credit_code`
- `award_amount`
- `confirm_user_id`
- `confirm_time`
- `public_notice_time`
- `rollback_reason`
- `deleted_flag`
- `version`
- `create_user_id`
- `create_time`
- `update_user_id`
- `update_time`

核心索引：

- `uk_lot_id`
- `idx_evaluation_id`
- `idx_winning_submission_id`
- `idx_confirm_time`

## P2 状态增量

### 项目状态

在 P2 进入实现前，`BidProjectStatusEnum` 增加：

- `OPENING_IN_PROGRESS`
- `EVALUATING`
- `AWARDED`

### 标段状态

`BidLotStatusEnum` 增加：

- `OPENED`
- `EVALUATING`
- `AWARDED`

### 投标状态

`BidSubmissionStatusEnum` 当前已有 `OPENED`。P2 第一轮可以先只在开标完成时将 `SUBMITTED -> OPENED`，评标 / 定标结果通过 `evaluation_item` 和 `award` 表表达。

如后续门户需要直接按投标状态展示结果，再增加：

- `EVALUATED`
- `AWARDED`
- `LOST`

## P2 API 设计

### 开标 `openings`

```text
POST /bid/openings/search
GET  /bid/openings/{openingId}
POST /bid/openings
GET  /bid/lots/{lotId}/opening
POST /bid/openings/{openingId}/actions/start-opening
POST /bid/openings/{openingId}/actions/complete-opening
POST /bid/openings/{openingId}/actions/abnormal-close-opening
```

权限码：

- `bid:opening:query`
- `bid:opening:create`
- `bid:opening:start-opening`
- `bid:opening:complete-opening`
- `bid:opening:abnormal-close-opening`

### 评标 `evaluations`

```text
POST /bid/evaluations/search
GET  /bid/evaluations/{evaluationId}
POST /bid/evaluations
GET  /bid/lots/{lotId}/evaluation
POST /bid/evaluations/{evaluationId}/actions/start-evaluation
POST /bid/evaluations/{evaluationId}/actions/finalize-evaluation
POST /bid/evaluations/{evaluationId}/actions/rollback-evaluation
```

权限码：

- `bid:evaluation:query`
- `bid:evaluation:create`
- `bid:evaluation:start-evaluation`
- `bid:evaluation:finalize-evaluation`
- `bid:evaluation:rollback-evaluation`

### 定标 `awards`

```text
POST /bid/awards/search
GET  /bid/awards/{awardId}
POST /bid/awards
GET  /bid/lots/{lotId}/award
POST /bid/awards/{awardId}/actions/confirm-award
POST /bid/awards/{awardId}/actions/rollback-award
POST /bid/awards/{awardId}/actions/cancel-award
```

权限码：

- `bid:award:query`
- `bid:award:create`
- `bid:award:confirm-award`
- `bid:award:rollback-award`
- `bid:award:cancel-award`

### 门户结果

```text
GET /bid/portal/lots/{lotId}/result
```

约束：

- 仅定标确认后返回。
- 只返回当前供应商自己的结果摘要。
- 不返回其他供应商评分明细。

## P2 动作矩阵

| resource | from_status | to_status | action_code | allowedActions | api_perm | web_perm | canonical_endpoint |
| --- | --- | --- | --- | --- | --- | --- | --- |
| opening | `PENDING` | `IN_PROGRESS` | `start-opening` | `start-opening` | `bid:opening:start-opening` | `bid:opening:start-opening` | `POST /bid/openings/{openingId}/actions/start-opening` |
| opening | `IN_PROGRESS` | `COMPLETED` | `complete-opening` | `complete-opening` | `bid:opening:complete-opening` | `bid:opening:complete-opening` | `POST /bid/openings/{openingId}/actions/complete-opening` |
| opening | `PENDING/IN_PROGRESS` | `ABNORMAL_CLOSED` | `abnormal-close-opening` | `abnormal-close-opening` | `bid:opening:abnormal-close-opening` | `bid:opening:abnormal-close-opening` | `POST /bid/openings/{openingId}/actions/abnormal-close-opening` |
| evaluation | `PENDING` | `SCORING` | `start-evaluation` | `start-evaluation` | `bid:evaluation:start-evaluation` | `bid:evaluation:start-evaluation` | `POST /bid/evaluations/{evaluationId}/actions/start-evaluation` |
| evaluation | `SCORING/SUMMARIZING` | `FINALIZED` | `finalize-evaluation` | `finalize-evaluation` | `bid:evaluation:finalize-evaluation` | `bid:evaluation:finalize-evaluation` | `POST /bid/evaluations/{evaluationId}/actions/finalize-evaluation` |
| evaluation | `SCORING/SUMMARIZING/FINALIZED` | `ROLLED_BACK` | `rollback-evaluation` | `rollback-evaluation` | `bid:evaluation:rollback-evaluation` | `bid:evaluation:rollback-evaluation` | `POST /bid/evaluations/{evaluationId}/actions/rollback-evaluation` |
| award | `PENDING/REVIEWING` | `CONFIRMED` | `confirm-award` | `confirm-award` | `bid:award:confirm-award` | `bid:award:confirm-award` | `POST /bid/awards/{awardId}/actions/confirm-award` |
| award | `CONFIRMED` | `ROLLED_BACK` | `rollback-award` | `rollback-award` | `bid:award:rollback-award` | `bid:award:rollback-award` | `POST /bid/awards/{awardId}/actions/rollback-award` |
| award | `PENDING/REVIEWING` | `CANCELLED` | `cancel-award` | `cancel-award` | `bid:award:cancel-award` | `bid:award:cancel-award` | `POST /bid/awards/{awardId}/actions/cancel-award` |

## P2 实施任务

### Task 1：冻结 P2 状态、SQL 和菜单权限

**验收标准：**

- `v3.36.0.sql` 新增 opening、opening_item、evaluation、evaluation_item、award 表。
- 菜单 ID、权限码、动作矩阵完成规划。
- Java enum 与动作矩阵一致。

**验证：**

- SQL 可重复执行。
- `BidDomainEnumTest` 覆盖新增状态。

**依赖：** P1 Ready For P2

### Task 2：实现 opening 后端切片

**验收标准：**

- 只有 `BID_CLOSED` 标段可创建 / 启动开标。
- 完成开标时，从每条有效 `SUBMITTED` 投标冻结最新 `submission_version` 到 `opening_item`。
- 完成开标后标段进入 `OPENED`，投标进入 `OPENED`。
- 异常关闭必须填写原因。

**验证：**

```powershell
cd E:\my-project\zhaotoubiao\zhaotoubiao
mvn -q -pl sa-admin -am "-Dtest=BidOpening*Test,BidCanonicalEndpointTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

**依赖：** Task 1

### Task 3：实现 opening 前端页面

**验收标准：**

- `api/system/bid.ts` 增加 `SystemBidOpeningApi`。
- 新增 `views/system/bid/opening/opening-list.vue`、`opening-detail.vue`。
- 动作按钮完全由 `allowedActions` 控制。

**验证：**

```powershell
cd E:\my-project\zhaotoubiao\vue-vben-admin-main
pnpm -F @vben/web-ele run typecheck
pnpm exec vitest run apps/web-ele/src/api/core/__tests__/smart-admin-menu.test.ts --dom
```

**依赖：** Task 2

### Task 4：实现 evaluation 最小后端切片

**验收标准：**

- 只有 `OPENED` 标段可创建 / 启动评标。
- 可维护每个开标明细的得分、排名、推荐标记。
- 汇总完成后 evaluation 进入 `FINALIZED`，标段进入 `EVALUATING` 或保持可进入定标状态。
- 回退必须记录原因和 workflow history。

**验证：**

```powershell
cd E:\my-project\zhaotoubiao\zhaotoubiao
mvn -q -pl sa-admin -am "-Dtest=BidEvaluation*Test,BidCanonicalEndpointTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

**依赖：** Task 2

### Task 5：实现 award 最小后端切片

**验收标准：**

- 只有 `FINALIZED` 评标可创建定标。
- 定标确认后写入中标供应商快照和中标金额。
- 定标确认后标段进入 `AWARDED`，项目在所有标段定标后进入 `AWARDED`。
- 回退定标必须保留原因，不删除评标记录。

**验证：**

```powershell
cd E:\my-project\zhaotoubiao\zhaotoubiao
mvn -q -pl sa-admin -am "-Dtest=BidAward*Test,BidCanonicalEndpointTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

**依赖：** Task 4

### Task 6：实现 evaluation / award 内部前端页面

**验收标准：**

- `api/system/bid.ts` 增加 `SystemBidEvaluationApi`、`SystemBidAwardApi`。
- 新增评标、定标列表 / 详情页面。
- 页面不展示门户专用字段，不自行推断动作。

**验证：**

```powershell
cd E:\my-project\zhaotoubiao\vue-vben-admin-main
pnpm -F @vben/web-ele run typecheck
pnpm exec vitest run apps/web-ele/src/api/core/__tests__/smart-admin-menu.test.ts --dom
```

**依赖：** Task 4、Task 5

### Task 7：实现门户结果最小查询

**验收标准：**

- 定标确认前门户结果接口返回不可查看。
- 定标确认后，供应商只看到自己的中标 / 未中标结果摘要。
- 不泄露其他供应商评分、报价、联系人。

**验证：**

- 用两个供应商身份构造数据，验证互不可见。
- `BidPortalContractTest` 覆盖结果 VO 不包含内部字段。

**依赖：** Task 5

### Checkpoint：P2 MVP-2 最小闭环

- P1 能走到待开标。
- 开标能冻结投标最新有效版本。
- 评标能形成最小汇总和推荐结果。
- 定标能确认中标供应商。
- 门户能查看本人结果摘要。
- 后续归档模块具备稳定输入。

## Agent 分工建议

- Agent A：P1 收口验证与附件关联，负责 Task C1-C3。
- Agent B：P2 SQL、菜单、权限和状态矩阵，负责 Task 1。
- Agent C：opening 后端和测试，负责 Task 2。
- Agent D：evaluation / award 后端和测试，负责 Task 4-5。
- Agent E：内部前端页面和 API 类型，负责 Task 3、Task 6。
- Agent F：门户结果、质量回归、安全检查，负责 Task 7 和最终验收。

并行约束：

- Task C1-C3 必须先完成或明确豁免，否则 P2 缺少可信输入。
- Task 1 完成后，Task 2 和 Task 3 可串行；Task 4、Task 5 必须依赖前一业务状态。
- 前端 Agent 不直接发明状态规则，只消费后端 VO 的 `allowedActions`。

## 风险与缓解

| 风险 | 影响 | 缓解 |
| --- | --- | --- |
| P1 只证明页面可达，未证明真实业务链路 | P2 开标没有可信投标数据 | 先完成 P1 Ready For P2 checkpoint |
| 附件仍停留在文本清单 | 开标和归档无法追溯文件版本 | P1 收口补业务附件关联接口 |
| 项目 / 标段状态枚举未扩展 | P2 页面和动作无法准确渲染 | Task 1 先冻结状态增量和动作矩阵 |
| 多标段项目状态聚合复杂 | 项目状态可能早于标段完成而误变更 | P2 第一轮只在所有非作废标段完成时推进项目状态 |
| 旧文档 API 命名继续被复用 | 新模块出现 `start` / `complete` 等泛化动作 | P2 统一使用资源专属 kebab action |
| 门户结果泄露内部评标信息 | 数据隔离和合规风险 | 门户结果只返回当前供应商摘要 |

## 待确认问题

1. P2 是否接受“最小评标汇总项”，暂不做专家逐项评分表？
2. 定标是否需要 `REVIEWING` 审批态，还是 P2 先从 `PENDING -> CONFIRMED`？
3. 门户结果是否只展示中标 / 未中标，还是需要展示中标金额和公告时间？
4. 多标段项目中，是否允许部分标段先定标、项目保持 `EVALUATING`？
5. `BidErrorCode` 是否在 P2 Task 1 同步建立错误码区间？

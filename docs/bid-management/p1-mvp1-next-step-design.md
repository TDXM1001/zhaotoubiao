# 招投标 P1 MVP-1 下一步设计

## 状态

- 文档状态：`Draft v0.1`
- 日期：`2026-06-08`
- 输入基线：`p0-implementation-baseline.md`、`ADR-004-bid-p0-implementation-baseline.md`
- 目标读者：后端、数据库、前端、质量验证 agent

## 目标

在 P0 已实现的项目、标段、报名主线基础上，推进 P1 MVP-1 的最小闭环：让业务可以从“项目发布 / 标段投标中 / 报名资格通过”继续走到“招标文件已发布、供应商可提交投标文件、投标截止后进入待开标”。

本阶段重点不是一次性实现开标、评标、定标，而是先冻结并落地后续阶段依赖的三类基础能力：

1. 招标文件版本与公告 / 澄清。
2. 投标主记录、投标版本和投标回执。
3. 招投标业务附件关联与版本关系。

## 当前基线

已落地：

- 后端模块：`project`、`lot`、`registration`、`common/workflow_history`
- 前端页面：项目管理、标段管理、报名管理
- 数据表：`t_bid_project`、`t_bid_lot`、`t_bid_project_member`、`t_bid_workflow_history`、`t_bid_registration`
- 资源化接口：项目、标段、报名已有 canonical 端点，同时保留 legacy 端点
- 前端 API：当前页面仍主要调用 legacy 端点

已知口径差异：

- 项目状态和 SQL 权限中已经出现 `ARCHIVED` / `bid:project:archive` 口径，但当前 P0 Java 枚举和项目 service 主流程尚未完整实现归档动作。本轮不实现归档业务，但必须在 P1 开发前明确：归档仍属于 P2/P3 后置能力，P1 不得临时复用 `archive` 权限承载其他动作。

必须继续遵守：

- 后端路径：`sa-admin/src/main/java/net/lab1024/sa/admin/module/system/bid`
- Mapper 路径：`sa-admin/src/main/resources/mapper/system/bid`
- 前端 API：`apps/web-ele/src/api/system/bid.ts`
- 内部页面：`apps/web-ele/src/views/system/bid`
- HTTP 命名空间：`/bid/**`
- 新模块只实现 canonical API，不再新增 `add/update/queryPage` 旧式端点

## 设计假设

1. P1 MVP-1 先做“待开标前闭环”，开标、评标、定标只做接口预留和状态交接，不进入本轮实现主体。
2. 供应商门户先做轻量入口和接口边界，不建设完整外部账号体系；门户账号模型需要后续单独冻结。
3. 附件文件本体复用现有文件模块，本轮只新增招投标业务对象和文件的关联、版本、分类关系。
4. 报名资格通过后才能创建或提交投标；未通过报名不允许进入投标提交。
5. `ResponseDTO<T>`、`PageParam`、`PageResult<T>`、`allowedActions`、`version` 继续作为前后端合同基础。

## 范围

### 本轮包含

- `bid-tender`
  - 招标文件版本
  - 公告发布
  - 澄清 / 更正版本
  - 当前有效版本查询
- `bid-submission`
  - 投标主记录
  - 投标文件版本
  - 截止前重复提交形成新版本
  - 投标撤回
  - 投标回执号
- `bid-attachment`
  - 业务对象与文件模块关联
  - 招标文件、投标文件、澄清附件的分类和版本号
- `bid-portal`
  - 供应商项目列表 / 详情
  - 门户报名入口复用现有报名 service
  - 门户投标提交入口复用 submission service
  - 门户数据隔离规则
- 质量回归
  - 菜单路径和组件路径单测
  - canonical API 契约回归
  - 状态动作矩阵和权限矩阵补齐

### 本轮不包含

- CA 签章、在线解密、保证金、监管对接
- 完整专家库、专家抽取、评标隔离环境
- 开标唱标、专家评分、定标审批的完整实现
- 合同履约、付款结算、归档包导出
- 迁移 P0 已落地 legacy 前端调用到 canonical 调用

## 领域切分

### 招标文件

主实体：`t_bid_tender_version`

关键规则：

- 归属到 `project_id + lot_id`。
- 同一标段同一时刻只允许一个当前有效主版本。
- `ACTIVE` 版本不可直接覆盖，只能发布新版本并让旧版本进入 `SUPERSEDED`。
- 澄清 / 更正必须保留 `parent_version_id`，形成版本链。
- 发布招标文件后，标段进入或保持 `BIDDING`，供应商门户可见。

建议状态：

| 状态 | 含义 |
| --- | --- |
| `DRAFT` | 草稿，可编辑 |
| `ACTIVE` | 当前有效版本 |
| `SUPERSEDED` | 已被新版本替代 |
| `WITHDRAWN` | 已撤回 |

### 投标提交

主实体：`t_bid_submission`

版本实体：`t_bid_submission_version`

关键规则：

- 一条合格报名最多对应一个投标主记录。
- 同一标段同一供应商最多一个有效投标主记录。
- `QUALIFIED` 报名是创建投标记录和提交投标的前置条件。
- 投标截止前允许多次提交，每次提交生成新的 `submission_version`。
- 投标截止后禁止提交和撤回，后续由开标动作冻结最新有效版本。

建议状态：

| 状态 | 含义 |
| --- | --- |
| `QUALIFIED` | 已具备投标资格，但未提交有效投标文件 |
| `SUBMITTED` | 已提交有效投标版本 |
| `WITHDRAWN` | 已撤回，不参与后续流程 |
| `OPENED` | 已开标，投标版本冻结 |

### 附件关联

主实体：`t_bid_attachment`

关键规则：

- 文件本体仍由现有文件模块负责。
- 招投标业务只保存 `business_type + business_id + file_id + file_category + version_no`。
- 招标文件附件挂到 `TENDER_VERSION`。
- 投标文件附件挂到 `SUBMISSION_VERSION`。
- 详情接口返回附件摘要，前端不直接拼装文件模块内部结构。

## 数据库设计切片

建议新增 SQL：`数据库SQL脚本/mysql/sql-update-log/v3.35.0.sql`

### `t_bid_tender_version`

核心字段：

- `tender_version_id`
- `project_id`
- `lot_id`
- `version_no`
- `version_type`
- `status`
- `parent_version_id`
- `effective_time`
- `publish_user_id`
- `publish_time`
- `summary`
- `deleted_flag`
- `version`
- `create_user_id`
- `create_time`
- `update_user_id`
- `update_time`

核心索引：

- `idx_project_lot_status`
- `idx_lot_version_no`
- `idx_parent_version_id`

### `t_bid_submission`

核心字段：

- `submission_id`
- `project_id`
- `lot_id`
- `registration_id`
- `supplier_enterprise_id`
- `supplier_name_snapshot`
- `supplier_credit_code`
- `status`
- `latest_version_no`
- `latest_submit_time`
- `receipt_no`
- `withdraw_time`
- `withdraw_reason`
- `deleted_flag`
- `version`
- `create_user_id`
- `create_time`
- `update_user_id`
- `update_time`

核心索引：

- `uk_lot_supplier_credit`
- `uk_registration_id`
- `idx_project_lot_status`
- `idx_latest_submit_time`

### `t_bid_submission_version`

核心字段：

- `submission_version_id`
- `submission_id`
- `version_no`
- `submit_time`
- `submit_user_id`
- `price_amount`
- `contact_name`
- `contact_phone`
- `file_manifest_json`
- `is_effective`
- `deleted_flag`
- `create_time`

核心索引：

- `uk_submission_version_no`
- `idx_submission_effective`

### `t_bid_attachment`

核心字段：

- `attachment_id`
- `business_type`
- `business_id`
- `project_id`
- `lot_id`
- `file_id`
- `file_category`
- `version_no`
- `sort_no`
- `is_main`
- `deleted_flag`
- `create_user_id`
- `create_time`

核心索引：

- `idx_business_type_id`
- `idx_project_lot`
- `idx_file_id`

## API 设计

新模块只按 canonical 契约实现。

### 招标文件 `tenders`

```text
POST /bid/tenders/search
GET  /bid/tenders/{tenderVersionId}
POST /bid/tenders
PUT  /bid/tenders/{tenderVersionId}
GET  /bid/lots/{lotId}/tenders/active
POST /bid/tenders/{tenderVersionId}/actions/publish-tender
POST /bid/tenders/{tenderVersionId}/actions/clarify-tender
POST /bid/tenders/{tenderVersionId}/actions/withdraw-tender
```

权限码：

- `bid:tender:query`
- `bid:tender:create`
- `bid:tender:update`
- `bid:tender:publish-tender`
- `bid:tender:clarify-tender`
- `bid:tender:withdraw-tender`

### 投标提交 `submissions`

```text
POST /bid/submissions/search
GET  /bid/submissions/{submissionId}
POST /bid/submissions
GET  /bid/registrations/{registrationId}/submission
POST /bid/submissions/{submissionId}/actions/submit-bid
POST /bid/submissions/{submissionId}/actions/withdraw-bid
```

权限码：

- `bid:submission:query`
- `bid:submission:create`
- `bid:submission:submit-bid`
- `bid:submission:withdraw-bid`
- `bid:submission:download`

### 门户 `portal`

门户接口必须落在 `/bid/portal/**`，不得复用内部主系统接口直接暴露字段。

```text
POST /bid/portal/projects/search
GET  /bid/portal/projects/{projectId}
POST /bid/portal/registrations
GET  /bid/portal/registrations/{registrationId}
POST /bid/portal/submissions
GET  /bid/portal/submissions/{submissionId}
POST /bid/portal/submissions/{submissionId}/actions/submit-bid
POST /bid/portal/submissions/{submissionId}/actions/withdraw-bid
```

门户响应约束：

- 不返回其他供应商报名、投标、联系人、报价信息。
- 不返回内部审批动作和内部 `allowedActions`。
- 只返回当前供应商企业可见的项目、标段、招标文件、本人投标记录。

### 附件摘要

业务详情 VO 中统一返回：

```json
{
  "attachments": [
    {
      "fileId": 1001,
      "fileName": "tender.pdf",
      "fileSize": 204800,
      "fileCategory": "TENDER_MAIN",
      "versionNo": 1,
      "downloadUrl": "/support/file/download/1001",
      "previewUrl": "/support/file/preview/1001",
      "uploadTime": "2026-06-08 10:00:00",
      "uploaderName": "管理员"
    }
  ]
}
```

## 前端设计

### 内部主系统

继续使用 `views/system/bid/{resource}`。

新增页面：

```text
views/system/bid/tender/tender-list.vue
views/system/bid/tender/tender-form.vue
views/system/bid/tender/tender-detail.vue
views/system/bid/submission/submission-list.vue
views/system/bid/submission/submission-detail.vue
```

API 类型继续追加到：

```text
apps/web-ele/src/api/system/bid.ts
```

新增命名空间：

- `SystemBidTenderApi`
- `SystemBidSubmissionApi`
- `SystemBidPortalApi`

### 供应商门户

P1 先冻结路径，不和内部 `/system/bid/**` 混用。

建议路径：

```text
views/bid-portal/project/portal-project-list.vue
views/bid-portal/project/portal-project-detail.vue
views/bid-portal/submission/portal-submission-form.vue
```

如果本轮不做完整门户 UI，至少要完成：

- 门户 API 类型定义。
- 门户数据可见字段设计。
- 门户页面壳和菜单路径样例。

## 动作矩阵增量

| resource | from_status | to_status | action_code | allowedActions | api_perm | web_perm | canonical_endpoint |
| --- | --- | --- | --- | --- | --- | --- | --- |
| tender | `DRAFT` | `ACTIVE` | `publish-tender` | `publish-tender` | `bid:tender:publish-tender` | `bid:tender:publish-tender` | `POST /bid/tenders/{tenderVersionId}/actions/publish-tender` |
| tender | `ACTIVE` | `SUPERSEDED` | `clarify-tender` | `clarify-tender` | `bid:tender:clarify-tender` | `bid:tender:clarify-tender` | `POST /bid/tenders/{tenderVersionId}/actions/clarify-tender` |
| tender | `DRAFT/ACTIVE` | `WITHDRAWN` | `withdraw-tender` | `withdraw-tender` | `bid:tender:withdraw-tender` | `bid:tender:withdraw-tender` | `POST /bid/tenders/{tenderVersionId}/actions/withdraw-tender` |
| submission | `QUALIFIED` | `SUBMITTED` | `submit-bid` | `submit-bid` | `bid:submission:submit-bid` | `bid:submission:submit-bid` | `POST /bid/submissions/{submissionId}/actions/submit-bid` |
| submission | `SUBMITTED` | `SUBMITTED` | `submit-bid` | `submit-bid` | `bid:submission:submit-bid` | `bid:submission:submit-bid` | `POST /bid/submissions/{submissionId}/actions/submit-bid` |
| submission | `SUBMITTED` | `WITHDRAWN` | `withdraw-bid` | `withdraw-bid` | `bid:submission:withdraw-bid` | `bid:submission:withdraw-bid` | `POST /bid/submissions/{submissionId}/actions/withdraw-bid` |

## Agent 分工建议

### Agent A：数据库与菜单

责任：

- 新增 `v3.35.0.sql`
- 创建 tender、submission、submission_version、attachment 表
- 新增字典、菜单、功能点、管理员默认授权

验收：

- SQL 可重复执行。
- 菜单路径和组件路径符合 ADR-004。
- 新权限码和动作矩阵一致。

### Agent B：后端 tender

责任：

- 新建 `bid/tender` 后端模块。
- 实现 canonical controller、service、dao、mapper、form、vo。
- 实现发布、澄清、撤回动作和 workflow history。

验收：

- 只暴露 canonical 接口。
- `ACTIVE` 主版本唯一。
- 动作接口校验 `version`。

### Agent C：后端 submission 与附件关联

责任：

- 新建 `bid/submission` 后端模块。
- 实现投标主记录、投标版本、投标撤回。
- 新建或复用 `bid/common` 附件关联 service。

验收：

- 未通过报名不能提交投标。
- 截止后不能提交或撤回。
- 重复提交生成新版本，不覆盖历史版本。

### Agent D：内部前端页面

责任：

- 扩展 `api/system/bid.ts` 类型和方法。
- 新增 tender、submission 内部页面。
- 基于 `allowedActions` 控制按钮。

验收：

- 前端类型检查通过。
- 菜单动态路由测试覆盖新增组件路径。
- 页面不自行推断状态权限。

### Agent E：门户接口与页面壳

责任：

- 冻结 `/bid/portal/**` DTO 和字段可见性。
- 新增门户页面壳或最小可访问视图。
- 复用报名和投标 service，增加门户数据隔离。

验收：

- 门户响应不泄露其他供应商数据。
- 门户动作不暴露内部审批动作。
- 门户路径不混入 `/system/bid/**`。

### Agent F：质量与回归

责任：

- 补菜单路径、权限矩阵、动作矩阵测试。
- 补 canonical API 最小回归。
- 汇总 P1 验收命令。

验收：

- 后端 compile 通过。
- 前端 typecheck 通过。
- 动态菜单单测覆盖新增路径。

## 实施任务

### Task 1：冻结 P1 数据库增量

**验收标准：**

- `v3.35.0.sql` 包含四张新表和必要索引。
- SQL 使用 `IF NOT EXISTS` / `ON DUPLICATE KEY UPDATE` 支持重复执行。
- 菜单 ID、权限码、字典编码有明确规划。

**验证：**

- 在测试库执行初始化顺序：`smart_admin_v3.sql -> v3.34.0.sql -> v3.34.1.sql -> v3.35.0.sql`
- 检查新增表、菜单、权限点存在。

**依赖：** 无

### Task 1.5：对齐 P0 状态与权限口径

**验收标准：**

- 明确 `ARCHIVED`、`bid:project:archive` 在 P1 中只作为后续预留，不进入待开标前闭环。
- tender、submission 新增动作不得复用既有 project / lot 权限码。
- 动作矩阵、权限矩阵、菜单功能点三者命名一致。

**验证：**

- 对照 `p0-implementation-baseline.md`、`v3.34.0.sql`、Java 枚举和新增 SQL 权限点完成清单检查。

**依赖：** Task 1

### Task 2：实现 tender canonical 后端切片

**验收标准：**

- `POST /bid/tenders/search`、`GET /bid/tenders/{id}`、`POST /bid/tenders`、`PUT /bid/tenders/{id}` 可用。
- 发布 / 澄清 / 撤回动作可用，并写入 `t_bid_workflow_history`。
- 同一标段只有一个 `ACTIVE` 主版本。

**验证：**

```powershell
cd E:\my-project\zhaotoubiao\zhaotoubiao
mvn -q -pl sa-admin -am -DskipTests compile
```

**依赖：** Task 1

### Task 3：实现 submission canonical 后端切片

**验收标准：**

- 合格报名可以创建或查询投标主记录。
- 投标提交生成 `submission_version` 和回执号。
- 重复提交产生新版本，撤回保留历史版本。

**验证：**

```powershell
cd E:\my-project\zhaotoubiao\zhaotoubiao
mvn -q -pl sa-admin -am -DskipTests compile
```

**依赖：** Task 1

### Task 4：实现附件关联服务

**验收标准：**

- tender 详情返回招标文件附件摘要。
- submission 详情返回投标文件附件摘要。
- 业务附件不直接复制文件本体数据。

**验证：**

- 手动构造文件关联数据，查询 tender / submission 详情可返回附件摘要。
- 删除业务关联不物理删除文件本体。

**依赖：** Task 2、Task 3

### Task 5：新增内部 tender / submission 页面

**验收标准：**

- `api/system/bid.ts` 包含 tender、submission 类型和请求函数。
- 内部菜单可打开招标文件列表 / 表单 / 详情、投标列表 / 详情。
- 操作按钮全部由 `allowedActions` 和权限码共同控制。

**验证：**

```powershell
cd E:\my-project\zhaotoubiao\vue-vben-admin-main
pnpm -F @vben/web-ele run typecheck
pnpm exec vitest run apps/web-ele/src/api/core/__tests__/smart-admin-menu.test.ts --dom
```

**依赖：** Task 2、Task 3

### Task 6：冻结门户最小合同

**验收标准：**

- `/bid/portal/**` 接口只返回供应商可见字段。
- 门户项目详情可看到可报名 / 已报名 / 可投标状态。
- 门户投标提交复用 submission service，不另建第二套投标逻辑。

**验证：**

- 用两个不同供应商身份构造数据，互不可见投标记录。
- 门户接口不返回内部审批字段。

**依赖：** Task 3、Task 4

### Checkpoint：P1 MVP-1 待开标前闭环

- 项目可发布。
- 标段可进入投标中。
- 供应商可报名并被资格通过。
- 招标文件可发布并对门户可见。
- 合格供应商可提交 / 重提 / 撤回投标。
- 投标截止后系统具备进入开标模块的稳定数据基础。

## 风险与缓解

| 风险 | 影响 | 缓解 |
| --- | --- | --- |
| 门户账号模型未冻结 | 数据隔离和权限实现容易返工 | P1 先冻结 `/bid/portal/**` 字段和 service 复用边界，账号模型单独设计 |
| 附件版本和文件模块耦合过深 | 投标版本难以追溯 | 业务附件只保存文件引用和版本关系，文件本体继续由现有模块负责 |
| 新模块继续扩展 legacy API | API 风格再次分叉 | 本文明确新模块只做 canonical API |
| 前端自行推断流程状态 | 状态动作变更后页面失真 | 所有按钮必须基于 `allowedActions` 和权限码 |
| 开标前冻结规则不清 | 后续开标模块无法可信使用投标版本 | submission 必须保留历史版本，并在截止后禁止提交 / 撤回 |

## 待确认问题

1. 供应商门户账号是否在 P1 独立建模，还是先用内部账号 / 模拟企业身份验证流程？
2. 招标文件是否需要 `REVIEWING` 审批态，还是 P1 先采用 `DRAFT -> ACTIVE` 的轻流程？
3. 投标报价字段是否必须结构化进入 `t_bid_submission_version.price_amount`，还是 P1 先只随附件清单存档？
4. 附件下载 / 预览权限是否由文件模块增强，还是由招投标模块在业务接口层二次校验？
5. P1 是否需要补 P0 legacy 前端调用迁移到 canonical，还是继续作为后续质量任务？

# 招投标管理系统 API 契约设计

## 设计目标

- 用一套稳定的 HTTP/JSON 契约支撑内部主系统和供应商轻量门户联调
- 延续现有仓库的 `ResponseDTO`、`PageParam`、`PageResult` 基础设施
- 把普通数据编辑和流程动作显式分开
- 让后端包结构、权限码、前端菜单和页面动作保持一一对应

## 基线约束

- 后端继续返回 `ResponseDTO<T>`
- 分页查询继续使用 `PageParam` + `PageResult<T>`
- 前端请求层继续按 `code=0`、`data` 解包
- 一期不强依赖 `PATCH`
- 一期不引入第二个版本前缀；已落地旧式接口仅作为兼容适配层，新模块以资源化契约为目标接口

## 统一响应契约

## `ResponseDTO<T>`

所有业务接口统一返回：

```json
{
  "code": 0,
  "level": null,
  "msg": "操作成功",
  "ok": true,
  "data": {}
}
```

字段语义：

- `code`
  - 业务码，`0` 表示成功
- `level`
  - 错误级别或提示级别
- `msg`
  - 面向用户的消息
- `ok`
  - 业务是否成功
- `data`
  - 业务数据载荷

## 分页响应

分页数据放在 `ResponseDTO<PageResult<T>>` 中：

```json
{
  "code": 0,
  "ok": true,
  "data": {
    "pageNum": 1,
    "pageSize": 20,
    "total": 120,
    "pages": 6,
    "list": [],
    "emptyFlag": false
  }
}
```

## 错误语义

### 总原则

- 业务错误优先通过 `ResponseDTO.error(...)` 返回
- 网关、鉴权、框架级异常保留 HTTP 错误语义
- 不再单独设计第二套 `{ error: ... }` 错误结构

### 推荐分类

- 参数校验失败
  - `code` 使用现有参数错误码
- 未登录 / Token 失效
  - 保持现有鉴权链路
- 无权限
  - 保持现有鉴权链路
- 状态冲突
  - 返回明确业务码和可读 `msg`
- 版本冲突
  - 返回明确业务码，前端提示刷新

### 招投标域新增错误分类建议

- `BID_STATUS_INVALID`
  - 当前状态不允许执行动作
- `BID_VERSION_CONFLICT`
  - 版本冲突
- `BID_TIME_WINDOW_CLOSED`
  - 已过报名或投标时间窗
- `BID_PERMISSION_SCOPE_DENIED`
  - 数据范围不允许访问
- `BID_ATTACHMENT_REQUIRED`
  - 关键附件缺失
- `BID_PORTAL_ACCESS_DENIED`
  - 门户用户访问越权

### 错误码区间建议

- 为 `BidErrorCode` 单独预留业务码区间
- 建议使用 `41001+`
- 由统一错误码注册表集中维护，不在各业务模块零散硬编码

## 资源模型

一期统一采用 `/bid/**` 命名空间，核心资源如下：

- `projects`
  - 招标项目
- `lots`
  - 标段 / 包件
- `tenders`
  - 招标文件与公告版本
- `registrations`
  - 报名 / 资格初审
- `submissions`
  - 投标主记录与版本
- `openings`
  - 开标
- `evaluations`
  - 评标
- `awards`
  - 定标与结果
- `portal`
  - 门户专用查询和动作

## 路径与动作规范

## 查询接口

- 复杂查询统一使用 `POST /.../search`
- 简单详情使用 `GET /.../{id}`
- 字典、下拉、轻量列表使用 `GET /.../options`

示例：

```text
POST /bid/projects/search
GET  /bid/projects/{projectId}
GET  /bid/projects/options
```

## 新增与更新

- 新增：`POST /resource`
- 更新：`PUT /resource/{id}`
- 不通过普通更新接口隐式推进流程状态

示例：

```text
POST /bid/projects
PUT  /bid/projects/{projectId}
POST /bid/lots
PUT  /bid/lots/{lotId}
```

## 动作接口

动作接口统一使用：

```text
POST /resource/{id}/actions/{actionName}
```

示例：

```text
POST /bid/projects/{projectId}/actions/submit-plan
POST /bid/projects/{projectId}/actions/publish-project
POST /bid/lots/{lotId}/actions/close-bid
POST /bid/submissions/{submissionId}/actions/approve-qualification
POST /bid/submissions/{submissionId}/actions/submit
POST /bid/openings/{openingId}/actions/complete
POST /bid/evaluations/{evaluationId}/actions/finalize
POST /bid/awards/{awardId}/actions/confirm
```

## 导出接口

- 导出保持现有风格，使用 `POST` 提交查询条件
- 返回文件流

示例：

```text
POST /bid/projects/export
POST /bid/awards/export-result
POST /bid/archive/export-package
```

## DTO 规范

## 请求对象

- `*SearchForm`
  - 继承或组合 `PageParam`
- `*CreateForm`
  - 新增
- `*UpdateForm`
  - 编辑
- `*ActionForm`
  - 动作命令
- `*BatchActionForm`
  - 批量动作

## 响应对象

- `*ListVO`
  - 列表行
- `*DetailVO`
  - 详情页主对象
- `*OptionVO`
  - 下拉与轻量选择
- `*SummaryVO`
  - 统计或摘要

## 字段命名

- JSON 字段使用 `camelCase`
- 状态枚举使用大写字符串
  - 例如：`DRAFT`、`PUBLISHED`、`BID_CLOSED`
- 布尔字段使用 `is/has/can`
  - 例如：`isPortalVisible`、`hasClarification`

## 详情页固定字段

所有主资源 `DetailVO` 建议固定保留：

- `id`
- `status`
- `version`
- `allowedActions`
- `attachments`
- `auditSummary`

按资源可选增加：

- `timelineSummary`
- `permissionSummary`
- `portalVisibleFlag`

前端必须基于 `allowedActions` 控制按钮可用性，不自行猜状态。

## `allowedActions` 约定

- `allowedActions` 建议为字符串数组
- 数组元素直接使用动作编码，和动作 URL 尾段保持同名
- 动作编码统一使用 `kebab-case`

示例：

```json
{
  "id": 1001,
  "status": "PUBLISHED",
  "version": 7,
  "allowedActions": [
    "submit-plan",
    "publish-project",
    "archive-project"
  ]
}
```

这样可避免：

- URL 用 `kebab-case`
- 前端按钮再单独维护一套 `camelCase`

如需展示文案，由前端本地映射动作编码到按钮文案和图标。

## 分页与搜索规范

## 分页请求

分页查询继续使用：

- `pageNum`
- `pageSize`
- `searchCount`
- `sortItemList`

排序字段必须限制在白名单内，不直接信任前端列名。

## 搜索表单

搜索条件建议分层：

- 通用条件
  - `keyword`
  - `status`
  - `dateRange`
- 资源特有条件
  - `ownerOrgId`
  - `agentOrgId`
  - `procurementMode`
  - `supplierEnterpriseId`

## 版本控制与并发规范

## 乐观锁

所有会修改主对象状态或内容的请求，建议携带：

- `id`
- `version`

示例：

```json
{
  "id": 1001,
  "version": 7,
  "comment": "确认定标"
}
```

服务端校验失败时返回：

- `code = BID_VERSION_CONFLICT`
- `msg = 数据已更新，请刷新后重试`

## 幂等与重复提交

## 默认策略

- 查询接口天然幂等
- 更新接口依赖 `version`
- 动作接口依赖：
  - 当前状态校验
  - 现有防重复提交能力

## 建议增强

对于门户高风险动作，建议增加可选字段：

- `clientRequestId`

适用动作：

- 门户报名
- 门户投标提交
- 定标确认

## 附件契约

## 附件返回结构

统一附件摘要建议包括：

- `fileId`
- `fileName`
- `fileSize`
- `fileCategory`
- `versionNo`
- `downloadUrl`
- `previewUrl`
- `uploadTime`
- `uploaderName`

## 业务对象与附件关系

- 项目附件
  - `businessType = PROJECT`
- 标段附件
  - `businessType = LOT`
- 招标文件附件
  - `businessType = TENDER_VERSION`
- 投标附件
  - `businessType = SUBMISSION_VERSION`
- 归档附件
  - `businessType = ARCHIVE_PACKAGE`

## 主资源端点草案

## 1. 项目 `projects`

```text
POST /bid/projects/search
GET  /bid/projects/{projectId}
POST /bid/projects
PUT  /bid/projects/{projectId}
POST /bid/projects/{projectId}/actions/submit-plan
POST /bid/projects/{projectId}/actions/publish-project
POST /bid/projects/{projectId}/actions/archive-project
POST /bid/projects/{projectId}/actions/cancel-project
```

## 2. 标段 `lots`

```text
POST /bid/lots/search
GET  /bid/lots/{lotId}
POST /bid/lots
PUT  /bid/lots/{lotId}
POST /bid/lots/{lotId}/actions/close-bid
POST /bid/lots/{lotId}/actions/void-lot
```

## 3. 招标文件 `tenders`

```text
POST /bid/tenders/search
GET  /bid/tenders/{tenderId}
POST /bid/tenders
PUT  /bid/tenders/{tenderId}
POST /bid/tenders/{tenderId}/actions/publish
POST /bid/tenders/{tenderId}/actions/clarify
POST /bid/tenders/{tenderId}/actions/withdraw
```

## 4. 报名与投标 `registrations` / `submissions`

```text
POST /bid/registrations/search
POST /bid/registrations/{registrationId}/actions/approve-registration
POST /bid/registrations/{registrationId}/actions/reject-registration
POST /bid/registrations/{registrationId}/actions/cancel-registration

POST /bid/submissions/search
GET  /bid/submissions/{submissionId}
POST /bid/submissions/{submissionId}/actions/submit
POST /bid/submissions/{submissionId}/actions/withdraw
POST /bid/submissions/{submissionId}/actions/request-clarification
```

## 5. 开评定标

```text
POST /bid/openings/search
GET  /bid/openings/{openingId}
POST /bid/openings
POST /bid/openings/{openingId}/actions/start
POST /bid/openings/{openingId}/actions/complete
POST /bid/openings/{openingId}/actions/abnormal-close

POST /bid/evaluations/search
GET  /bid/evaluations/{evaluationId}
POST /bid/evaluations
POST /bid/evaluations/{evaluationId}/actions/start
POST /bid/evaluations/{evaluationId}/actions/finalize
POST /bid/evaluations/{evaluationId}/actions/rollback

POST /bid/awards/search
GET  /bid/awards/{awardId}
POST /bid/awards
POST /bid/awards/{awardId}/actions/review
POST /bid/awards/{awardId}/actions/confirm
POST /bid/awards/{awardId}/actions/rollback
```

## 门户与内部端边界

## 门户专用接口

一期建议将门户接口也收敛在 `/bid/portal/**` 下，避免与内部主系统混用。

示例：

```text
POST /bid/portal/projects/search
GET  /bid/portal/projects/{projectId}
POST /bid/portal/registrations
POST /bid/portal/submissions/{submissionId}/actions/submit
POST /bid/portal/submissions/{submissionId}/actions/withdraw
POST /bid/portal/clarifications/{clarificationId}/actions/reply
GET  /bid/portal/results/{lotId}
```

## 门户边界规则

- 门户详情不返回其他供应商信息
- 门户不返回完整评标明细
- 门户不暴露内部审批动作
- 门户上传动作必须带企业身份和项目授权校验

## 菜单与权限契约

## 菜单组件路径

后端菜单 `component` 必须严格匹配前端路径：

- 内部主系统
  - `src/views/system/bid/**/*`
- 门户
  - 建议 `src/views/bid-portal/**/*` 或独立入口

## 权限码与页面动作

- 后端 `apiPerms`
  - 控制接口访问
- 前端 `webPerms`
  - 控制按钮和页面显隐

二者必须保持同名映射，不要出现：

- 页面按钮名和接口动作名脱节
- 一个动作对应多个权限码
- 一个权限码覆盖多个不相关动作

## 与现有仓库风格的关系

当前仓库老模块大量使用：

- `/create`
- `/update`
- `/page/query`
- `/delete/{id}`

招投标域一期建议采用“资源 + 动作”的新约定，但必须满足：

- 统一响应不变
- 权限系统不变
- 前端解包方式不变
- 菜单路由装配方式不变

当前已落地的 `/bid/project/**`、`/bid/lot/**`、`/bid/registration/**` 旧式接口按 ADR-004 保留为兼容适配层；新增模块不得继续扩展旧式 `add/update/queryPage` 端点。

## 实施要求

- 在编码前先冻结主资源命名
- 在编码前先冻结动作名
- `Form / VO / 权限码 / 菜单路径` 必须同名体系化
- 联调前先产出接口清单和样例 JSON

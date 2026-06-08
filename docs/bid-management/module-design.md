# 招投标管理系统模块与接口设计

## 总体设计

- 后端
  - 在 `sa-admin/module/system` 下维护 `bid` 业务域
  - 保持现有 `controller/service/manager/dao/domain` 分层
- 前端
  - 在 `apps/web-ele/src/api/system/bid.ts` 维护招投标接口
  - 在 `apps/web-ele/src/views/system/bid` 新增业务页面
  - 继续复用现有登录、动态菜单、权限、请求封装

## 领域模块拆分

### bid-project

- 项目建档
- 标段管理
- 项目成员与组织归属
- 项目进度总览

### bid-tender

- 招标方案
- 招标文件版本
- 公告发布
- 澄清答疑

### bid-party

- 供应商关系
- 代理机构关系
- 评标专家受控角色关系
- 外部主体快照

### bid-submission

- 报名 / 受邀
- 资格初审
- 投标登记
- 投标文件与回执

### bid-opening

- 开标会
- 签到
- 唱标记录
- 开标异常

### bid-evaluation

- 评标委员会
- 评分模板
- 专家评分
- 汇总结果

### bid-award

- 定标确认
- 中标结果
- 结果公示
- 归档

## 复用现有基础能力

- `sa-base` 文件模块
  - 附件上传、下载、预览、元数据
- `sa-base` 字典模块
  - 采购方式、项目状态、资格类型等字典
- `sa-base` 消息模块
  - 站内信、待办、提醒
- `sa-base` 审计与日志
  - 操作日志、数据变更留痕
- 现有系统组织与权限模块
  - 部门、岗位、角色、数据权限

## 后端包建议

```text
sa-admin/src/main/java/net/lab1024/sa/admin/module/system/bid/
  project/
  tender/
  party/
  submission/
  opening/
  evaluation/
  award/
```

每个子模块遵循：

```text
controller/
service/
manager/
dao/
domain/
  entity/
  form/
  vo/
  dto/
constant/
```

## 前端菜单建议

- `招投标工作台`
  - 我的待办
  - 项目进度看板
- `项目管理`
  - 招标项目
  - 标段管理
- `招标管理`
  - 招标方案
  - 招标文件
  - 公告与澄清
- `投标管理`
  - 供应商名录
  - 报名管理
  - 投标登记
- `开评定标`
  - 开标记录
  - 评标管理
  - 定标结果
- `归档与统计`
  - 归档查询
  - 结果导出

供应商门户建议单独布局，但一期也可先挂在同一前端工程下按角色动态显示。

## API 契约约束

### 统一包装

- 继续使用 `ResponseDTO<T>`
- 分页继续使用 `PageParam` + `PageResult<T>`
- 前端仍按现有 `request.ts` 解包

### 路径与动作

- 新域统一命名空间：`/bid/**`
- 列表查询优先使用搜索接口
- 状态流转使用动作接口，不与普通更新混写
- 已落地的 `/bid/project/queryPage`、`/bid/lot/add` 等旧式接口仅保留为兼容适配层，新模块不再扩展旧式路径

示例：

```text
POST /bid/projects/search
GET  /bid/projects/{projectId}
POST /bid/projects
PUT  /bid/projects/{projectId}

POST /bid/tenders/{tenderId}/actions/publish
POST /bid/submissions/{submissionId}/actions/submit
POST /bid/openings/{openingId}/actions/complete
POST /bid/evaluations/{evaluationId}/actions/finalize
POST /bid/awards/{awardId}/actions/confirm
```

### DTO 规则

- 请求对象
  - `*CreateForm`
  - `*UpdateForm`
  - `*QueryForm`
  - `*ActionForm`
- 响应对象
  - `*ListVO`
  - `*DetailVO`
  - `*OptionVO`

### 详情页固定字段

- `status`
- `version`
- `allowedActions`
- `attachments`
- `auditSummary`

前端以 `allowedActions` 决定按钮可用性，不自行猜测流程权限。

## 权限模型建议

- 资源权限码
  - `bid:project:query`
  - `bid:project:create`
  - `bid:project:update`
  - `bid:project:publish`
  - `bid:submission:submit`
  - `bid:evaluation:score`
  - `bid:award:confirm`
- 数据隔离
  - 招标人只能看到本组织授权项目
  - 代理机构只能看到受托项目
  - 供应商只能看到可参与和已参与项目

## 动态菜单约束

- 后端菜单 `path` 字段统一使用 `/system/bid/{resource}/{page}`
- 后端菜单 `component` 字段必须精确映射到 `src/views/system/bid/**/*`
- 后端菜单 `component` 字段不写 `src/views` 前缀，格式统一为 `/system/bid/{resource}/{resource}-{page}.vue`
- 避免组件路径与菜单配置不一致，否则会落到占位页

## 首批实现顺序

1. `bid-project`
2. `bid-tender`
3. `bid-submission`
4. `bid-opening`
5. `bid-evaluation`
6. `bid-award`

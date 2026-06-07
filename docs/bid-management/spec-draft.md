# Spec Draft: 招投标管理系统

## Status

- `Draft v0.1`
- `Intent Confirmed`

## Objective

在现有仓库技术底座上设计一套覆盖招标、投标、开标、评标、定标、归档全过程的招投标管理系统，并将一期目标收敛为：

- 招标人/采购人内部可完成全过程管理
- 招标代理机构可在同一系统内协同推进业务
- 供应商通过轻量门户完成报名、投标、澄清、结果查看

## Confirmed Intent

- Outcome:
  - 建立一套三方协同但分阶段落地的招投标管理系统方案
- Users:
  - 招标人/采购人内部用户
  - 招标代理机构用户
  - 投标供应商用户
- Why now:
  - 现有仓库已经具备后台底座，需要围绕真实业务域落地一套完整设计
- Success:
  - 形成可执行的业务、模块、接口、实施路线图，而不是概念化方案
- Constraint:
  - 一期必须控制复杂度，供应商门户先轻量，不上公共交易平台重能力
- Out of scope:
  - CA 签章、在线解密、隔离式电子评标、监管对接、合同履约结算

## Tech Stack

- Backend
  - Java 17
  - Spring Boot 3
  - MyBatis-Plus
  - Sa-Token
  - Redis / Redisson
- Frontend
  - Vue 3
  - TypeScript
  - Vben Admin
  - Element Plus
- Existing project shape
  - 后端：`zhaotoubiao/sa-base` + `zhaotoubiao/sa-admin`
  - 前端：`vue-vben-admin-main/apps/web-ele`

## Commands

```bash
# Backend
cd zhaotoubiao
mvn -q -pl sa-admin -am -DskipTests compile

# Frontend
cd vue-vben-admin-main
pnpm install
pnpm dev:ele
pnpm build:ele
pnpm test:unit
```

## Project Structure

```text
docs/
  architecture-overview.md
  bid-management/
    README.md
    spec-draft.md
    business-process.md
    workflow-state-machine.md
    data-model.md
    domain-model.md
    permission-and-org-model.md
    api-contract.md
    module-design.md
    implementation-roadmap.md
  decisions/
    ADR-001-system-shape.md
    ADR-002-phase1-collaboration-scope.md
    ADR-003-workflow-and-api-contract.md

zhaotoubiao/
  sa-base/
  sa-admin/

vue-vben-admin-main/
  apps/web-ele/
```

## Phase 1 Scope

### Included

- 项目建档与立项信息管理
- 标段 / 包件管理
- 招标方案、公告、招标文件版本管理
- 供应商报名、投标登记、投标文件提交
- 澄清答疑
- 开标记录
- 评标模板、评分记录、汇总结果
- 定标确认与结果归档
- 附件、消息、导出、审计留痕、权限控制

### Supplier Portal Scope

- 用户注册与认证
- 可参与项目查看
- 报名 / 受邀确认
- 投标文件提交
- 澄清响应
- 结果查看

### Explicitly Excluded

- CA 签章
- 在线解密
- 隔离式电子评标环境
- 监管平台对接
- 复杂专家抽取、回避、合议机制
- 合同履约、结算、付款

## Core Business Objects

- 招标项目 `project`
- 标段 / 包件 `lot`
- 招标方案 / 招标文件 `tender`
- 参与方关系 `party`
- 报名 / 投标 `submission`
- 开标记录 `opening`
- 评标记录 `evaluation`
- 定标结果 `award`
- 附件与过程留痕 `attachment / audit`

## Core Assumptions

1. 一期采用“三方协同 + 供应商门户轻量版”，而不是完整公共交易平台。
2. 以后端单体新增 `bid` 业务域的方式最稳妥。
3. 业务主线围绕“项目/标段”展开，后续阶段均归属其生命周期。
4. 状态流转优先采用业务状态机，不先引入 BPM 引擎。
5. 前端和后端继续沿用现有权限、菜单、统一响应包装。

## Boundaries

- Always:
  - 优先复用现有 `sa-base` 与前端基础设施
  - 将状态流转、权限码、附件归属、审计留痕作为一等设计对象
  - 为供应商门户和内部主系统设计清晰边界
- Ask first:
  - 是否升级为多公司 / 多租户
  - 是否引入电子签章与加解密
  - 是否需要对接监管平台或第三方公告平台
  - 是否将专家评审升级为复杂规则引擎
- Never:
  - 在一期中把“轻量门户”扩成“完整公共交易平台”
  - 在没有冻结核心状态机前先铺开大规模开发
  - 在前后端契约未冻结前并行大面积联调

## Testing Strategy

- 设计阶段
  - 文档评审
  - 状态机与权限矩阵评审
  - API 契约评审
- 实施阶段
  - 后端最小回归：登录、权限、附件、核心流程动作
  - 前端最小回归：菜单加载、关键表单、关键列表、门户提交流程
  - 端到端回归：从建项目到定标归档的主流程

## Success Criteria

- 明确一期服务对象和协同深度
- 明确核心业务模块、后端包结构、前端菜单分区
- 明确统一 API 契约和流程动作接口形态
- 明确领域聚合、不变量、引用边界和命令模型
- 明确 P0/P1/P2/P3 分阶段路线图和验收标准
- 明确首批 ADR，避免关键设计在实现时反复摇摆

## Open Questions

1. 组织模型是单公司、多公司，还是集团多级组织？
2. 供应商门户是否需要实名、企业认证、邀请码等准入机制？
3. 开标与评标是否需要严格的时间窗、锁定机制、并发版本控制？
4. 投标文件是否要求模板化清单、必传校验、份数控制？
5. 结果公示与通知是否需要对外短信 / 邮件联动？

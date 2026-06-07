# 招投标管理系统设计工作区

## 状态

- 当前状态：`Draft v0.1`
- 一期定位：`招标人/采购人内部 + 招标代理机构主系统 + 供应商门户轻量版`
- 目标：在现有 `Spring Boot 3 + MyBatis-Plus + Sa-Token + Vue 3 / Vben` 底座上，沉淀一套可直接指导落地的招投标管理系统方案。

## 设计原则

- 先沿用现有单体后端和独立管理端，不先拆微服务。
- 先做三方协同的轻量闭环，不先做公共电子交易平台重能力。
- 以“项目/标段”为核心聚合，不把各业务阶段做成数据孤岛。
- 复用现有权限、菜单、文件、消息、日志、字典、组织能力。
- 把不可逆设计决策前置成 ADR，而不是实现时临场拍板。

## 文档地图

- `spec-draft.md`
  - 系统总规
  - 说明目标用户、一期范围、边界、成功标准、开放问题
- `business-process.md`
  - 全流程业务设计
  - 说明参与方、主流程、阶段产出、异常流转
- `workflow-state-machine.md`
  - 状态机设计
  - 说明项目、标段、投标、开标、评标、定标状态流转与回退规则
- `data-model.md`
  - 数据模型设计
  - 说明核心实体、主表关系、快照、附件、索引与审计字段
- `domain-model.md`
  - 领域模型设计
  - 说明核心聚合、实体边界、不变量、命令与事件、聚合引用规则
- `permission-and-org-model.md`
  - 组织与权限设计
  - 说明组织模型、角色模型、数据隔离、权限码与评标隔离
- `api-contract.md`
  - API 契约设计
  - 说明统一响应、资源模型、动作接口、错误语义、前后端联调约束
- `module-design.md`
  - 技术落地设计
  - 说明后端模块、前端菜单、权限码、API 约束、复用点
- `implementation-roadmap.md`
  - 分阶段实施路线图
  - 说明 P0/P1/P2/P3 目标、验收标准、主要风险

## 决策记录

- `../decisions/ADR-001-system-shape.md`
  - 沿用现有单体后端 + 独立前端
- `../decisions/ADR-002-phase1-collaboration-scope.md`
  - 一期采用“三方协同 + 供应商门户轻量版”
- `../decisions/ADR-003-workflow-and-api-contract.md`
  - 流程采用业务状态机，接口采用统一资源化契约

## 一期范围摘要

- 招标人/采购人内部
  - 项目、标段、招标文件、开标、评标、定标、归档全流程
- 招标代理机构
  - 协同办理、资料维护、流程推进、公告发布
- 供应商门户
  - 注册/认证、项目查看、报名、投标文件提交、澄清响应、结果查看

## 一期明确不做

- CA 签章
- 在线解密
- 隔离式电子评标环境
- 监管平台对接
- 复杂专家抽取与回避机制
- 合同履约、付款结算等后置业务

## 阅读顺序

1. 先看 `spec-draft.md`
2. 再看 `business-process.md`
3. 再看 `workflow-state-machine.md`
4. 再看 `data-model.md`
5. 再看 `domain-model.md`
6. 再看 `permission-and-org-model.md`
7. 再看 `api-contract.md`
8. 然后看 `module-design.md`
9. 最后看 `implementation-roadmap.md` 和 `../decisions/`

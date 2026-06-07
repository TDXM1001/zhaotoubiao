# ADR-003: 流程采用业务状态机，接口采用统一资源化契约

## Status

Accepted

## Date

2026-06-07

## Context

当前仓库已经存在稳定的统一响应与分页包装，前端请求层也按该约定解包。招投标业务又天然包含大量状态流转动作。

如果：

- 一开始引入 BPM 引擎
- 把状态流转混入通用更新接口
- 前后端不统一动作接口与权限码

则一期复杂度会失控，联调成本也会明显上升。

## Decision

- 流程控制优先采用业务状态机
- API 继续沿用统一包装
- 状态流转使用显式动作接口

示例：

```text
POST /bid/projects/search
POST /bid/tenders/{tenderId}/actions/publish
POST /bid/submissions/{submissionId}/actions/submit
POST /bid/evaluations/{evaluationId}/actions/finalize
```

## Alternatives Considered

### 一期引入 BPM / 工作流引擎

- Pros
  - 理论上更灵活
- Cons
  - 与当前业务边界不匹配
  - 配置和运行复杂度显著提高

### 继续大量使用 `add/update/delete/query`

- Pros
  - 与老模块风格一致
- Cons
  - 无法清晰表达状态动作
  - 难以把权限和按钮能力收敛到资源动作

## Consequences

- 一期实现与联调路径更清晰
- 前端可以基于 `allowedActions` 决定交互
- 后续若流程复杂度显著提升，再评估 BPM 化，而不是预先引入

# 面向导师的项目讲解框架（代码实现导向）

## 1. 讲解目标与边界

- 目标：让导师清楚看到“功能如何落地到代码”，而不仅是功能清单。
- 边界：本版本聚焦“项目本身”，后续可在此基础上扩展为毕业答辩版本（研究背景、实验评估、创新点强化）。
- 主线：`角色场景 -> 请求入口 -> 业务编排 -> 算法/规则 -> 数据落库 -> 返回前端`。

## 2. 建议的讲解总结构（30~40 分钟）

1. 项目定位与核心问题（3 分钟）
2. 系统架构与分层（5 分钟）
3. 三条核心业务流程（12 分钟）
4. 推荐模块实现细节（10 分钟）
5. 数据模型与一致性约束（5 分钟）
6. 已知问题与下一步演进（3~5 分钟）

## 3. 每一部分怎么讲（含代码锚点）

### 3.1 项目定位（先讲“为什么”）

- 问题定义：竞赛信息分散、队伍匹配效率低、审核链路缺少闭环。
- 你的回答：用统一平台打通“竞赛管理 + 报名审核 + 组队协作 + 推荐”。
- 建议引用文档：
  - `docs/thesis/ch4_system_structure_notes.md`
  - `docs/thesis/ch4_system_module_and_api.md`

### 3.2 架构与分层（讲“系统如何组织”）

- 前后端分离：Vue3 前端 + Spring Boot 后端 + MySQL。
- 后端分层：Controller（入口）-> Service（规则）-> Repository（持久化）。
- 鉴权链路：JWT + Spring Security 过滤链。
- 代码锚点：
  - `backend/src/main/java/com/competition/config/SecurityConfig.java`
  - `backend/src/main/java/com/competition/security/JwtAuthenticationFilter.java`
  - `backend/src/main/java/com/competition/controller`
  - `backend/src/main/java/com/competition/service`

### 3.3 核心流程一：学生“浏览竞赛 -> 报名申请 -> 入队”

- 讲解顺序：
  - 列表与推荐入口：`CompetitionController.getCompetitions(...)`
  - 报名创建：`ApplicationService.createApplication(...)`
  - 教师审核：`ApplicationService.review(...)`
- 关键规则：
  - 报名时校验竞赛截止时间、队伍状态、重复有效申请。
  - 审核通过时校验队伍容量并写入 `team_members`。
  - 报名成功触发行为埋点（APPLY）。
- 代码锚点：
  - `backend/src/main/java/com/competition/controller/CompetitionController.java`
  - `backend/src/main/java/com/competition/service/ApplicationService.java`
  - `backend/src/main/java/com/competition/service/UserBehaviorService.java`

### 3.4 核心流程二：教师“申请带队 -> 管理队伍 -> 审核学生”

- 讲解顺序：
  - 教师申请提交与分页查询（TeacherApplication 模块）
  - 管理员审核后生成队伍并同步技能方向
  - 教师在队伍内执行审核与成员管理
- 价值点：
  - 形成“带队资格治理”和“学生入队治理”两条审批链。
- 代码锚点：
  - `backend/src/main/java/com/competition/service/TeacherApplicationService.java`
  - `backend/src/main/java/com/competition/controller/AdminTeacherApplicationController.java`
  - `backend/src/main/java/com/competition/controller/TeacherTeamApplicationController.java`

### 3.5 核心流程三：管理员“竞赛治理 -> 奖项发布 -> 荣誉沉淀”

- 讲解顺序：
  - 竞赛发布/更新（状态管理）
  - 奖项发布（按发布时成员快照固化获奖记录）
  - 用户荣誉查询（历史稳定，不受后续成员变动影响）
- 代码锚点：
  - `backend/src/main/java/com/competition/service/AdminCompetitionService.java`
  - `backend/src/main/java/com/competition/service/AwardAdminService.java`
  - `backend/src/main/java/com/competition/service/UserHonorService.java`

### 3.6 推荐模块（重点讲“怎么实现”）

- 推荐入口：
  - 竞赛推荐：`GET /api/competitions?recommend=true`
  - 队伍推荐：`GET /api/competitions/{competitionId}/teams/recommend`
- 代码执行主链：
  - 入口编排：`CompetitionService.getCompetitions(...)`
  - 混合计算：`RecommendationService.calculateCompetitionHybridScores(...)`
  - 内容推荐：`ContentBasedAlgorithm.calculateCompetitionSimilarity(...)`
  - 协同过滤：`CollaborativeFilteringAlgorithm.scoreCompetitionsForUser(...)`
  - 行为采集：`UserBehaviorService.recordCompetitionView(...)` / `recordCompetitionApply(...)`
- 关键机制：
  - `alpha=0.8` 线性融合（内容为主，CF 为辅）。
  - 行为不足（<3）或 CF 无结果时 fallback 到内容推荐。
  - 未登录/无技能画像时返回默认排序并标记 fallback 原因。

## 4. 建议你给导师“展示代码”的固定模板

每个功能都按下面 6 句讲，逻辑会非常清楚：

1. 这个功能解决什么问题。
2. 用户从哪个页面/接口进入。
3. Controller 做了什么参数与权限处理。
4. Service 执行了哪些核心业务规则。
5. Repository/数据库最终写入了什么。
6. 返回前端时给了什么关键字段（例如 `matchScore`、`recommendReason`、状态位）。

## 5. 建议的“现场演示脚本”（8~12 分钟）

1. 学生登录，打开竞赛列表，切换 `recommend=true`，展示推荐理由字段。
2. 进入竞赛详情，触发 `VIEW` 行为后再次回列表，解释推荐变化依据。
3. 学生提交报名，展示 `APPLY` 行为写入后的链路闭环。
4. 教师端审核申请，展示成员入队结果与状态变更。
5. 管理员发布奖项，展示学生荣誉页快照数据。

## 6. 你现在就可以补齐的两项“讲解可信度增强”

1. 文档一致性修复  
   `README.md` 仍有旧技术栈描述（React），建议改为当前 Vue3 技术栈，避免导师提问时出现口径冲突。
2. 编码与可读性修复  
   `docs/db_schema.md` 当前存在乱码（编码不一致），建议统一为 UTF-8 并校对字段注释。

## 7. 向毕业答辩扩展时，直接在本框架上新增

- 增加“研究动机与对比”：为何选混合推荐而非纯内容/纯协同。
- 增加“实验设计与指标”：命中率、覆盖率、满意度问卷、冷启动表现。
- 增加“可扩展性与风险”：数据稀疏、行为噪声、权限安全、并发一致性。
- 增加“演进路线”：在线学习、实时特征、解释性增强、A/B 测试。

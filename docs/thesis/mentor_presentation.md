# 面向导师的项目讲解稿（Competition Platform）

## 1. 讲解起点：先从系统入口与职责边界开始

我会先说明这个项目不是“单点功能”，而是一个完整闭环系统，核心目标是把竞赛组织过程数字化，并把推荐能力嵌入业务流程。

- 前端负责交互与流程引导：`frontend/src/views`、`frontend/src/router`、`frontend/src/api`
- 后端负责规则执行与数据一致性：`controller -> service -> repository`
- 推荐模块不是独立服务，而是嵌入竞赛业务主链路

讲到这里马上切代码入口，先看后端：

- 统一接口入口：`backend/src/main/java/com/competition/controller`
- 核心业务编排：`backend/src/main/java/com/competition/service`
- 算法实现：`backend/src/main/java/com/competition/algorithm`
- 数据模型：`backend/src/main/java/com/competition/entity`

## 2. 第二部分：从“竞赛列表接口”展开整条主链路

我会从最关键的查询入口开始讲，因为它把普通列表与推荐列表统一了。

### 2.1 入口模块如何启动

- 控制器起点：`CompetitionController.getCompetitions(...)`
- 该方法接收：分页参数、筛选参数、`recommend`、`topK`
- 如果 `recommend=true`，控制器会尝试从 JWT 中解析 `userId`，再交给服务层

### 2.2 服务层如何分流

进入 `CompetitionService.getCompetitions(...)` 后，流程是：

1. 先计算 `effectiveTopK`（限制最大值，避免一次返回过多）
2. 检查当前用户是否可推荐（是否登录、是否有技能画像）
3. 不满足推荐条件时，直接进入 `getCompetitionsDefault(...)` 走默认列表
4. 满足条件时，进入推荐计算主流程

### 2.3 推荐主流程如何运作

推荐计算由 `RecommendationService.calculateCompetitionHybridScores(...)` 统一编排：

1. 先算内容推荐分（技能画像匹配）
2. 再判断行为数据是否足够支持协同过滤
3. 如果行为不足或协同过滤无结果，直接 fallback 到内容分
4. 如果协同过滤可用，按 `alpha=0.8` 做线性融合
5. 返回每个竞赛的最终分数，服务层按分数排序并回填推荐理由

这里我会强调一句：推荐不是“有就用、没有就报错”，而是“始终可用”的设计，核心依赖 fallback。

## 3. 第三部分：推荐模块内部拆解（算法级讲解）

这一部分是导师最关心的“你到底怎么做的”。

### 3.1 内容推荐：从用户技能到竞赛技能

- 类：`ContentBasedAlgorithm`
- 方法：`calculateCompetitionSimilarity(...)`
- 逻辑步骤：
  1. 读取用户技能向量（`user_skills`）
  2. 读取每个竞赛的技能需求向量（`competition_skills`）
  3. 对每个竞赛计算余弦相似度
  4. 输出 `competitionId -> score`

这部分解决的是“用户会什么”和“竞赛要什么”之间的匹配问题。

### 3.2 协同过滤：从行为数据到邻居偏好

- 类：`CollaborativeFilteringAlgorithm`
- 核心方法：`scoreCompetitionsForUser(...)`
- 逻辑步骤：
  1. 读取该用户在竞赛上的历史行为偏好（来自 `user_behaviors`）
  2. 找到与其有交集行为的候选用户
  3. 计算用户相似度（余弦相似度）
  4. 选取 Top-K 邻居（`NEIGHBOR_K=20`）
  5. 聚合邻居对未交互竞赛的偏好，得到 CF 分数
  6. 归一化后输出

这部分解决的是“和你行为类似的人喜欢什么”。

### 3.3 混合策略：为什么要融合

- 类：`RecommendationService`
- 方法：`calculateCompetitionHybridScores(...)`
- 公式：`final = 0.8 * content + 0.2 * cf`
- 设计意图：
  - 内容推荐可解释性好，冷启动友好
  - 协同过滤个性化更强，但依赖行为密度
  - 融合后稳定性和个性化同时兼顾

## 4. 第四部分：行为闭环如何形成（推荐数据从哪里来）

这部分要讲清楚推荐不是“离线假设”，而是业务实时沉淀。

### 4.1 浏览行为如何写入

- 触发点：`CompetitionService.getCompetitionById(...)`
- 调用：`recordCompetitionViewSafely(...)`
- 落库：`UserBehaviorService.recordCompetitionView(...)`
- 结果：写入 `user_behaviors` 的 `VIEW` 行为

### 4.2 报名行为如何写入

- 触发点：`ApplicationService.createApplication(...)`
- 调用：`UserBehaviorService.recordCompetitionApply(...)`
- 结果：写入 `user_behaviors` 的 `APPLY` 行为

### 4.3 行为权重如何影响推荐

在 `UserBehaviorService` 中：

- `VIEW_WEIGHT = 1`
- `APPLY_WEIGHT = 5`

这意味着报名行为被视为更强偏好信号，后续协同过滤会优先受这类行为影响。

## 5. 第五部分：报名与审核主流程（业务规则实现）

推荐只是入口，系统核心价值在于“从推荐到成队”的闭环执行。

### 5.1 学生报名是如何执行的

- 起点：`ApplicationService.createApplication(...)`
- 执行顺序：
  1. 校验参数完整性（competitionId、teamId）
  2. 校验当前用户角色必须是学生
  3. 校验队伍属于该竞赛、队伍状态可招募
  4. 校验竞赛报名截止时间
  5. 校验是否已有有效申请（防重复）
  6. 保存申请记录
  7. 记录 APPLY 行为

### 5.2 教师审核是如何执行的

- 起点：`ApplicationService.review(...)`
- 执行顺序：
  1. 校验审核者必须是教师
  2. 校验该教师是否队伍负责人
  3. 校验申请状态是否仍为待审核
  4. 审核通过时检查队伍容量、竞赛状态、重复成员
  5. 写入 `team_members`
  6. 更新申请状态与审核信息

这部分体现系统在 Service 层做“强业务约束”，不是仅靠前端限制。

## 6. 第六部分：教师申请与管理员治理链路

这一段我会用来说明平台不是单纯学生端工具，而是多角色治理系统。

### 6.1 教师申请链路

- 教师提交带队申请：`TeacherApplicationService` 相关创建流程
- 管理员审核教师申请：`AdminTeacherApplicationController` + 对应 Service
- 审核通过后自动生成队伍并同步技能方向

### 6.2 管理员竞赛治理

- 竞赛发布/更新：`AdminCompetitionService`、`CompetitionService.updateCompetition(...)`
- 用于控制竞赛状态、报名时间窗口、队伍招募边界

### 6.3 奖项发布与荣誉沉淀

- 奖项发布：`AwardAdminService`
- 学生荣誉查询：`UserHonorService`
- 关键点：奖项发布时固化成员快照，避免后续队伍成员变化影响历史结果

## 7. 第七部分：数据库如何支撑上述流程

这部分对应 `docs/db_schema.md`，讲“表如何服务流程”，而不是逐表念字段。

建议按业务链路讲表关系：

1. 用户与画像：`users`、`user_skills`
2. 竞赛与需求：`competitions`、`competition_skills`
3. 队伍与能力方向：`teams`、`team_skills`、`team_members`
4. 报名审核：`applications`、`teacher_applications`
5. 推荐行为：`user_behaviors`
6. 结果沉淀：`team_awards`、`award_recipients`

核心说明点：

- 唯一约束用于防重复申请、防重复获奖记录
- 外键关系用于保证流程链路可追溯
- 行为表用于支撑推荐持续优化

## 8. 第八部分：演示顺序（汇报时可直接照做）

1. 进入竞赛列表，先展示普通列表，再打开 `recommend=true` 展示推荐差异
2. 打开竞赛详情，说明 `VIEW` 行为会被记录
3. 学生提交申请，说明 `APPLY` 行为会强化偏好
4. 教师端审核申请，展示成员写入与状态变化
5. 管理员发布奖项，展示荣誉页结果沉淀

每一步都按同一句式收束：  
“入口在哪个 Controller，规则在哪个 Service，数据写到了哪张表，返回字段如何被前端展示。”

## 9. 讲解收束（结论）

本项目的实现重点不是“单个页面”或“单个算法”，而是把竞赛组织过程中的关键环节串成了可执行、可追溯、可推荐优化的完整闭环：

- 业务上：报名、审核、组队、协作、评奖全链路可落地
- 技术上：分层清晰，规则集中在 Service，推荐与业务深度融合
- 数据上：结构化沉淀画像与行为，支持后续算法持续迭代

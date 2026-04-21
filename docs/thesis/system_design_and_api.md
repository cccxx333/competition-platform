# 系统模块与接口整理

## 1. 项目实际模块总览

### 1.1 后端模块（`backend/src/main/java/com/competition`）

| 模块类别 | 实际目录/类 | 说明 |
|---|---|---|
| 核心业务模块 | `controller` + `service` + `repository` + `entity` + `dto` | 围绕竞赛、队伍、申请、教师申请、奖项、技能、用户等业务闭环 |
| 推荐模块 | `algorithm`（`ContentBasedAlgorithm`、`CollaborativeFilteringAlgorithm`）+ `RecommendationService` + `UserBehaviorService` | 已并入竞赛域接口，不再独立暴露 `/api/recommendations` |
| 认证鉴权模块 | `config/SecurityConfig` + `security/*` + `utils/JwtUtils` | JWT 鉴权、无状态会话、接口访问控制 |
| 系统支撑模块 | `bootstrap/DatabaseBootstrapInitializer`、`scheduler/CompetitionStatusScheduler`、`config/WebConfig` | 数据库初始化、竞赛状态定时同步、静态文件映射与 CORS |
| 异常处理模块 | `exception/*` | 统一异常对象与全局异常处理 |

### 1.2 前端功能区（`frontend/src`）

| 模块类别 | 实际目录 | 说明 |
|---|---|---|
| 页面层 | `views` | 业务主页面（竞赛、队伍、申请、个人中心、管理端） |
| 路由层 | `router` | 统一路由注册与登录守卫 |
| 接口层 | `api` | 与后端 REST 接口一一对应 |
| 状态管理 | `stores/auth.ts` + `pinia/*` | 认证态、用户态、布局等状态 |
| 布局与通用组件 | `layouts` + `components` + `common/components` | 左侧导航、页面框架、状态标签等 |

### 1.3 当前实现中的“核心业务模块”与“支撑模块”划分

- 核心业务模块：竞赛管理、队伍协作、报名申请、教师申请审核、技能维护、奖项发布与荣誉查询、推荐服务。
- 支撑模块：认证鉴权（JWT + Security）、数据库初始化、状态调度、文件上传与静态访问、全局异常处理。

## 2. 角色与功能映射

### 2.1 STUDENT

- 主要页面/功能：
  - `竞赛列表`（`/competitions`，可切换算法推荐模式）
  - `竞赛报名`（`/competitions/apply`）
  - `我的申请`（`/teams/my-applications`）
  - `我的队伍`（`/teams/my`）
  - `队伍详情/讨论/提交/成员`（通过详情页进入）
  - `个人信息`（`/me/profile`）
  - `技能`（`/me/skills`）
  - `荣誉`（`/me/honors`）
- 核心接口类别：竞赛查询/推荐、报名申请、我的申请与队伍、个人技能与荣誉。
- 主要业务对象：`competitions`、`applications`、`teams`、`team_members`、`user_skills`、`user_behaviors`、`team_awards`。

### 2.2 TEACHER

- 主要页面/功能：
  - `竞赛列表/详情`（详情页可提交教师申请）
  - `我的教师申请`（`/teacher/applications`）
  - `队伍查询`（`/teams/lookup`，仅查看本人相关队伍）
  - `教师审核`（`/teams/review`，审核学生入队申请）
  - `队伍详情/成员/讨论/提交`（作为指导教师管理）
  - `个人信息`（`/me/profile`）
- 核心接口类别：教师申请提交与查询、学生申请审核、队伍管理。
- 主要业务对象：`teacher_applications`、`teacher_application_skills`、`teams`、`applications`、`team_members`。

### 2.3 ADMIN

- 主要页面/功能：
  - `竞赛列表`（可发布竞赛）
  - `竞赛详情`（可修改竞赛状态）
  - `教师申请审核`（`/admin/teacher-applications`）
  - `队伍查询`（`/teams/lookup`）
  - `奖项发布`（`/admin/awards/publish`）
  - `技能管理`（`/me/skills`，管理员模式）
  - `个人信息`（`/me/profile`）
- 核心接口类别：竞赛管理、教师申请审核、奖项管理、技能管理、队伍解散。
- 主要业务对象：`competitions`、`teacher_applications`、`team_awards`、`award_recipients`、`skills`、`teams`。

## 3. 后端模块拆解

### 3.1 用户与认证模块

- 模块作用：完成注册、登录、JWT 鉴权、当前用户信息与技能绑定。
- 核心实体/表：`users`、`user_skills`。
- 主要 Controller：`UserController`。
- 主要 Service：`UserService`。
- 主要接口：`/api/users/register`、`/api/users/login`、`/api/users/me`、`/api/users/me/skills*`。
- 依赖关系：被竞赛、申请、队伍、推荐等所有业务模块引用。

### 3.2 竞赛模块

- 模块作用：竞赛列表/详情/检索、管理员更新竞赛、公开可报名竞赛查询。
- 核心实体/表：`competitions`、`competition_skills`。
- 主要 Controller：`CompetitionController`、`AdminCompetitionController`。
- 主要 Service：`CompetitionService`、`AdminCompetitionService`。
- 主要接口：`/api/competitions`、`/api/competitions/{id}`、`/api/admin/competitions/{id}`。
- 依赖关系：依赖推荐服务、行为服务、申请模块、队伍模块。

### 3.3 队伍模块

- 模块作用：队伍详情、成员管理、关闭招募、管理员解散、我的队伍/我的队伍列表。
- 核心实体/表：`teams`、`team_members`、`team_skills`。
- 主要 Controller：`TeamController`。
- 主要 Service：`TeamService`。
- 主要接口：`/api/teams/*`、`/api/users/me/team`。
- 依赖关系：与竞赛、申请、教师申请、奖项、讨论、提交模块紧耦合。

### 3.4 报名申请模块（学生入队申请）

- 模块作用：学生提交报名申请、教师审核申请。
- 核心实体/表：`applications`、`team_members`。
- 主要 Controller：`ApplicationController`、`TeacherTeamApplicationController`。
- 主要 Service：`ApplicationService`。
- 主要接口：`/api/applications`、`/api/teacher/applications`、`/api/teacher/applications/{id}/review`。
- 依赖关系：依赖竞赛、队伍、用户；审核通过时写入队伍成员；申请成功时写入行为数据。

### 3.5 教师申请模块

- 模块作用：教师申请带队、管理员审核，审核通过后自动生成教师队伍并同步技能方向。
- 核心实体/表：`teacher_applications`、`teacher_application_skills`、`teams`、`team_skills`。
- 主要 Controller：`TeacherApplicationController`、`AdminTeacherApplicationController`。
- 主要 Service：`TeacherApplicationService`。
- 主要接口：`/api/teacher-applications`、`/api/admin/teacher-applications`、`/api/admin/teacher-applications/{id}/review`。
- 依赖关系：依赖竞赛、用户、技能、队伍模块。

### 3.6 技能模块

- 模块作用：技能字典维护（管理员）与用户技能维护（用户本人）。
- 核心实体/表：`skills`、`user_skills`。
- 主要 Controller：`SkillController`、`UserController`（`/me/skills`）。
- 主要 Service：`SkillService`、`UserService`。
- 主要接口：`/api/skills`、`/api/skills/search`、`/api/users/me/skills*`。
- 依赖关系：被推荐模块、教师申请模块、队伍技能模块复用。

### 3.7 奖项模块

- 模块作用：管理员发布奖项、记录获奖快照、用户荣誉查询。
- 核心实体/表：`team_awards`、`award_recipients`、`team_members`。
- 主要 Controller：`AdminAwardController`、`UserHonorController`。
- 主要 Service：`AwardAdminService`、`UserHonorService`。
- 主要接口：`/api/admin/awards`、`/api/admin/awards/records`、`/api/users/me/honors`。
- 依赖关系：依赖竞赛状态、队伍状态、成员快照。

### 3.8 推荐模块

- 模块作用：竞赛推荐（内容 + 协同过滤混合）与队伍推荐（技能匹配）。
- 核心实体/表：`user_skills`、`competition_skills`、`team_skills`、`user_behaviors`。
- 主要 Controller：入口位于 `CompetitionController`（非独立控制器）。
- 主要 Service/Algorithm：`CompetitionService`、`RecommendationService`、`UserBehaviorService`、`ContentBasedAlgorithm`、`CollaborativeFilteringAlgorithm`。
- 主要接口：`/api/competitions?recommend=true`、`/api/competitions/{competitionId}/teams/recommend`。
- 依赖关系：依赖用户技能、竞赛技能、队伍技能、行为日志。

## 4. 前端功能模块拆解

| 页面名称 | 页面路径 | 面向角色 | 主要功能 | 对应后端接口类别 | 论文第四章价值 |
|---|---|---|---|---|---|
| 登录 | `/login` | 全部 | 登录获取 Token | 用户认证 | 体现鉴权入口设计 |
| 仪表盘（角色分流） | `/dashboard/*` | 全部 | 按角色展示统计概览 | 竞赛/申请/荣誉统计接口 | 体现角色化门户 |
| 竞赛列表 | `/competitions` | 全部（算法推荐 UI 主要给学生） | 列表查询、推荐模式、管理员发布竞赛 | 竞赛列表/创建接口 | 体现竞赛管理与推荐入口统一 |
| 竞赛详情 | `/competitions/:id` | 全部 | 查看详情、教师提交申请、管理员改状态 | 竞赛详情、教师申请、竞赛更新 | 体现单竞赛场景下多角色分工 |
| 竞赛报名 | `/competitions/apply` | 学生 | 选择竞赛、拉取推荐队伍、发起报名 | 推荐队伍、创建申请 | 体现“推荐到报名”闭环 |
| 我的申请 | `/teams/my-applications` | 学生 | 查看申请状态与原因 | 我的申请 | 体现报名流程可追踪 |
| 我的队伍 | `/teams/my` | 学生 | 查看本人队伍并进入详情 | 我的队伍列表 | 体现学生侧队伍入口 |
| 队伍查询 | `/teams/lookup` | 教师/管理员 | 按关键词查询队伍 | 队伍查询接口 | 体现管理侧队伍检索 |
| 队伍详情 | `/teams/:teamId` | 学生/教师/管理员 | 查看队伍信息、关闭招募、解散、进入讨论/提交 | 队伍详情/成员/关闭/解散/奖项摘要 | 体现队伍协作中枢 |
| 成员管理 | `/teams/:teamId/members` | 教师/管理员（学生可读） | 查看成员、移除成员 | 队伍成员列表/移除 | 体现协作权限边界 |
| 队伍讨论 | `/teams/:teamId/posts` + 线程页 | 队伍成员/教师/管理员 | 发帖、回复、删除本人帖 | 讨论接口 | 体现队内沟通能力 |
| 队伍提交 | `/teams/:teamId/submissions` | 队伍成员/教师/管理员 | 上传版本、查看历史、下载 | 提交接口 | 体现成果提交链路 |
| 教师申请列表 | `/teacher/applications` | 教师 | 查看个人教师申请进度 | 教师申请分页查询 | 体现教师端业务闭环 |
| 教师审核 | `/teams/review` | 教师 | 审核学生入队申请 | 教师审核接口 | 体现教师审核职责 |
| 教师申请审核 | `/admin/teacher-applications` | 管理员 | 审核教师申请 | 管理员审核教师申请 | 体现平台治理能力 |
| 奖项发布 | `/admin/awards/publish` | 管理员 | 发布奖项并查看记录 | 管理员奖项接口 | 体现竞赛结果管理 |
| 个人信息 | `/me/profile` | 全部 | 查看个人信息 | 用户信息接口 | 体现用户中心 |
| 技能/技能管理 | `/me/skills` | 学生/管理员 | 学生维护技能；管理员维护技能字典 | 用户技能 + 技能字典接口 | 体现推荐输入维护 |
| 荣誉 | `/me/honors` | 学生 | 查看获奖与参赛统计 | 荣誉接口 | 体现成果沉淀 |

## 5. 核心接口整理

### 5.1 登录/认证

| 接口路径 | 方法 | 主要参数 | 返回概述 | 面向角色 | 业务作用 |
|---|---|---|---|---|---|
| `/api/users/login` | POST | `username`,`password` | `token`,`message` | 全部 | 登录并签发 JWT |
| `/api/users/register` | POST | 用户注册信息 | 用户 DTO | 全部 | 用户注册 |
| `/api/users/me` | GET | Header: `Authorization` | 当前用户资料 | 全部 | 前端加载角色与身份 |

说明：前端实际通过 `authStore.loadMe()` 使用 `/users/me` 获取角色，不依赖登录响应中的角色字段。

### 5.2 用户信息与技能

| 接口路径 | 方法 | 主要参数 | 返回概述 | 面向角色 | 业务作用 |
|---|---|---|---|---|---|
| `/api/users/me` | GET | 无 | 用户资料 | 全部 | 个人中心 |
| `/api/users/me/skills` | GET | 无 | 当前用户技能列表 | 学生 | 推荐画像输入 |
| `/api/users/me/skills` | POST | `skillId`,`level/proficiency` | 绑定结果 | 学生 | 技能绑定 |
| `/api/users/me/skills/{skillId}/level` | PUT | `level` | 更新结果 | 学生 | 熟练度调整 |
| `/api/users/me/skills/{skillId}` | DELETE | skillId | 无 | 学生 | 技能解绑 |
| `/api/skills` | GET | `sortBy` | 技能字典列表 | 全部 | 选择技能 |
| `/api/skills` | POST | 技能信息 | 技能对象 | 管理员 | 新建技能 |
| `/api/skills/{id}` | PUT | 技能信息 | 技能对象 | 管理员 | 更新技能 |

### 5.3 竞赛列表/详情/管理

| 接口路径 | 方法 | 主要参数 | 返回概述 | 面向角色 | 业务作用 |
|---|---|---|---|---|---|
| `/api/competitions` | GET | `keyword`,`status`,`recommend`,`topK`,`applyable`,`page`,`size` | 竞赛分页，含 `matchScore/recommend/recommendReason` | 全部 | 列表与推荐统一入口 |
| `/api/competitions/{id}` | GET | id | 竞赛详情 | 全部 | 查看详情（并触发浏览行为记录） |
| `/api/competitions` | POST | 竞赛创建字段 | 新建竞赛 | 管理员（前端约束） | 发布竞赛 |
| `/api/competitions/{id}` | PUT | 更新字段 | 更新结果 | 管理员（服务层校验） | 更新竞赛状态 |
| `/api/admin/competitions/{id}` | PUT | 管理员更新字段 | 更新结果 | 管理员 | 后台更新竞赛 |

### 5.4 队伍相关

| 接口路径 | 方法 | 主要参数 | 返回概述 | 面向角色 | 业务作用 |
|---|---|---|---|---|---|
| `/api/teams` | GET | `page`,`size` | 队伍分页 | 全部 | 队伍列表 |
| `/api/teams/search` | GET | `keyword` | 队伍列表 | 管理员/教师 | 条件查询 |
| `/api/teams/mine` | GET | `keyword` | 当前用户队伍列表 | 学生/教师 | 我的队伍 |
| `/api/teams/{id}` | GET | id | 队伍详情 | 全部 | 队伍主信息 |
| `/api/teams/{teamId}/members` | GET | teamId | 成员列表 | 成员/教师/管理员 | 成员查看 |
| `/api/teams/{teamId}/members/{userId}` | DELETE | `reason` | `ok` | 教师/管理员 | 移除成员 |
| `/api/teams/{teamId}/close` | PUT | teamId | 队伍对象 | 教师/管理员 | 关闭招募 |
| `/api/teams/{teamId}/disband` | PUT | teamId | 队伍对象 | 管理员 | 解散队伍 |
| `/api/teams/{teamId}/award` | GET | teamId | 奖项摘要 | 全部 | 显示队伍获奖状态 |

### 5.5 报名与审核

| 接口路径 | 方法 | 主要参数 | 返回概述 | 面向角色 | 业务作用 |
|---|---|---|---|---|---|
| `/api/applications` | POST | `competitionId`,`teamId` | 申请对象 | 学生 | 提交报名 |
| `/api/users/me/applications` | GET | `competitionId`(可选) | 我的申请列表 | 学生 | 查询申请 |
| `/api/teacher/applications` | GET | `teamId`,`status` | 待审申请列表 | 教师 | 审核入口 |
| `/api/teacher/applications/{id}/review` | PUT | `approved`,`reason` | 审核后的申请对象 | 教师 | 审核申请 |

### 5.6 教师申请

| 接口路径 | 方法 | 主要参数 | 返回概述 | 面向角色 | 业务作用 |
|---|---|---|---|---|---|
| `/api/teacher-applications` | POST | `competitionId`,`description`,`skills` | 教师申请对象 | 教师 | 提交带队申请 |
| `/api/teacher-applications` | GET | `page`,`size`,`status` | 教师申请分页 | 教师 | 查看个人申请 |
| `/api/admin/teacher-applications` | GET | `page`,`size`,`status`,`keyword` | 管理员审核分页 | 管理员 | 审核列表 |
| `/api/admin/teacher-applications/{id}/review` | PUT | `approved`,`reviewComment` | 审核结果 | 管理员 | 审核教师申请 |

### 5.7 奖项管理

| 接口路径 | 方法 | 主要参数 | 返回概述 | 面向角色 | 业务作用 |
|---|---|---|---|---|---|
| `/api/admin/awards` | POST | `competitionId`,`teamId`,`awardName` | 奖项发布结果与获奖成员 ID | 管理员 | 发布奖项 |
| `/api/admin/awards/records` | GET | `competitionId`,`teamId`,`size` | 奖项记录列表 | 管理员 | 查询奖项记录 |
| `/api/users/me/honors` | GET | 无 | 参赛次数、获奖次数、奖项详情 | 学生 | 荣誉展示 |

### 5.8 推荐相关

| 接口路径 | 方法 | 主要参数 | 返回概述 | 面向角色 | 业务作用 |
|---|---|---|---|---|---|
| `/api/competitions` | GET | `recommend=true`,`topK` | 推荐排序后的竞赛分页，含推荐理由 | 学生（匿名可降级） | 竞赛推荐 |
| `/api/competitions/{competitionId}/teams/recommend` | GET | `topK` | 队伍推荐列表、匹配分、理由、`fallbackSorted` | 学生 | 组队推荐 |

旧接口说明：

- `backend/controller/RecommendationController.java` 标注已废弃，占位文件；`/api/recommendations` 已移除。
- `backend/dto/RecommendationResult.java`、`frontend/src/api/recommendations.ts`、`frontend/src/views/recommendations/CompetitionRecommendations.vue` 为历史遗留/未启用。

## 6. 推荐相关接口与调用链专项整理

### 6.1 推荐功能入口

- 竞赛推荐入口：`GET /api/competitions?recommend=true&topK=...`
- 队伍推荐入口：`GET /api/competitions/{competitionId}/teams/recommend?topK=...`
- 已确认推荐能力并入 `competitions` 域，不再走独立 recommendation 控制器。

### 6.2 recommend=true 启用条件

- `CompetitionController` 接收 `recommend` 参数。
- `CompetitionService.getCompetitions(...)` 中：
  - `recommend=false` 时走默认列表逻辑。
  - `recommend=true` 时，先校验回退原因（未登录/无技能），满足条件后进入混合推荐排序。

### 6.3 topK 传递路径

- 查询参数 `topK` -> `CompetitionController` -> `CompetitionService`。
- `calculateEffectiveTopK`：默认 `10`，上限 `50`。
- 同样用于队伍推荐接口的数量截断。

### 6.4 team recommendation 接口情况

- 实际存在：`/api/competitions/{competitionId}/teams/recommend`。
- 服务实现：`CompetitionService.recommendTeams` + `RecommendationService.calculateTeamMatchScores`。

### 6.5 推荐理由返回

- 竞赛推荐：`CompetitionResponse.recommendReason`（字符串，来源 `buildCompetitionRecommendReason`）。
- 队伍推荐：`TeamRecommendationResponse.reasons`（`TeamRecommendReason` 列表）。

### 6.6 行为数据写入触发点

- `VIEW`：`CompetitionService.getCompetitionById` 成功后调用 `recordCompetitionViewSafely`。
- `APPLY`：`ApplicationService.createApplication` 成功后调用 `userBehaviorService.recordCompetitionApply`。

### 6.7 VIEW/APPLY 权重可见性

- `UserBehaviorService`：`VIEW_WEIGHT=1`，`APPLY_WEIGHT=5`（写入 `user_behaviors.weight`）。
- 协同过滤打分 (`scoreCompetitionsForUser`) 使用行为表中的 `weight` 聚合。
- `CollaborativeFilteringAlgorithm` 中仍保留 `getActionWeight(VIEW=1, APPLY=3, JOIN=5...)` 的旧路径方法，属于并存实现；当前竞赛混合推荐主要走 `scoreCompetitionsForUser` 路径。

### 6.8 协同过滤不可用时回退

- 回退层次：
  - 用户未登录/无技能：直接回退默认列表。
  - 行为不足（<3）或 CF 结果为空：混合推荐回退到内容推荐分。
  - 混合分为空：回退默认列表。
- 队伍推荐中另有排序回退：当最高匹配分 `<0.10`，按默认创建时间排序并标记 `fallbackSorted=true`。

## 7. 供论文第四章使用的模块设计摘要

### 7.1 用户与认证模块

- 系统采用 JWT 无状态认证机制，登录后通过统一请求头完成身份传递。
- 用户模块不仅承担登录与个人资料维护，还负责技能画像维护，为后续推荐计算提供结构化输入。
- 角色类型固定为 `STUDENT/TEACHER/ADMIN`，在业务服务层执行权限约束。

### 7.2 竞赛管理模块

- 竞赛模块提供统一列表与详情能力，同时复用列表接口承载个性化推荐结果输出。
- 管理端可维护竞赛状态与基础信息，系统定时任务可按时间自动推进竞赛状态。
- 竞赛详情访问与报名动作可沉淀行为数据，形成推荐输入闭环。

### 7.3 队伍协作模块

- 队伍模块覆盖查询、详情、成员、讨论、提交等协作场景，形成参赛过程中的组织中枢。
- 系统区分“关闭招募”与“解散队伍”两类状态，确保流程控制与历史数据可追溯。
- 成员移除、讨论、文件上传等操作均受角色与队伍状态联合约束。

### 7.4 报名与审核模块

- 学生报名以申请单为中心，教师审核后驱动成员入队，构成从“申请”到“成队”的核心链路。
- 模块内置唯一性与状态一致性约束，避免同竞赛重复有效申请。
- 报名成功触发行为日志写入，为推荐模块提供显式偏好信号。

### 7.5 教师申请模块

- 教师申请模块支持教师提交带队申请、管理员审核、自动生成教师队伍。
- 申请中的技能方向可同步到队伍技能画像，作为后续组队匹配依据。
- 该模块将平台治理流程与队伍生成流程耦合，强化竞赛组织规范性。

### 7.6 奖项与荣誉模块

- 管理端发布奖项时按当前成员生成获奖快照，避免成员变动导致历史荣誉漂移。
- 学生端荣誉页通过快照表统计参与与获奖数据，保证结果展示稳定。
- 奖项发布受竞赛状态与队伍状态双重约束，确保发布时机符合业务规则。

### 7.7 推荐服务模块

- 推荐模块采用“内容推荐 + 协同过滤 + 回退机制”的组合策略，并嵌入竞赛与队伍业务接口。
- 内容推荐基于用户技能与目标技能向量匹配，协同过滤基于行为偏好聚合评分，最终按权重融合排序。
- 在冷启动或行为不足场景下自动回退，保证推荐接口可用性与结果稳定性。
- 整体形成“竞赛管理 + 队伍协作 + 推荐服务”一体化业务链路。

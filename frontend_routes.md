## 0. 约定与说明

### 0.1 角色枚举
- public：未登录
- student：学生
- teacher：教师
- admin：管理员
- authed：任意已登录用户（student/teacher/admin）

### 0.2 路由守卫约定
- requireAuth=true：
  - 无 token → 强制跳转 /login
- 已登录访问 /login：
  - 自动跳转 /dashboard
- 页面刷新：
  - 若存在 token，自动调用 /api/users/me 初始化用户状态
- 401：
  - axios 拦截，清 token，跳转 /login

### 0.3 阶段归属
- 每条路由标注所属阶段（F0–F9），用于按计划推进逐步落地页面

---

## 1. 公共入口路由（Public）

| Route Name          | Path               | requireAuth | Roles  | Phase | 页面/用途      | 备注                                   |
| ------------------- | ------------------ | ----------: | ------ | ----- | -------------- | -------------------------------------- |
| Login               | `/login`           |       false | public | F1    | 登录页         | 登录成功后按 role 跳转到对应 Dashboard |
| Register (optional) | `/register`        |       false | public | F1    | 注册页（可选） | 若后端未提供注册则保留占位或移除       |
| NotFound            | `/:pathMatch(.*)*` |       false | public | F0    | 404            | 统一兜底                               |

---

## 2. 应用壳与通用路由（Authed）

| Route Name                  | Path                 | requireAuth | Roles   | Phase | 页面/用途          | 备注                                                         |
| --------------------------- | -------------------- | ----------: | ------- | ----- | ------------------ | ------------------------------------------------------------ |
| Root                        | `/`                  |        true | authed  | F0    | Layout 容器        | 进入后重定向至 /dashboard                                    |
| DashboardRedirect           | `/dashboard`         |        true | authed  | F1    | 角色首页跳转       | 根据 role 重定向至子路由                                     |
| StudentDashboard            | `/dashboard/student` |        true | student | F1/F2 | 学生首页占位页     | F1：完成登录后按角色跳转与鉴权验证；<br/>F2：完成 Notion 风格布局骨架（cp-page / cp-grid / cp-card），页面为结构占位。 |
| TeacherDashboard            | `/dashboard/teacher` |        true | teacher | F2    | 教师首页占位页     | 与 StudentDashboard 共用布局骨架；页面内容后续补齐           |
| AdminDashboard              | `/dashboard/admin`   |        true | admin   | F2    | 管理员首页占位页   | 与 StudentDashboard 共用布局骨架；页面内容后续补齐           |
| Profile                     | `/me/profile`        |        true | authed  | F2    | 个人信息           | 页面骨架完成（cp-page / cp-card）；业务字段与接口在后续阶段接入 |
| MySkills                    | `/me/skills`         |        true | student | F2    | 我的技能画像       | 页面结构已规划；功能实现对齐后端 M1，在后续 F2 子阶段完成    |
| AdminSkillManage (optional) | `/admin/skills`      |        true | admin   | F2    | 技能库管理（可选） | 是否实现取决于后端能力；当前仅作为路由与菜单占位             |

> 当前 student/teacher/admin 子路由已建；仅 student 已完成运行态验证，teacher/admin 为占位页。
>
> F2 阶段
>
> - “完成”定义为：**路由存在 + Layout 正常 + 页面骨架与样式基础成立**，不等同于“业务功能完成”
> - F2 阶段说明：
>   - F2 聚焦“前端页面结构与样式基础”，不追求业务功能完整
>   - 所有 F2 页面均统一采用 Notion 风格布局骨架（cp-page / cp-grid / cp-card）
>   - 具体字段展示、接口联调与交互逻辑在后续对应功能阶段补齐

---

## 3. 竞赛相关（Authed）

### 3.1 竞赛列表/检索/详情（F3）
| Route Name        | Path                           | requireAuth | Roles  | Phase | 页面/用途            | 备注                                     |
| ----------------- | ------------------------------ | ----------: | ------ | ----- | -------------------- | ---------------------------------------- |
| CompetitionList   | `/competitions`                |        true | authed | F3    | 竞赛列表 + 检索/筛选 | 支持关键词、状态筛选、分页（若后端支持） |
| CompetitionDetail | `/competitions/:competitionId` |        true | authed | F3    | 竞赛详情             | 展示详情、报名入口、教师组入口等         |

### 3.2 推荐展示（F9）
> 推荐可做成独立页，或作为 competitions 页的一个 tab/开关。这里给独立页，方便作为亮点展示。

| Route Name           | Path                            | requireAuth | Roles   | Phase | 页面/用途  | 备注                                                |
| -------------------- | ------------------------------- | ----------: | ------- | ----- | ---------- | --------------------------------------------------- |
| CompetitionRecommend | `/recommendations/competitions` |        true | student | F9    | 竞赛推荐页 | 与检索互斥规则在页面层处理（有条件时提示/关闭推荐） |

---

## 4. 教师申请与审核（对齐后端 M3）

### 4.1 教师端：提交申请（F4）
| Route Name         | Path                                         | requireAuth | Roles   | Phase | 页面/用途        | 备注                                      |
| ------------------ | -------------------------------------------- | ----------: | ------- | ----- | ---------------- | ----------------------------------------- |
| TeacherApplyList   | `/teacher/applications`                      |        true | teacher | F4    | 我的教师申请列表 | 展示申请状态（PENDING/APPROVED/REJECTED）；侧边栏入口已完成 | | Sidebar entry available via active layout sidebar (teacher only).
| TeacherApplyCreate | `/competitions/:competitionId/teacher-apply` |        true | teacher | F4    | 提交教师申请     | 从竞赛详情进入更自然                      |

### 4.2 管理员端：审核教师申请（F4）
| Route Name               | Path                          | requireAuth | Roles | Phase | 页面/用途    | 备注                 |
| ------------------------ | ----------------------------- | ----------: | ----- | ----- | ------------ | -------------------- |
| AdminTeacherApplications | `/admin/teacher-applications` |        true | admin | F4    | 教师申请审核 | 列表 + 审核通过/拒绝；侧边栏入口已完成 | | Sidebar entry available via active layout sidebar (admin only).

---

## 5. 学生报名申请与教师审核入队（对齐后端 M4）

### 5.1 学生端（F5）
| Route Name           | Path                     | requireAuth | Roles   | Phase | 页面/用途        | 备注                          |
| -------------------- | ------------------------ | ----------: | ------- | ----- | ---------------- | ----------------------------- |
| StudentJoinRequests  | `/student/join-requests` |        true | student | F5    | 我的报名申请     | 查看申请状态与结果            |
| StudentTeamHome      | `/my/team`               |        true | student | F5/F6 | 我的组（学生）   | 若未入队显示空态与指引        |
| StudentApplyJoinTeam | `/teams/:teamId/join`    |        true | student | F5    | 申请加入某教师组 | 通常从竞赛详情/教师组列表进入 |

### 5.2 教师端（F5）
| Route Name           | Path                     | requireAuth | Roles   | Phase | 页面/用途      | 备注               |
| -------------------- | ------------------------ | ----------: | ------- | ----- | -------------- | ------------------ |
| TeacherTeamHome      | `/teacher/my-team`       |        true | teacher | F5/F6 | 我的组（教师） | 展示队伍信息与成员 |
| TeacherJoinApprovals | `/teacher/join-requests` |        true | teacher | F5    | 入队申请审核   | 列表 + 通过/拒绝   |

---

## 6. 组队控制与成员可见性（对齐后端 M5）

| Route Name            | Path                     | requireAuth | Roles         | Phase | 页面/用途 | 备注                                           |
| --------------------- | ------------------------ | ----------: | ------------- | ----- | --------- | ---------------------------------------------- |
| TeamDetail            | `/teams/:teamId`         |        true | authed        | F6    | 队伍详情  | 后端决定可见范围；前端按 403 提示              |
| TeamMembers           | `/teams/:teamId/members` |        true | authed        | F6    | 成员列表  | 权限由后端裁决；教师/管理员可见更多操作        |
| TeamManage (optional) | `/teams/:teamId/manage`  |        true | teacher,admin | F6    | 队伍管理  | 关闭组队、移除成员等入口也可放在 TeamDetail 内 |

---

## 7. 组内讨论区（对齐后端 M6）

| Route Name     | Path                           | requireAuth | Roles  | Phase | 页面/用途     | 备注                         |
| -------------- | ------------------------------ | ----------: | ------ | ----- | ------------- | ---------------------------- |
| TeamPosts      | `/teams/:teamId/posts`         |        true | authed | F7    | 讨论区列表    | 发帖、分页（可选）           |
| TeamPostDetail | `/teams/:teamId/posts/:postId` |        true | authed | F7    | 帖子详情/回复 | 回复、删除（权限由后端裁决） |

---

## 8. 文件提交（对齐后端 M7）

| Route Name      | Path                         | requireAuth | Roles  | Phase | 页面/用途      | 备注                                  |
| --------------- | ---------------------------- | ----------: | ------ | ----- | -------------- | ------------------------------------- |
| TeamSubmissions | `/teams/:teamId/submissions` |        true | authed | F8    | 文件提交与历史 | 上传 + 备注 + 历史列表 + current 标识 |

---

## 9. 管理员辅助入口（可选）

| Route Name                   | Path                  | requireAuth | Roles | Phase | 页面/用途    | 备注                                           |
| ---------------------------- | --------------------- | ----------: | ----- | ----- | ------------ | ---------------------------------------------- |
| AdminTeams (optional)        | `/admin/teams`        |        true | admin | F6    | 队伍总览     | 若后端无此接口可不实现                         |
| AdminCompetitions (optional) | `/admin/competitions` |        true | admin | F3    | 竞赛管理入口 | 若后端竞赛管理完全在后端/DB 侧，也可只保留查看 |

---

## 10. 导航菜单建议（非强制，仅作为锚点）

### Student 菜单
- 竞赛列表 `/competitions`
- 推荐竞赛 `/recommendations/competitions`
- 我的申请 `/student/join-requests`
- 我的组 `/my/team`
- 个人中心 `/me/profile`
- 我的技能 `/me/skills`

### Teacher 菜单
- 竞赛列表 `/competitions`
- 我的教师申请 `/teacher/applications`
- 入队审核 `/teacher/join-requests`
- 我的组 `/teacher/my-team`
- 个人中心 `/me/profile`

### Admin 菜单
- 竞赛列表 `/competitions`
- 教师申请审核 `/admin/teacher-applications`
- （可选）技能库管理 `/admin/skills`

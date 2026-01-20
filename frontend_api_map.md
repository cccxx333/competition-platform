> 定位：前端页�?模块 -> 后端 API 契约映射（契约清单，不是 Swagger）�?
> 目的�?
> 1) 防止前端“编接口/编字段�?
> 2) 快速判断“哪些接口已存在/哪些需补�?
> 3) �?Codex 提供强约束输入：只允许使用本表列出的接口与字段来�?

---

## 0. 规则与填表规范（强制�?

### 0.1 字段与路径来源（强制�?
- API 路径、方法、请求体/响应字段必须来自后端 Controller/DTO（Single Source of Truth）�?
- 本文件可先使�?`TBD` 占位，但在编码前必须补齐为真实路径与字段�?
- 前端不得自造字段：未知字段用可选类�?+ 空态处理，不得猜测�?

### 0.2 认证与鉴权（强制�?
- �?public 接口外，默认都需�?`Authorization: Bearer <token>`.
- 权限以服务端为准：前端仅做“显�?隐藏按钮 + 路由守卫”的最小控制�?

### 0.3 状态码统一处理（建议约定）
- 200/201：成�?
- 400：参数错误（前端提示 message�?
- 401：会话失效（�?token，跳登录�?
- 403：无权限（提示无权限�?
- 404：资源不存在（提示不存在�?
- 409：业务冲突（提示冲突原因，例如“已申请/截止/已关闭组队”）
- 5xx：系统异常（提示稍后重试�?

### 0.4 参数来源约定
- Route Param：来自前端路由参数（�?`:competitionId`, `:teamId`�?
- Query Param：来自列表筛�?分页控件
- Body：来自表单输�?
- Header：token 注入

### 0.5 “存在性”标�?
- �?已存在：已在后端 Controller 确认
- �?待确认：可能存在，需要从 Controller 检索核�?
- �?待补：后端不存在，需要补最小接口（不改 db_schema�?

---

## 1. Auth / Session（F1�?

| Page/Module       | API              | Method | Purpose                     | Params (source)                      | Auth | Exists | Notes                                                |
| ----------------- | ---------------- | ------ | --------------------------- | ------------------------------------ | ---- | ------ | ---------------------------------------------------- |
| Login             | /api/users/login | POST   | 登录获取 token �?role      | Body: { username, password }（表单） | No   | �?     | 已验证；返回 { token }，role 通过 /api/users/me 获取 |
| Fetch Me          | /api/users/me    | GET    | 获取当前用户信息（含 role�?| Header: Authorization Bearer <token> | Yes  | �?     | 登录后与刷新时调�?                                  |
| Logout (optional) | �?               | �?     | 退出登录（前端�?token�?   | LocalStorage / Store                 | �?   | �?     | 后端无登出接口，前端本地处理                         |

---

## 2. Profile & Skills（F2，对�?M1�?

### 2.1 个人信息
| Page/Module | API           | Method    | Purpose      | Params (source)              | Auth | Exists | Notes                                            |
| ----------- | ------------- | --------- | ------------ | ---------------------------- | ---- | ------ | ------------------------------------------------ |
| Profile     | /api/users/me | GET       | 查看个人信息 | Header: token                | Yes  | �?     | 后端已确认存在；前端页面骨架阶段，业务字段待接入 |
| Profile     | TBD           | PUT/PATCH | 更新个人信息 | Body: profile fields（表单） | Yes  | �?     | 字段�?DTO 为准                                  |

### 2.2 技能画�?
| Page/Module             | API                            | Method | Purpose                | Params (source)              | Auth | Exists | Notes                                          |
| ----------------------- | ------------------------------ | ------ | ---------------------- | ---------------------------- | ---- | ------ | ---------------------------------------------- |
| MySkills                | /api/users/me/skills           | GET    | 查询我的技能列�?      | Header: token                | Yes  | �?     | 后端已确认存在（M1）；前端页面结构阶段         |
| MySkills                | /api/users/me/skills           | POST   | 绑定技�?              | Body: {skillId}（表�?选择�?| Yes  | �?     | postman 已验�?200 / 409 冲突                  |
| MySkills                | /api/users/me/skills/{skillId} | DELETE | 解绑技�?              | Route: skillId（选择�?      | Yes  | �?     | postman 已验�?                                |
| SkillCatalog (optional) | TBD                            | GET    | 技能库列表（用于选择�?| Query: keyword?              | Yes  | �?     | 若无该接口可用初始化 skills 表直接分页查询接�?|

---

## 3. Competitions（F3，对�?M2�?

### 3.1 竞赛列表/检�?
| Page/Module                       | API  | Method | Purpose               | Params (source)                   | Auth | Exists | Notes                            |
| --------------------------------- | ---- | ------ | --------------------- | --------------------------------- | ---- | ------ | -------------------------------- |
| CompetitionList                   | /api/competitions       | GET    | 竞赛列表（分�?筛选） | Query: keyword?, status?, page, size, sortBy?, sortDir?, recommend?, topK? | Yes  | �?     | 后端�?Page 结构（content/totalElements/number/size），前端按此解包；client baseURL 默认 /api；列表列：name/status/date/organizer（status �?Tag，日期格式化）；支持 URL query 初始�?同步（keyword/status/page/size），地址栏回车无需刷新 |
| CompetitionDetail                 | /api/competitions/{id}  | GET    | 竞赛详情              | Route Param: id                                                  | Yes  | �?     | 详情分组展示：基�?时间/规则/描述；status �?Tag；日期格式化 |
| CompetitionTeams (for join/apply) | TBD  | GET    | 竞赛下教师组列表      | Route Param: competitionId        | Yes  | �?     | 若后端未提供可改�?team 搜索接口 |

---

## 4. Teacher Application & Admin Review

> Note: F4 sidebar incident was UI layout/sidebar path issue; API mapping unchanged.
（F4，对�?M3�?

### 4.1 教师端：提交/查看申请
| Page/Module                   | API  | Method | Purpose                  | Params (source)                  | Auth | Exists | Notes                                      |
| ----------------------------- | ---- | ------ | ------------------------ | -------------------------------- | ---- | ------ | ------------------------------------------ |
| TeacherApplyCreate            | /api/teacher-applications | POST   | �ύ��ʦ���루��ĳ������ | Body: { competitionId, teamName?, description?, skills? } | Yes  | YES    | Rules: UPCOMING + before registrationDeadline; PENDING/APPROVED -> 409; REJECTED resets to PENDING |
| TeacherApplyList              | /api/teacher-applications | GET    | �ҵĽ�ʦ�����б�         | Query: status?, page?, size?     | Yes  | YES    |                                            |
| TeacherApplyDetail (optional) | TBD  | GET    | 单条申请详情             | Route: applicationId             | Yes  | �?     | 可不�?                                    |

### 4.2 管理员端：审核教师申�?
| Page/Module              | API  | Method     | Purpose          | Params (source)                                        | Auth | Exists | Notes                             |
| ------------------------ | ---- | ---------- | ---------------- | ------------------------------------------------------ | ---- | ------ | --------------------------------- |
| AdminTeacherApplications | /api/admin/teacher-applications | GET        | ��ʦ��������б� | Query: status?, keyword?, page?, size?                         | Yes  | YES    |                                   |
| AdminTeacherApplications | /api/admin/teacher-applications/{id}/review | PUT | ���ͨ��/�ܾ�    | Route: id; Body: { approved, reviewComment? } | Yes  | YES    | ���ͨ��������� team����˴����� |

---

## 5. Student Join Application & Teacher Approval（F5，对�?M4�?

### 5.1 学生端：申请加入教师�?查看申请
| Page/Module          | API  | Method | Purpose                     | Params (source)                 | Auth | Exists | Notes                                  |
| -------------------- | ---- | ------ | --------------------------- | ------------------------------- | ---- | ------ | -------------------------------------- |
| StudentApplyJoinTeam | TBD  | POST   | 申请加入�?team             | Route: teamId; Body: {message?} | Yes  | �?�?   | 冲突�?409 表达：已申请/截止�?        |
| StudentJoinRequests  | TBD  | GET    | 我的报名申请列表            | Query: status?, competitionId?  | Yes  | �?     |                                        |
| MyTeam (student)     | TBD  | GET    | 查询我当前所�?team（如有） | Header: token                   | Yes  | �?     | 若无专用接口，可通过“我的成员关系”查�?|

### 5.2 教师端：审核学生申请
| Page/Module          | API  | Method     | Purpose                | Params (source)                            | Auth | Exists | Notes |
| -------------------- | ---- | ---------- | ---------------------- | ------------------------------------------ | ---- | ------ | ----- |
| TeacherJoinApprovals | TBD  | GET        | 待我审核的学生申请列�?| Query: status?, teamId?                    | Yes  | �?�?   |       |
| TeacherJoinApprovals | TBD  | POST/PATCH | 通过/拒绝学生申请      | Route: requestId; Body: {approve, reason?} | Yes  | �?�?   |       |

---

## 6. Team Control & Visibility（F6，对�?M5�?

| Page/Module      | API  | Method      | Purpose                | Params (source)         | Auth | Exists | Notes                |
| ---------------- | ---- | ----------- | ---------------------- | ----------------------- | ---- | ------ | -------------------- |
| TeamDetail       | TBD  | GET         | team 详情              | Route: teamId           | Yes  | �?�?   |                      |
| TeamMembers      | TBD  | GET         | 成员列表（受权限约束�?| Route: teamId           | Yes  | �?�?   | 403 由后端控�?      |
| TeamCloseRecruit | TBD  | POST/PATCH  | 关闭组队/停止招募      | Route: teamId           | Yes  | �?�?   | 后端 M5 若已实现�?�?|
| TeamRemoveMember | TBD  | DELETE/POST | 移除成员               | Route: teamId, memberId | Yes  | �?�?   | 后端裁决是否可移�?  |

---

## 7. Team Discussion（F7，对�?M6�?

| Page/Module                | API  | Method | Purpose            | Params (source)                        | Auth | Exists | Notes                            |
| -------------------------- | ---- | ------ | ------------------ | -------------------------------------- | ---- | ------ | -------------------------------- |
| TeamPosts                  | TBD  | GET    | 讨论区帖子列�?    | Route: teamId; Query: page,size        | Yes  | �?�?   |                                  |
| TeamPosts                  | TBD  | POST   | 发帖               | Route: teamId; Body: {content, title?} | Yes  | �?�?   |                                  |
| TeamPostDetail             | TBD  | GET    | 帖子详情（含回复�?| Route: teamId, postId                  | Yes  | �?     | 或分�?detail + replies 两个接口 |
| TeamReplyCreate            | TBD  | POST   | 回复帖子           | Route: teamId, postId; Body: {content} | Yes  | �?�?   |                                  |
| TeamPostDelete             | TBD  | DELETE | 删除帖子（软删）   | Route: teamId, postId                  | Yes  | �?�?   |                                  |
| TeamReplyDelete (optional) | TBD  | DELETE | 删除回复（软删）   | Route: teamId, replyId                 | Yes  | �?     | 若后端未提供可不�?              |

---

## 8. Submissions / File Upload（F8，对�?M7�?

| Page/Module                 | API  | Method | Purpose                         | Params (source)                    | Auth  | Exists | Notes                             |
| --------------------------- | ---- | ------ | ------------------------------- | ---------------------------------- | ----- | ------ | --------------------------------- |
| TeamSubmissions             | TBD  | GET    | 提交历史列表（含 current 标识�?| Route: teamId; Query: page,size?   | Yes   | �?�?   |                                   |
| TeamSubmitUpload            | TBD  | POST   | 上传新版本（multipart�?        | Route: teamId; Body: file + remark | Yes   | �?�?   | multipart/form-data               |
| FileAccess                  | TBD  | GET    | 访问/下载文件                   | Param: fileKey/path                | Maybe | �?�?   | 若是公开下载 URL，前端直接用 href |
| SubmissionDetail (optional) | TBD  | GET    | 单条提交详情                    | Route: submissionId                | Yes   | �?     | 可不�?                           |

---

## 9. Recommendations（F9，对�?M8�?

| Page/Module                 | API  | Method | Purpose                     | Params (source)              | Auth | Exists | Notes                                    |
| --------------------------- | ---- | ------ | --------------------------- | ---------------------------- | ---- | ------ | ---------------------------------------- |
| CompetitionRecommend        | TBD  | GET    | 获取推荐竞赛列表            | Query: topK? recommend=true? | Yes  | �?�?   | 与检索互斥规则在页面层处�?              |
| RecommendExplain (optional) | TBD  | GET    | 推荐理由/解释（如单独接口�?| Query/Route                  | Yes  | �?     | 若后端已在列表返�?reason/score 则不需�?|

---

## 10. 待补接口清单（从上表自动摘录，手工维护）

> 用于快速判断是否需要改后端。把 Exists=�?的条目集中列在这里�?

- TBD

---

## 11. Controller 对照索引（填充来源）

> 编码前，把每个模块实际对应的 Controller 类名与路径前缀写在这里，便�?Codex 快速定位�?

- AuthController: TBD
- UserController: TBD
- SkillController: TBD
- CompetitionController: TBD
- TeamController: TBD
- TeacherApplicationController: TBD
- Application/JoinRequestController: TBD
- Post/DiscussionController: TBD
- Submission/FileController: TBD
- RecommendationController: TBD





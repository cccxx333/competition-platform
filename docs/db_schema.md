> 本文件为数据库结构说明文档，已按 SQL 建表文件完整对齐。
> 
> 说明：
> - 本文档覆盖 SQL 中的全部数据表、唯一约束、索引与关键外键说明
> - 为提升可读性，统一采用“字段名 / 类型 / 约束 / 中文说明”的表格形式
> - 其中 FK 表示外键；PK 表示主键；AUTO 表示自增

---

## 1. users（用户表）

用于存储系统用户信息，包括管理员、教师和学生。

| 字段名 | 类型 | 约束 | 中文说明 |
|---|---|---|---|
| id | BIGINT | PK, AUTO | 用户唯一标识 |
| account_no | VARCHAR(32) | NOT NULL, UNIQUE | 登录账号，通常为学号或工号 |
| role | VARCHAR(20) | NOT NULL | 用户角色：ADMIN / TEACHER / STUDENT |
| password | VARCHAR(255) | NOT NULL | 加密后的登录密码 |
| real_name | VARCHAR(64) | - | 真实姓名 |
| username | VARCHAR(64) | - | 昵称或展示名 |
| email | VARCHAR(128) | UNIQUE | 邮箱地址 |
| phone | VARCHAR(32) | UNIQUE | 手机号 |
| avatar_url | VARCHAR(255) | - | 头像地址 |
| school | VARCHAR(128) | - | 学院或学校名称 |
| major | VARCHAR(128) | - | 专业 |
| grade | VARCHAR(32) | - | 年级 |
| created_at | DATETIME | - | 创建时间 |
| updated_at | DATETIME | - | 更新时间 |

**唯一约束**
- `uk_users_account_no (account_no)`
- `uk_users_email (email)`
- `uk_users_phone (phone)`

---

## 2. skills（技能表）

用于定义系统中的技能标签及其分类信息。

| 字段名 | 类型 | 约束 | 中文说明 |
|---|---|---|---|
| id | BIGINT | PK, AUTO | 技能唯一标识 |
| name | VARCHAR(64) | NOT NULL, UNIQUE | 技能名称 |
| category | VARCHAR(64) | - | 技能分类 |
| description | TEXT | - | 技能描述 |
| is_active | BIT(1) | DEFAULT b'1' | 是否启用，1表示启用 |
| created_at | DATETIME | - | 创建时间 |
| updated_at | DATETIME | - | 更新时间 |

**唯一约束**
- `uk_skills_name (name)`

---

## 3. user_skills（用户技能表）

用于建立用户与技能之间的关联关系，可记录熟练度。

| 字段名 | 类型 | 约束 | 中文说明 |
|---|---|---|---|
| id | BIGINT | PK, AUTO | 记录唯一标识 |
| user_id | BIGINT | NOT NULL, FK(users.id) | 用户ID |
| skill_id | BIGINT | NOT NULL, FK(skills.id) | 技能ID |
| level | INT | - | 技能熟练度 |
| created_at | DATETIME | - | 创建时间 |

**外键**
- `fk_user_skills_user: user_id -> users(id)`
- `fk_user_skills_skill: skill_id -> skills(id)`

---

## 4. competitions（竞赛表）

用于存储竞赛的基础信息、时间范围、队伍人数限制及状态等内容。

| 字段名 | 类型 | 约束 | 中文说明 |
|---|---|---|---|
| id | BIGINT | PK, AUTO | 竞赛唯一标识 |
| name | VARCHAR(128) | NOT NULL | 竞赛名称 |
| description | TEXT | - | 竞赛描述 |
| organizer | VARCHAR(128) | - | 主办方 |
| start_date | DATE | - | 开始日期 |
| end_date | DATE | - | 结束日期 |
| registration_deadline | DATE | - | 报名截止日期 |
| min_team_size | INT | DEFAULT 1 | 队伍最小人数 |
| max_team_size | INT | NOT NULL | 队伍最大人数 |
| category | VARCHAR(64) | - | 竞赛类别 |
| level | VARCHAR(64) | - | 竞赛级别 |
| status | VARCHAR(20) | DEFAULT 'UPCOMING' | 竞赛状态 |
| created_by | BIGINT | FK(users.id) | 创建人ID |
| created_at | DATETIME | - | 创建时间 |
| updated_at | DATETIME | - | 更新时间 |

**外键**
- `fk_competitions_created_by: created_by -> users(id)`

**索引**
- `idx_competitions_status_deadline (status, registration_deadline)`

---

## 5. competition_skills（竞赛技能需求表）

用于记录某项竞赛所需技能及其重要程度。

| 字段名 | 类型 | 约束 | 中文说明 |
|---|---|---|---|
| id | BIGINT | PK, AUTO | 记录唯一标识 |
| competition_id | BIGINT | NOT NULL, FK(competitions.id) | 竞赛ID |
| skill_id | BIGINT | NOT NULL, FK(skills.id) | 技能ID |
| importance | INT | DEFAULT 1 | 技能重要程度或权重 |

**外键**
- `fk_competition_skills_competition: competition_id -> competitions(id)`
- `fk_competition_skills_skill: skill_id -> skills(id)`

---

## 6. teams（队伍表）

用于存储教师创建的竞赛队伍信息。

| 字段名 | 类型 | 约束 | 中文说明 |
|---|---|---|---|
| id | BIGINT | PK, AUTO | 队伍唯一标识 |
| competition_id | BIGINT | NOT NULL, FK(competitions.id) | 所属竞赛ID |
| leader_id | BIGINT | NOT NULL, FK(users.id) | 队伍负责人ID，语义上通常为指导教师 |
| name | VARCHAR(128) | NOT NULL | 队伍名称 |
| description | TEXT | - | 队伍介绍或招募说明 |
| status | VARCHAR(20) | DEFAULT 'RECRUITING' | 队伍状态 |
| closed_at | DATETIME | - | 关闭招募时间 |
| closed_by | BIGINT | FK(users.id) | 关闭操作人ID |
| created_at | DATETIME | - | 创建时间 |
| updated_at | DATETIME | - | 更新时间 |

**唯一约束**
- `uk_teams_competition_leader (competition_id, leader_id)`

**外键**
- `fk_teams_competition: competition_id -> competitions(id)`
- `fk_teams_leader: leader_id -> users(id)`
- `fk_teams_closed_by: closed_by -> users(id)`

---

## 7. teacher_applications（教师申请表）

用于记录教师申请带队参赛的审批流程信息。

| 字段名 | 类型 | 约束 | 中文说明 |
|---|---|---|---|
| id | BIGINT | PK, AUTO | 申请唯一标识 |
| competition_id | BIGINT | NOT NULL, FK(competitions.id) | 竞赛ID |
| teacher_id | BIGINT | NOT NULL, FK(users.id) | 教师ID |
| status | VARCHAR(20) | DEFAULT 'PENDING' | 审核状态 |
| applied_at | DATETIME | - | 申请时间 |
| reviewed_at | DATETIME | - | 审核时间 |
| reviewed_by | BIGINT | FK(users.id) | 审核人ID |
| review_comment | VARCHAR(255) | - | 审核备注或拒绝原因 |
| generated_team_id | BIGINT | FK(teams.id) | 审核通过后生成的队伍ID |

**唯一约束**
- `uk_teacher_applications_competition_teacher (competition_id, teacher_id)`

**外键**
- `fk_teacher_applications_competition: competition_id -> competitions(id)`
- `fk_teacher_applications_teacher: teacher_id -> users(id)`
- `fk_teacher_applications_reviewer: reviewed_by -> users(id)`
- `fk_teacher_applications_generated_team: generated_team_id -> teams(id)`

---

## 8. teacher_application_skills（教师申请技能方向表）

用于记录教师在申请带队时填写的技能方向及权重。

| 字段名 | 类型 | 约束 | 中文说明 |
|---|---|---|---|
| id | BIGINT | PK, AUTO | 记录唯一标识 |
| teacher_application_id | BIGINT | NOT NULL, FK(teacher_applications.id) | 教师申请ID |
| skill_id | BIGINT | NOT NULL, FK(skills.id) | 技能ID |
| weight | INT | DEFAULT 1 | 技能权重 |

**外键**
- `fk_teacher_application_skills_application: teacher_application_id -> teacher_applications(id)`
- `fk_teacher_application_skills_skill: skill_id -> skills(id)`

---

## 9. team_skills（队伍技能方向表）

用于记录某个队伍的技能需求方向及权重。

| 字段名 | 类型 | 约束 | 中文说明 |
|---|---|---|---|
| id | BIGINT | PK, AUTO | 记录唯一标识 |
| team_id | BIGINT | NOT NULL, FK(teams.id) | 队伍ID |
| skill_id | BIGINT | NOT NULL, FK(skills.id) | 技能ID |
| weight | INT | DEFAULT 1 | 技能权重 |

**外键**
- `fk_team_skills_team: team_id -> teams(id)`
- `fk_team_skills_skill: skill_id -> skills(id)`

---

## 10. applications（学生报名申请表）

用于记录学生申请加入某个队伍的审核流程及状态变化。

| 字段名 | 类型 | 约束 | 中文说明 |
|---|---|---|---|
| id | BIGINT | PK, AUTO | 申请唯一标识 |
| competition_id | BIGINT | NOT NULL, FK(competitions.id) | 竞赛ID |
| team_id | BIGINT | NOT NULL, FK(teams.id) | 申请加入的队伍ID |
| student_id | BIGINT | NOT NULL, FK(users.id) | 学生ID |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING' | 申请状态 |
| is_active | BIT(1) | NOT NULL, DEFAULT b'1' | 是否为当前有效申请 |
| applied_at | DATETIME | - | 申请时间 |
| reviewed_at | DATETIME | - | 审核时间 |
| reviewed_by | BIGINT | FK(users.id) | 审核人ID |
| removed_at | DATETIME | - | 移除时间 |
| removed_by | BIGINT | FK(users.id) | 移除人ID |
| reason | VARCHAR(255) | - | 拒绝或移除原因 |

**唯一约束**
- `uk_applications_student_competition_active (student_id, competition_id, is_active)`

**外键**
- `fk_applications_competition: competition_id -> competitions(id)`
- `fk_applications_team: team_id -> teams(id)`
- `fk_applications_student: student_id -> users(id)`
- `fk_applications_reviewer: reviewed_by -> users(id)`
- `fk_applications_remover: removed_by -> users(id)`

**索引**
- `idx_applications_team_status (team_id, status)`

---

## 11. team_members（队伍成员表）

用于记录学生加入队伍后的正式成员关系。

| 字段名 | 类型 | 约束 | 中文说明 |
|---|---|---|---|
| id | BIGINT | PK, AUTO | 记录唯一标识 |
| team_id | BIGINT | NOT NULL, FK(teams.id) | 队伍ID |
| user_id | BIGINT | NOT NULL, FK(users.id) | 成员用户ID |
| role | VARCHAR(20) | DEFAULT 'MEMBER' | 成员角色 |
| joined_at | DATETIME | - | 加入时间 |
| left_at | DATETIME | - | 离队时间 |

**外键**
- `fk_team_members_team: team_id -> teams(id)`
- `fk_team_members_user: user_id -> users(id)`

**索引**
- `idx_team_members_team_left_at (team_id, left_at)`

---

## 12. team_discussion_posts（组内讨论帖子表）

用于记录队伍内部讨论区的主帖和回复帖内容。

| 字段名 | 类型 | 约束 | 中文说明 |
|---|---|---|---|
| id | BIGINT | PK, AUTO | 帖子唯一标识 |
| team_id | BIGINT | NOT NULL, FK(teams.id) | 所属队伍ID |
| author_id | BIGINT | NOT NULL, FK(users.id) | 发帖人ID |
| parent_post_id | BIGINT | FK(team_discussion_posts.id) | 父帖子ID，主帖时为空 |
| content | TEXT | NOT NULL | 帖子内容 |
| created_at | DATETIME | - | 发布时间 |
| updated_at | DATETIME | - | 更新时间 |
| deleted_at | DATETIME | - | 软删除时间 |
| deleted_by | BIGINT | FK(users.id) | 删除操作人ID |

**外键**
- `fk_team_discussion_posts_team: team_id -> teams(id)`
- `fk_team_discussion_posts_author: author_id -> users(id)`
- `fk_team_discussion_posts_parent: parent_post_id -> team_discussion_posts(id)`
- `fk_team_discussion_posts_deleted_by: deleted_by -> users(id)`

---

## 13. team_submissions（作品提交表）

用于记录队伍提交的作品文件及其版本信息。

| 字段名 | 类型 | 约束 | 中文说明 |
|---|---|---|---|
| id | BIGINT | PK, AUTO | 提交记录ID |
| team_id | BIGINT | NOT NULL, FK(teams.id) | 所属队伍ID |
| competition_id | BIGINT | NOT NULL, FK(competitions.id) | 所属竞赛ID |
| submitted_by | BIGINT | NOT NULL, FK(users.id) | 提交人ID |
| file_name | VARCHAR(255) | NOT NULL | 原始文件名 |
| file_url | VARCHAR(512) | NOT NULL | 文件存储地址 |
| remark | VARCHAR(255) | - | 提交备注 |
| submitted_at | DATETIME | - | 提交时间 |
| is_current | BIT(1) | DEFAULT b'1' | 是否为当前有效版本 |

**外键**
- `fk_team_submissions_team: team_id -> teams(id)`
- `fk_team_submissions_competition: competition_id -> competitions(id)`
- `fk_team_submissions_submitter: submitted_by -> users(id)`

---

## 14. user_behaviors（用户行为表）

用于记录用户在系统中的行为数据，为推荐功能提供基础数据支持。

| 字段名 | 类型 | 约束 | 中文说明 |
|---|---|---|---|
| id | BIGINT | PK, AUTO | 行为记录ID |
| user_id | BIGINT | NOT NULL, FK(users.id) | 用户ID |
| behavior_type | VARCHAR(20) | NOT NULL | 行为类型 |
| target_type | VARCHAR(20) | NOT NULL | 行为目标类型 |
| target_id | BIGINT | NOT NULL | 行为目标ID |
| weight | INT | DEFAULT 1 | 行为权重 |
| created_at | DATETIME | - | 记录时间 |

**外键**
- `fk_user_behaviors_user: user_id -> users(id)`

**索引**
- `idx_user_behaviors_user_created_at (user_id, created_at)`

---

## 15. team_awards（获奖发布记录表）

用于记录管理员发布的竞赛奖项信息，是奖项主记录表。

| 字段名 | 类型 | 约束 | 中文说明 |
|---|---|---|---|
| id | BIGINT | PK, AUTO | 主键ID |
| competition_id | BIGINT | NOT NULL, FK(competitions.id) | 所属竞赛ID |
| team_id | BIGINT | NOT NULL, FK(teams.id) | 获奖队伍ID |
| award_name | VARCHAR(64) | NOT NULL | 奖项名称 |
| published_by | BIGINT | NOT NULL, FK(users.id) | 发布人ID |
| published_at | DATETIME | NOT NULL | 发布时间 |
| is_active | TINYINT | NOT NULL, DEFAULT 1 | 是否有效 |

**唯一约束**
- `uk_team_awards_competition_team_award (competition_id, team_id, award_name)`

**外键**
- `fk_team_awards_competition: competition_id -> competitions(id)`
- `fk_team_awards_team: team_id -> teams(id)`
- `fk_team_awards_published_by: published_by -> users(id)`

**索引**
- `idx_team_awards_team (team_id)`
- `idx_team_awards_comp (competition_id)`

---

## 16. award_recipients（获奖成员快照表）

用于冻结奖项发布时刻的获奖成员信息，避免后续成员变动影响历史记录。

| 字段名 | 类型 | 约束 | 中文说明 |
|---|---|---|---|
| id | BIGINT | PK, AUTO | 主键ID |
| team_award_id | BIGINT | NOT NULL, FK(team_awards.id) | 对应奖项记录ID |
| user_id | BIGINT | NOT NULL, FK(users.id) | 获奖用户ID |
| recorded_at | DATETIME | NOT NULL | 记录时间 |

**唯一约束**
- `uk_award_recipients_award_user (team_award_id, user_id)`

**外键**
- `fk_award_recipients_team_award: team_award_id -> team_awards(id) ON DELETE RESTRICT`
- `fk_award_recipients_user: user_id -> users(id)`

**索引**
- `idx_award_recipients_user (user_id)`

---

## 附：已实现高频索引汇总

SQL 中已实际创建的高频索引如下：

1. `idx_competitions_status_deadline`：`competitions(status, registration_deadline)`
2. `idx_applications_team_status`：`applications(team_id, status)`
3. `idx_team_members_team_left_at`：`team_members(team_id, left_at)`
4. `idx_user_behaviors_user_created_at`：`user_behaviors(user_id, created_at)`
5. `idx_team_awards_team`：`team_awards(team_id)`
6. `idx_team_awards_comp`：`team_awards(competition_id)`
7. `idx_award_recipients_user`：`award_recipients(user_id)`



| 字段名                 | 类型   | 中文说明     |
| ---------------------- | ------ | ------------ |
| id                     | BIGINT | 记录唯一标识 |
| teacher_application_id | BIGINT | 教师申请ID   |
| skill_id               | BIGINT | 技能ID       |
| weight                 | INT    | 技能权重     |

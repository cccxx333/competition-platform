# db_schema.md

> 本文件为数据库结构的**极简版 Schema 说明**（非建表语句）。用于让 Codex 快速定位表/字段，不重复叙述业务流程。

## users
- 说明：用户表

- `id` (BIGINT, PK, AUTO)：用户唯一标识
- `account_no` (VARCHAR(32), UNIQUE, NOT NULL)：登录账号（学号/工号）
- `role` (VARCHAR(20), NOT NULL)：角色：ADMIN / TEACHER / STUDENT
- `password` (VARCHAR(255), NOT NULL)：密码（加密存储）
- `real_name` (VARCHAR(64))：真实姓名
- `username` (VARCHAR(64))：昵称/展示名（可选）
- `email` (VARCHAR(128), UNIQUE)：邮箱（与 phone 至少一项由业务层校验）
- `phone` (VARCHAR(32), UNIQUE)：手机号（与 email 至少一项由业务层校验）
- `avatar_url` (VARCHAR(255))：头像URL
- `school` (VARCHAR(128))：学院/学校（学生/教师可用）
- `major` (VARCHAR(128))：专业（学生常用）
- `grade` (VARCHAR(32))：年级（学生常用）
- `created_at` (DATETIME)：创建时间
- `updated_at` (DATETIME)：更新时间

---

## skills
- 说明：技能表

- `id` (BIGINT, PK, AUTO)：技能ID
- `name` (VARCHAR(64), UNIQUE, NOT NULL)：技能名称
- `category` (VARCHAR(64))：技能分类
- `description` (TEXT)：技能描述
- `is_active` (BIT(1), DEFAULT 1)：是否启用（1启用/0停用）
- `created_at` (DATETIME)：创建时间
- `updated_at` (DATETIME)：更新时间

---

## user_skills
- 说明：用户技能表

- `id` (BIGINT, PK, AUTO)：记录ID
- `user_id` (BIGINT, FK(users.id))：用户ID
- `skill_id` (BIGINT, FK(skills.id))：技能ID
- `level` (INT)：熟练度（可选，自定义范围）
- `created_at` (DATETIME)：创建时间

---

## competitions
- 说明：竞赛表

- `id` (BIGINT, PK, AUTO)：竞赛ID
- `name` (VARCHAR(128), NOT NULL)：竞赛名称
- `description` (TEXT)：竞赛描述
- `organizer` (VARCHAR(128))：主办方
- `start_date` (DATE)：开始日期
- `end_date` (DATE)：结束日期
- `registration_deadline` (DATE)：报名截止日期（截止后禁止新申请，但允许审核已存在申请）
- `min_team_size` (INT, DEFAULT 1)：队伍最小人数
- `max_team_size` (INT, NOT NULL)：队伍最大人数
- `category` (VARCHAR(64))：竞赛类别
- `level` (VARCHAR(64))：竞赛级别
- `status` (VARCHAR(20), DEFAULT UPCOMING)：状态：UPCOMING / ONGOING / FINISHED
- `created_by` (BIGINT, FK(users.id))：创建人（管理员）
- `created_at` (DATETIME)：创建时间
- `updated_at` (DATETIME)：更新时间

---

## competition_skills
- 说明：竞赛技能需求表

- `id` (BIGINT, PK, AUTO)：记录ID
- `competition_id` (BIGINT, FK(competitions.id))：竞赛ID
- `skill_id` (BIGINT, FK(skills.id))：技能ID
- `importance` (INT, DEFAULT 1)：重要程度/权重（可选范围自定义）

---

## teacher_applications
- 说明：教师申请表

- `id` (BIGINT, PK, AUTO)：申请ID
- `competition_id` (BIGINT, FK(competitions.id))：竞赛ID
- `teacher_id` (BIGINT, FK(users.id))：教师ID
- `status` (VARCHAR(20), DEFAULT PENDING)：状态：PENDING / APPROVED / REJECTED
- `applied_at` (DATETIME)：申请时间
- `reviewed_at` (DATETIME)：审核时间
- `reviewed_by` (BIGINT, FK(users.id))：审核人（管理员）
- `review_comment` (VARCHAR(255))：审核备注/拒绝原因
- `generated_team_id` (BIGINT, FK(teams.id))：审核通过后生成的队伍ID（可空，用于追溯）

**备注/约束提示**
- 结构约束建议：同一竞赛同一教师仅允许一条申请（competition_id + teacher_id 唯一）。

---

## teacher_application_skills
- 说明：教师申请技能方向表

- `id` (BIGINT, PK, AUTO)：记录ID
- `teacher_application_id` (BIGINT, FK(teacher_applications.id))：教师申请ID
- `skill_id` (BIGINT, FK(skills.id))：技能ID
- `weight` (INT, DEFAULT 1)：权重/重要性（可选）

---

## teams
- 说明：队伍表 / 教师组

- `id` (BIGINT, PK, AUTO)：队伍ID
- `competition_id` (BIGINT, FK(competitions.id))：所属竞赛
- `leader_id` (BIGINT, FK(users.id))：队长（语义=指导教师）
- `name` (VARCHAR(128), NOT NULL)：队伍名称
- `description` (TEXT)：队伍介绍/招募说明
- `status` (VARCHAR(20), DEFAULT RECRUITING)：状态：RECRUITING / CLOSED / DISBANDED
- `closed_at` (DATETIME)：结束组队时间
- `closed_by` (BIGINT, FK(users.id))：结束组队操作者（教师/管理员）
- `created_at` (DATETIME)：创建时间
- `updated_at` (DATETIME)：更新时间

**备注/约束提示**
- 结构约束建议：同一竞赛下教师唯一（competition_id + leader_id 唯一）。

---

## team_skills
- 说明：队伍技能方向表

- `id` (BIGINT, PK, AUTO)：记录ID
- `team_id` (BIGINT, FK(teams.id))：队伍ID
- `skill_id` (BIGINT, FK(skills.id))：技能ID
- `weight` (INT, DEFAULT 1)：权重/重要性

---

## applications
- 说明：学生报名申请表

- `id` (BIGINT, PK, AUTO)：申请ID
- `competition_id` (BIGINT, FK(competitions.id))：竞赛ID
- `team_id` (BIGINT, FK(teams.id))：申请队伍
- `student_id` (BIGINT, FK(users.id))：学生ID
- `status` (VARCHAR(20), DEFAULT PENDING)：状态：PENDING / APPROVED / REJECTED / REMOVED
- `is_active` (TINYINT, DEFAULT 1)：是否当前有效（PENDING/APPROVED=1，REJECTED/REMOVED=0，由业务层维护）
- `applied_at` (DATETIME)：申请时间
- `reviewed_at` (DATETIME)：审核时间
- `reviewed_by` (BIGINT, FK(users.id))：审核人（通常为队伍教师，管理员可兜底）
- `removed_at` (DATETIME)：移除时间
- `removed_by` (BIGINT, FK(users.id))：移除人（教师/管理员）
- `reason` (VARCHAR(255))：拒绝/移除原因

**备注/约束提示**
- 结构约束建议：为实现排他，可对 (student_id + competition_id + is_active) 做唯一约束。

---

## team_members
- 说明：队伍成员表

- `id` (BIGINT, PK, AUTO)：记录ID
- `team_id` (BIGINT, FK(teams.id))：队伍ID
- `user_id` (BIGINT, FK(users.id))：成员ID（学生）
- `role` (VARCHAR(20), DEFAULT MEMBER)：成员角色（本项目可简化为 MEMBER）
- `joined_at` (DATETIME)：加入时间
- `left_at` (DATETIME)：离队时间（被移除/退出）

---

## team_discussion_posts
- 说明：组内讨论区帖子表

- `id` (BIGINT, PK, AUTO)：帖子ID
- `team_id` (BIGINT, FK(teams.id))：所属队伍
- `author_id` (BIGINT, FK(users.id))：发布者
- `parent_post_id` (BIGINT, FK(team_discussion_posts.id))：父帖子ID（单层回复；主帖为空）
- `content` (TEXT, NOT NULL)：内容（纯文本）
- `created_at` (DATETIME)：发布时间
- `updated_at` (DATETIME)：编辑时间（可选）
- `deleted_at` (DATETIME)：删除时间（软删除）
- `deleted_by` (BIGINT, FK(users.id))：删除人（发布者/教师/管理员）

---

## team_submissions
- 说明：作品文件提交表

- `id` (BIGINT, PK, AUTO)：提交记录ID
- `team_id` (BIGINT, FK(teams.id))：所属队伍
- `competition_id` (BIGINT, FK(competitions.id))：竞赛ID（冗余存储便于查询/校验）
- `submitted_by` (BIGINT, FK(users.id))：提交人（通常为组内学生）
- `file_name` (VARCHAR(255), NOT NULL)：原始文件名
- `file_url` (VARCHAR(512), NOT NULL)：文件存储地址（本地/对象存储URL）
- `remark` (VARCHAR(255))：提交备注
- `submitted_at` (DATETIME)：提交时间
- `is_current` (BIT(1), DEFAULT 1)：是否当前有效版本（最新=1，旧版本=0）

---

## user_behaviors
- 说明：用户行为表

- `id` (BIGINT, PK, AUTO)：行为ID
- `user_id` (BIGINT, FK(users.id))：用户ID
- `behavior_type` (VARCHAR(20), NOT NULL)：行为类型（VIEW / LIKE / FAVORITE / APPLY / JOIN 等）
- `target_type` (VARCHAR(20), NOT NULL)：目标类型（COMPETITION / TEAM / SKILL）
- `target_id` (BIGINT, NOT NULL)：目标ID
- `weight` (INT, DEFAULT 1)：行为权重（用于推荐）
- `created_at` (DATETIME)：行为时间

**备注/约束提示**
- 1. 同一竞赛同一教师仅一个教师组：`teams(competition_id, leader_id)` 唯一
- 2. 同一竞赛同一教师仅一条申请：`teacher_applications(competition_id, teacher_id)` 唯一
- 3. 同一学生同一竞赛同一时间仅一个有效申请/通过：`applications(student_id, competition_id, is_active)` 唯一（由业务层维护 is_active）
- 4. 建议为高频查询建立索引：`competitions(status, registration_deadline)`、`applications(team_id, status)`、`team_members(team_id, left_at)`、`user_behaviors(user_id, created_at)`

---

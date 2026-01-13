# DOC_INDEX.md  

# 项目文档索引与使用说明

项目名称：**基于混合推荐算法的学科竞赛平台设计与实现**  

本文件用于说明本项目中各类文档的**职责划分、使用场景与读取优先级**，  
是人类开发者与 AI（Codex）之间的**统一导航索引文件**。

---

## 一、文档总体分层说明

本项目文档分为四个层级（由“约束”到“事实”）：

1. **AI 行为与协作规范（How AI should work）**
2. **业务需求与流程规则（What the system should do）**
3. **数据库结构参考（How data is organized）**
4. **项目事实与进度记录（What has been done）**

AI 在任何分析或实现前，必须理解并遵守该分层结构。

---

## 二、AI 行为与协作规范（约束 Codex 怎么干活）

### 1. agent.md

- **定位**：AI 行为宪法（最高约束文件）
- **内容**：
  - AI 阅读顺序
  - 不可突破边界
  - 数据库冻结规则
  - 输出格式与偏移自检
- **作用**：防止需求扩张、过度设计、数据库结构被破坏
- **使用方式**：
  - 每次让 Codex 开始任何任务前，必须首先阅读

### 2. CODEX_COMMIT_GUIDE.md

- **定位**：提交协作规范（辅助）
- **内容**：
  - Codex 与 Git 的职责边界
  - commit message 命名规范
- **使用方式**：
  - 仅在需要生成 commit 建议时阅读
  - 非业务实现必读文件

### 3. commit-template.txt

- **定位**：提交模板（工具文件）
- **内容**：
  - commit message 第一行格式
- **使用方式**：
  - 提交阶段人工填写
  - AI 只需理解模板存在

---

## 三、业务需求与流程规则（项目做什么）

### 4. project_spec.md

- **定位**：项目**唯一业务需求与流程说明文件**
- **内容**：
  - 项目定位与边界
  - 系统角色与权限
  - 核心业务流程（状态级）
  - 关键业务规则与约束
  - 推荐算法（教学级）
- **作用**：
  - 取代原有 PROJECT_BRIEF / scope_requirements / user_flow
- **使用方式**：
  - 涉及任何业务功能实现时，必须阅读

> 说明：以下文件已被 project_spec.md 吸收，仅作为历史留档：  
>
> - PROJECT_BRIEF.md  
> - scope_requirements.md  
> - user_flow.md  

---

## 四、数据库结构参考（数据如何组织）

### 5. db_schema.md

- **定位**：数据库结构主参考文件（冻结）
- **内容**：
  - 表结构
  - 字段名称与一句话语义
- **作用**：
  - 快速辅助 Entity / Mapper / Service 编写
- **使用方式**：
  - 涉及数据库读写、关联、查询时必读
  - **不得作为业务规则来源**

### 6. 新数据库表结构_v3.md

- **定位**：数据库设计完整版留档（历史对照）
- **内容**：
  - 完整数据库设计说明
  - 设计阶段的一致性备注
- **使用方式**：
  - 仅在需要复核设计或对照时使用
  - 非主参考文件

---

## 五、项目事实与进度（做到哪一步）

### 7. PROJECT_CHANGELOG.md

- **定位**：项目状态与历史的唯一事实源（Single Source of Truth）
- **内容**：
  - 当前完成情况
  - 最近一次修改
  - 已知问题与待办事项
- **使用方式**：
  - AI 在修改代码前必须阅读
  - 每次 commit 后由人工更新（AI 可给草稿）

---

## 六、Codex 实际工作时的最小必读集合

### 业务功能开发（90% 情况）

AI 只需重点阅读以下四个文件：

1. agent.md  
2. project_spec.md  
3. db_schema.md  
4. PROJECT_CHANGELOG.md  

### 提交相关任务

在上述基础上，额外阅读：

5. CODEX_COMMIT_GUIDE.md  
6. commit-template.txt  

---

## 七、快速判断：我现在该看哪个文件？

- **想约束 Codex 行为** → agent.md  
- **想确认项目需求与规则** → project_spec.md  
- **想确认数据库字段与表结构** → db_schema.md  
- **想知道项目当前进度** → PROJECT_CHANGELOG.md  
- **想处理提交规范** → CODEX_COMMIT_GUIDE.md + commit-template.txt  

---

## 八、总原则

> **文档职责单一  
> 主次清晰  
> 规则集中  
> 事实唯一**

若多个文件内容出现冲突：  
以 agent.md → project_spec.md → db_schema.md → PROJECT_CHANGELOG.md 的优先级为准。
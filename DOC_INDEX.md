# 项目文档索引与使用说明（DOC_INDEX）

项目名称：**基于混合推荐算法的学科竞赛平台设计与实现**

本文件用于说明本项目中各类文档的**职责划分、使用场景与读取优先级**，  
是人类开发者与 AI（Codex）之间的**统一导航索引文件**。

---

## 一、文档总体分层说明

本项目文档按“约束 → 规则 → 契约 → 结构 → 事实”的逻辑分为五个层级：

1. **AI 行为与协作规范（How AI should work）**
2. **业务需求与系统规则（What the system should do）**
3. **前端开发治理与接口契约（How frontend interacts with backend）**
4. **数据库结构参考（How data is organized）**
5. **项目事实与推进记录（What has been done / planned）**

AI 在任何分析、设计或实现前，必须理解并遵守该分层结构。

---

## 二、AI 行为与协作规范（最高约束）

### 1. agent.md

- **定位**：AI 行为宪法（最高约束文件）
- **内容**：
  - AI 阅读顺序与权限边界
  - 不可突破的技术与项目边界
  - 数据库冻结规则
  - 前后端职责划分与前端治理规则
  - 输出格式与偏移自检
- **使用方式**：
  - 每次让 Codex 执行任何任务前，必须首先阅读

---

## 三、业务需求与系统规则（唯一业务来源）

### 2. project_spec.md

- **定位**：项目**唯一的业务需求、流程与规则说明文件**
- **内容**：
  - 项目定位与功能边界
  - 系统角色与权限模型
  - 核心业务流程与状态流转
  - 关键业务规则与约束
  - 推荐算法（教学级）
- **使用方式**：
  - 涉及任何业务功能实现或判断时，必须阅读

> 说明：以下文件已被 project_spec.md 吸收，仅作为历史留档：  
> PROJECT_BRIEF.md / scope_requirements.md / user_flow.md

---

## 四、前端开发治理与接口契约（前后端分离核心）

> 本层为前端阶段新增，是前端开发与 Codex 执行时的**权威输入**。

### 3.frontend_plan.md

- **定位**：前端开发阶段性框架（F0–F9）
- **内容**：
  - 前端推进阶段与验收口径
  - 页面风格调整的两个允许窗口
  - 前端开发的硬约束与禁止项
- **使用方式**：
  - 前端任何阶段性工作前必读

### 4.frontend_routes.md

- **定位**：前端页面路由骨架与角色权限锚点
- **内容**：
  - 页面访问路径（Vue Router）
  - requireAuth / roles
  - 所属前端阶段（F0–F9）
- **使用方式**：
  - 组织页面结构与导航
  - 不作为后端接口依据

### 5. frontend_api_map.md

- **定位**：前端页面 → 后端 API 的接口契约映射
- **内容**：
  - 页面/模块所需 API 列表
  - 参数来源与鉴权要求
  - 接口存在性标记（已存在 / 待确认 / 待补）
- **使用方式**：
  - 前端编码前必读
  - 用于判断是否需要补后端接口

---

## 五、数据库结构参考（冻结）

### 6. db_schema.md

- **定位**：数据库结构主参考文件（冻结）
- **内容**：
  - 表结构
  - 字段名称与语义说明
- **使用方式**：
  - Entity / Repository / Service 编写时参考
  - **不得作为业务规则来源**

### 7. schema_v3.md

- **定位**：数据库设计完整版历史留档
- **使用方式**：
  - 仅用于设计对照与复核
  - 非主参考文件

---

## 六、项目事实与推进记录（人类主导）

### 8. 前端 / 后端项目计划表

- **定位**：项目推进与复盘的**事实记录工具**
- **内容**：
  - 各模块目标
  - Codex 指令草稿
  - 每一步执行结果与风险记录
- **使用方式**：
  - 主要供人类开发者与 ChatGPT 使用
  - **不作为 Codex 的直接权威输入**

---

## 七、Codex 实际工作的推荐阅读集合

### 前端相关任务
1. agent.md  
2. project_spec.md  
3. docs/frontend/frontend_plan.md  
4. docs/frontend/frontend_api_map.md  
5. docs/frontend/frontend_routes.md  

### 后端相关任务
1. agent.md  
2. project_spec.md  
3. db_schema.md  

---

## 八、快速判断：我现在该看哪个文件？

- **想约束 Codex 行为** → agent.md  
- **想确认业务规则** → project_spec.md  
- **想推进前端阶段** → docs/frontend/frontend_plan.md  
- **想写页面但不确定接口** → frontend_api_map.md  
- **想确认页面路径与权限** → frontend_routes.md  
- **想回顾自己做了什么** → 项目计划表  

---

## 九、总原则

> **规则必须集中  
> 契约必须明确  
> 过程允许自由  
> 决策不留歧义**

若多个文件内容出现冲突：  
以 **agent.md → project_spec.md → docs/frontend/* → db_schema.md → 计划表** 的优先级为准。
---

## Active Layout & Sidebar (Important)

- Active frontend dir: `frontend/`
- Active Layout: `frontend/src/layouts/BasicLayout.vue`
- Active Sidebar: `frontend/src/layouts/BasicLayout.vue` (static `<el-menu>` inside layout, no separate Sidebar component)
- Active chain: router (`frontend/src/router/index.ts`) -> `BasicLayout` -> static menu
- Note: F4 sidebar issue was caused by editing an inactive Sidebar file; fixes must target the active layout file.
- Prevention checklist for UI changes not taking effect:
  1) Confirm runtime directory and dev command (e.g. `cd frontend && npm run dev`)
  2) Locate active component via router -> layout -> sidebar reference chain
  3) Use a temporary visual marker to confirm the active file, then remove it before commit


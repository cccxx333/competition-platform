# 学科竞赛管理平台

基于混合推荐算法的学科竞赛管理平台，旨在解决高校学生在参赛过程中"信息获取效率低、队伍匹配困难、协作过程分散"等问题。

## 技术栈

### 后端
- Spring Boot 2.7.14
- Spring Security
- Spring Data JPA
- MySQL 8.0
- JWT认证
- Maven

### 前端
- React 18
- Ant Design 5.x
- Axios
- React Router Dom

## 功能特性

1. **用户管理**
    - 用户注册/登录
    - 个人信息管理
    - 技能档案管理

2. **竞赛管理**
    - 竞赛信息发布
    - 竞赛搜索与筛选
    - 竞赛详情展示

3. **队伍管理**
    - 队伍创建与管理
    - 队伍成员招募
    - 队伍协作

4. **智能推荐**
    - 基于内容的推荐算法
    - 协同过滤推荐算法
    - 混合推荐策略

## 快速开始

### 环境要求
- JDK 8+
- Node.js 14+
- MySQL 8.0+
- Maven 3.6+

## 后端启动步骤：

### 创建数据库：
```sql 
CREATE DATABASE competition_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 修改 application.yml 中的数据库配置

### 启动 Spring Boot 应用：
```bash
mvn spring-boot:run
```
## 前端启动步骤：

### 安装依赖：
```bash
cd competition-frontend
npm install
```
### 启动开发服务器：
```bash
npm start
```

## 项目结构
```text
competition-platform/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/
│   │   └── com/competition/
│   │       ├── algorithm/      # 推荐算法
│   │       ├── controller/     # 控制器
│   │       ├── service/        # 服务层
│   │       ├── repository/     # 数据访问层
│   │       ├── entity/         # 实体类
│   │       ├── dto/            # 数据传输对象
│   │       ├── config/         # 配置类
│   │       └── utils/          # 工具类
│   └── src/main/resources/
├── frontend/                   # React 前端
│   ├── src/
│   │   ├── components/         # 组件
│   │   ├── pages/              # 页面
│   │   ├── services/           # API服务
│   │   └── utils/              # 工具函数
│   └── public/
└── docs/                       # 项目文档
```

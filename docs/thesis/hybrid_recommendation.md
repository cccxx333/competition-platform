# 混合推荐实现理解总结与代码示例

## 一、你现在已经理解到的内容

你可以先把“混合推荐”理解成一个业务编排流程，而不只是一个算法函数。

### 1. 混合推荐的整体流程

```text
前端请求竞赛列表（recommend=true）
        -> CompetitionController 接收参数并解析 userId
        -> CompetitionService 进行推荐开关与 fallback 判断
        -> RecommendationService 计算混合分
            -> ContentBasedAlgorithm 计算内容匹配分
            -> CollaborativeFilteringAlgorithm 计算协同过滤分
            -> alpha=0.8 线性融合
        -> CompetitionService 按分数排序、补充推荐理由
        -> Controller 返回 CompetitionResponse(JSON)
```

---

### 2. Controller 在混合推荐中做什么

- 接收 `recommend/topK/page/size/sort` 参数
- 在 `recommend=true` 时尝试解析登录用户 ID
- 调用 `competitionService.getCompetitions(...)`
- 返回分页结果给前端

对应代码位置：`CompetitionController.getCompetitions(...)`

---

### 3. CompetitionService 在混合推荐中做什么

- 统一推荐入口与默认列表入口
- 控制 `topK` 范围（默认 10，上限 50）
- 判断是否 fallback（未登录、无技能、空分数）
- 对候选竞赛做“推荐优先 + 默认排序兜底”
- 回填 `matchScore/recommend/recommendReason/fallbackReason`

对应代码位置：`CompetitionService.getCompetitions(...)`

---

### 4. RecommendationService 在混合推荐中做什么

- 判断推荐可用性：`getRecommendFallbackReason(...)`
- 先算内容分：`contentBasedAlgorithm.calculateCompetitionSimilarity(...)`
- 行为不足时直接 fallback 到内容分
- 行为足够时算 CF 分：`collaborativeFilteringAlgorithm.scoreCompetitionsForUser(...)`
- 用 `alpha=0.8` 混合输出最终分数

对应代码位置：`RecommendationService.calculateCompetitionHybridScores(...)`

---

### 5. ContentBased 与 Collaborative 的分工

- ContentBasedAlgorithm：回答“我会什么，竞赛要什么”
- CollaborativeFilteringAlgorithm：回答“和我相似的人还喜欢什么”
- RecommendationService：回答“这两种信号怎么融合并稳定输出”

---

### 6. 行为数据在混合推荐里的作用

- 浏览竞赛详情会写入 `VIEW`
- 学生提交报名会写入 `APPLY`
- `APPLY` 权重高于 `VIEW`，影响后续 CF 偏好强度
- 行为不足时系统自动退回内容推荐，不会中断主流程

对应代码位置：
- `CompetitionService.recordCompetitionViewSafely(...)`
- `ApplicationService.createApplication(...)`
- `UserBehaviorService.recordCompetitionView(...) / recordCompetitionApply(...)`

## 二、一个完整的建议流程代码示例

下面用“竞赛列表推荐”做一条完整示例，串起 Controller -> Service -> Algorithm。

### 1）Controller 入口（参数接收 + userId 解析）

```java
@GetMapping
public ResponseEntity<Page<CompetitionResponse>> getCompetitions(
        HttpServletRequest request,
        @RequestParam(defaultValue = "false") boolean recommend,
        @RequestParam(defaultValue = "10") int topK,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Long userId = recommend ? tryGetUserId(request) : null;
    Page<CompetitionResponse> competitions = competitionService.getCompetitions(
            pageable, null, null, null, recommend, false, userId, topK);
    return ResponseEntity.ok(competitions);
}
```

### 2）Service 编排（推荐分流 + fallback + 排序回填）

```java
public Page<CompetitionResponse> getCompetitions(..., boolean recommend, ..., Long userId, Integer topK) {
    int effectiveTopK = calculateEffectiveTopK(topK);
    String fallbackReason = recommend ? recommendationService.getRecommendFallbackReason(userId) : null;
    if (!recommend || fallbackReason != null) {
        return getCompetitionsDefault(..., recommend && fallbackReason != null, fallbackReason);
    }

    List<Competition> candidates = getCandidateCompetitions(...);
    HybridScoreResult hybridResult = recommendationService
            .calculateCompetitionHybridScores(userId, candidates, effectiveTopK);
    Map<Long, Double> matchScores = hybridResult.getScores();
    ...
}
```

### 3）RecommendationService 混合计算

```java
public HybridScoreResult calculateCompetitionHybridScores(Long userId, List<Competition> competitions, int topK) {
    Map<Long, Double> contentScores = contentBasedAlgorithm.calculateCompetitionSimilarity(userId, competitions);
    int behaviorCount = userBehaviorRepository
            .findByUserIdAndTargetType(userId, UserBehavior.TargetType.COMPETITION).size();
    if (behaviorCount < MIN_BEHAVIORS_FOR_CF) {
        return HybridScoreResult.fallback(contentScores, HYBRID_ALPHA, contentScores.size());
    }

    Map<Long, Double> cfScores = collaborativeFilteringAlgorithm.scoreCompetitionsForUser(userId, topK);
    ...
    double finalScore = HYBRID_ALPHA * contentScore + (1.0 - HYBRID_ALPHA) * cfScore;
    ...
}
```

### 4）内容推荐计算

```java
public Map<Long, Double> calculateCompetitionSimilarity(Long userId, List<Competition> competitions) {
    Map<Long, Integer> userSkillMap = getUserSkillMap(user);
    for (Competition competition : competitions) {
        double similarity = calculateCompetitionSkillSimilarity(userSkillMap, competition);
        similarities.put(competition.getId(), similarity);
    }
    return similarities;
}
```

### 5）协同过滤计算

```java
public Map<Long, Double> scoreCompetitionsForUser(Long userId, int topK) {
    Map<Long, Double> prefsU = aggregatePreferences(userBehaviors);
    Map<Long, Map<Long, Double>> prefsByUser = aggregatePreferencesByUser(candidateBehaviors);
    double sim = calculateCosineSimilarity(prefsU, entry.getValue());
    ...
    weightedSum.merge(itemId, sim * value, Double::sum);
    similaritySum.merge(itemId, Math.abs(sim), Double::sum);
    ...
}
```

### 6）行为写入（推荐输入闭环）

```java
// 详情页浏览触发
userBehaviorService.recordCompetitionView(userId, competitionId);

// 报名提交触发
userBehaviorService.recordCompetitionApply(student.getId(), competition.getId());
```

## 三、这段完整代码对应的流程

1. 前端请求竞赛列表，带上 `recommend=true`
2. Controller 解析参数，并尝试从 token 提取 `userId`
3. Service 检查推荐是否可用（登录态、技能画像）
4. 若不可用，直接进入默认分页列表
5. 若可用，先计算内容推荐分
6. 检查行为数量是否达到协同过滤阈值
7. 若不足，直接 fallback 到内容分
8. 若足够，执行协同过滤并得到 CF 分
9. 用 `0.8*content + 0.2*cf` 计算最终分
10. Service 按最终分排序 topK，剩余结果按默认规则补齐
11. 回填推荐理由、fallback 标记，返回分页响应

## 四、根据你的理解，对这段代码做逐段讲解

### A. Controller 逐段讲解

- `recommend` 是推荐开关，不开就不走混合推荐
- `tryGetUserId` 只在推荐模式尝试取用户，匿名场景不会抛错
- Controller 不计算分数，只做参数与路由转发

---

### B. CompetitionService 逐段讲解

- `calculateEffectiveTopK` 保证推荐数量可控
- `getRecommendFallbackReason` 控制推荐是否进入算法阶段
- `getCandidateCompetitions` 先做业务过滤（关键词/状态/可报名）
- 推荐分数为空时再兜底默认列表，保证接口稳定返回

---

### C. RecommendationService 逐段讲解

- `getRecommendFallbackReason` 是推荐入口前置门禁
- `MIN_BEHAVIORS_FOR_CF` 控制 CF 的最小行为数据要求
- `HybridScoreResult` 同时返回分数和 fallback 状态，便于上层记录与回填
- `buildCompetitionRecommendReason` 提供可解释性输出

---

### D. ContentBasedAlgorithm 逐段讲解

- 先把用户技能和竞赛技能都映射成“skillId -> 权重”
- 再做余弦相似度，得到每个竞赛的内容匹配分
- 该分数在冷启动阶段是核心主信号

---

### E. CollaborativeFilteringAlgorithm 逐段讲解

- 从行为表聚合用户偏好向量
- 按交集行为筛选候选邻居，减少无效比较
- 用相似度加权邻居偏好，预测用户未交互竞赛的得分
- 归一化后参与混合融合

---

### F. 行为闭环逐段讲解

- 用户浏览详情会沉淀 `VIEW`，权重低
- 用户报名会沉淀 `APPLY`，权重高
- 行为不断累积后，协同过滤效果会逐步增强
- 形成“使用系统 -> 产生行为 -> 优化推荐”的正反馈闭环

## 五、把这段代码压缩成一句可以稳定复述的话

混合推荐以竞赛列表接口为入口，先做可推荐性校验，再分别计算内容分与协同过滤分，并按 `alpha=0.8` 融合排序；在行为不足时自动 fallback 到内容推荐，从而保证推荐链路稳定、可解释、可持续迭代。

## 六、你接下来最适合怎么学

1. 按同样模板再写“队伍推荐实现理解”一份，形成对照。
2. 把 `HybridScoreResult` 的字段含义整理成一张表，汇报时可直接解释每个返回字段。
3. 录一条 5 分钟演示链路：打开推荐 -> 触发 VIEW/APPLY -> 再看推荐变化。

package com.competition.service;

import com.competition.dto.CompetitionCreateRequest;
import com.competition.dto.CompetitionRequiredSkillDTO;
import com.competition.dto.CompetitionResponse;
import com.competition.dto.CompetitionUpdateRequest;
import com.competition.dto.TeamRecommendationResponse;
import com.competition.entity.Competition;
import com.competition.entity.CompetitionSkill;
import com.competition.entity.Skill;
import com.competition.entity.Team;
import com.competition.entity.User;
import com.competition.exception.ApiException;
import com.competition.repository.CompetitionRepository;
import com.competition.repository.CompetitionSkillRepository;
import com.competition.repository.SkillRepository;
import com.competition.repository.TeamRepository;
import com.competition.repository.UserRepository;
import com.competition.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CompetitionService {

    private final CompetitionRepository competitionRepository;
    private final CompetitionSkillRepository competitionSkillRepository;
    private final SkillRepository skillRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final RecommendationService recommendationService;
    private final UserBehaviorService userBehaviorService;
    private final JwtUtils jwtUtils;
    private static final int DEFAULT_TOP_K = 10;
    private static final int MAX_TOP_K = 50;

    /**
     * 创建竞赛
     */
    public CompetitionResponse createCompetition(Long currentUserId, CompetitionCreateRequest request) {
        User admin = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user not found"));
        if (admin.getRole() != User.Role.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "only ADMIN can create competition");
        }
        validateCompetitionDates(
                request.getRegistrationDeadline(),
                request.getStartDate(),
                request.getEndDate(),
                request.getMinTeamSize(),
                request.getMaxTeamSize()
        );
        Competition competition = convertToEntity(request, admin);
        Competition savedCompetition = competitionRepository.save(competition);

        // 保存竞赛技能需求
        List<CompetitionSkill> requiredSkills = buildCompetitionSkills(request, savedCompetition);
        if (!requiredSkills.isEmpty()) {
            competitionSkillRepository.saveAll(requiredSkills);
        }

        return convertToResponse(savedCompetition);
    }

    private List<CompetitionSkill> buildCompetitionSkills(CompetitionCreateRequest request, Competition competition) {
        if (request == null || request.getRequiredSkills() == null || request.getRequiredSkills().isEmpty()) {
            return List.of();
        }

        Map<Long, Integer> skillImportanceMap = new HashMap<>();
        for (CompetitionRequiredSkillDTO dto : request.getRequiredSkills()) {
            if (dto == null || dto.getSkillId() == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "requiredSkills.skillId is required");
            }
            int importance = dto.getImportance() != null ? dto.getImportance() : 1;
            if (importance <= 0) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "requiredSkills.importance must be positive");
            }
            skillImportanceMap.merge(dto.getSkillId(), importance, Integer::max);
        }

        List<Long> skillIds = new ArrayList<>(skillImportanceMap.keySet());
        List<Skill> skills = skillRepository.findByIdIn(skillIds);
        if (skills.size() != skillIds.size()) {
            Set<Long> foundIds = skills.stream().map(Skill::getId).collect(Collectors.toSet());
            List<Long> missing = skillIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .sorted()
                    .collect(Collectors.toList());
            throw new ApiException(HttpStatus.NOT_FOUND, "skills not found: " + missing);
        }

        List<CompetitionSkill> competitionSkills = new ArrayList<>();
        for (Skill skill : skills) {
            CompetitionSkill competitionSkill = new CompetitionSkill();
            competitionSkill.setCompetition(competition);
            competitionSkill.setSkill(skill);
            competitionSkill.setImportance(skillImportanceMap.getOrDefault(skill.getId(), 1));
            competitionSkills.add(competitionSkill);
        }
        return competitionSkills;
    }
    /**
     * 获取可用竞赛
     */
    @Transactional(readOnly = true)
    public List<CompetitionResponse> getAvailableCompetitions() {
        return competitionRepository.findAvailableCompetitions(LocalDate.now())
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取可用竞赛实体
     */
    @Transactional(readOnly = true)
    public List<Competition> getAvailableCompetitionEntities() {
        return competitionRepository.findAvailableCompetitions(LocalDate.now());
    }

    /**
     * 分页获取竞赛列表
     */
    @Transactional(readOnly = true)
    public Page<CompetitionResponse> getCompetitions(Pageable pageable,
                                                     String name,
                                                     Competition.CompetitionStatus status,
                                                     String keyword,
                                                     boolean recommend,
                                                     boolean applyable,
                                                     Long userId,
                                                     Integer topK) {
        Competition.CompetitionStatus effectiveStatus =
                recommend ? Competition.CompetitionStatus.UPCOMING : status;
        int effectiveTopK = calculateEffectiveTopK(topK);
        String fallbackReason = recommend ? recommendationService.getRecommendFallbackReason(userId) : null;
        if (recommend) {
            log.info("competitions recommendEnabled={}, effectiveTopK={}, fallbackReason={}",
                    true, effectiveTopK, fallbackReason);
        } else {
            log.info("competitions recommendEnabled={}, effectiveTopK={}, fallbackReason={}",
                    false, effectiveTopK, null);
        }

        if (!recommend || fallbackReason != null) {
            return getCompetitionsDefault(
                    pageable, name, effectiveStatus, keyword, applyable, recommend && fallbackReason != null, fallbackReason);
        }

        List<Competition> candidates = getCandidateCompetitions(name, effectiveStatus, keyword, applyable);
        if (candidates.isEmpty()) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }

        RecommendationService.HybridScoreResult hybridResult =
                recommendationService.calculateCompetitionHybridScores(userId, candidates, effectiveTopK);
        Map<Long, Double> matchScores = hybridResult.getScores();
        log.info("competitions hybrid userId={}, topK={}, alpha={}, fallback={}, contentCandidates={}, cfCandidates={}",
                userId,
                effectiveTopK,
                hybridResult.getAlpha(),
                hybridResult.isFallback(),
                hybridResult.getContentCandidateCount(),
                hybridResult.getCfCandidateCount());
        if (matchScores.isEmpty()) {
            return getCompetitionsDefault(pageable, name, effectiveStatus, keyword, applyable, true, "EMPTY_SCORES");
        }

        Comparator<Competition> baseComparator = buildSortComparator(pageable.getSort());
        List<Competition> sortedCandidates = new ArrayList<>(candidates);
        sortedCandidates.sort(baseComparator);

        List<Competition> scored = new ArrayList<>(sortedCandidates);
        scored.sort(Comparator
                .comparing((Competition competition) ->
                        matchScores.getOrDefault(competition.getId(), 0.0))
                .reversed()
                .thenComparing(baseComparator));

        int topLimit = Math.min(effectiveTopK, scored.size());
        List<Competition> topList = scored.subList(0, topLimit);
        Set<Long> topIds = topList.stream()
                .map(Competition::getId)
                .collect(Collectors.toCollection(HashSet::new));

        List<Competition> rest = sortedCandidates.stream()
                .filter(competition -> !topIds.contains(competition.getId()))
                .sorted(baseComparator)
                .collect(Collectors.toList());

        List<Competition> combined = new ArrayList<>(topList);
        combined.addAll(rest);

        List<Competition> pageContent = slice(combined, pageable);
        List<CompetitionResponse> responses = pageContent.stream()
                .map(competition -> convertToResponse(
                        competition,
                        matchScores.get(competition.getId()),
                        topIds.contains(competition.getId()),
                        topIds.contains(competition.getId())
                                ? recommendationService.buildCompetitionRecommendReason(userId, competition)
                                : null,
                        hybridResult.isFallback(),
                        hybridResult.isFallback() ? "CF_FALLBACK" : null))
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, combined.size());
    }

    /**
     * 获取竞赛下队伍推荐
     */
    @Transactional(readOnly = true)
    public List<TeamRecommendationResponse> recommendTeams(Long currentUserId, Long competitionId, Integer topK) {
        try {
            User student = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "无权限：仅学生可进行竞赛报名推荐"));
            if (student.getRole() != User.Role.STUDENT) {
                throw new ApiException(HttpStatus.FORBIDDEN, "无权限：仅学生可进行竞赛报名推荐");
            }

            Competition competition = competitionRepository.findById(competitionId)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "竞赛不存在"));

            List<Team> teams = teamRepository.findByCompetitionId(competition.getId()).stream()
                    .filter(team -> team.getStatus() == Team.TeamStatus.RECRUITING)
                    .collect(Collectors.toList());

            if (teams.isEmpty()) {
                return List.of();
            }

            int limit = calculateEffectiveTopK(topK);
            Map<Long, Double> matchScores = recommendationService.calculateTeamMatchScores(currentUserId, teams);
            double maxScore = matchScores.values().stream().max(Double::compareTo).orElse(0.0);
            boolean fallbackSorted = maxScore < 0.10;

            Comparator<Team> defaultComparator = Comparator
                    .comparing(Team::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                    .reversed()
                    .thenComparing(Team::getId, Comparator.nullsLast(Comparator.naturalOrder()));

            List<Team> sorted = new ArrayList<>(teams);
            if (fallbackSorted) {
                sorted.sort(defaultComparator);
            } else {
                sorted.sort(Comparator
                        .comparing((Team team) -> matchScores.getOrDefault(team.getId(), 0.0))
                        .reversed()
                        .thenComparing(defaultComparator));
            }

            return sorted.stream()
                    .limit(limit)
                    .map(team -> {
                        TeamRecommendationResponse response = new TeamRecommendationResponse();
                        response.setTeamId(team.getId());
                        response.setTeamName(team.getName());
                        response.setTeamStatus(team.getStatus());
                        response.setMatchScore(matchScores.getOrDefault(team.getId(), 0.0));
                        response.setReasons(recommendationService.buildTeamRecommendReasons(currentUserId, team));
                        response.setFallbackSorted(fallbackSorted);
                        return response;
                    })
                    .collect(Collectors.toList());
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "队伍推荐失败，请稍后重试");
        }
    }

    /**
     * 根据ID获取竞赛详情
     */
    @Transactional(readOnly = true)
    public CompetitionResponse getCompetitionById(Long id) {
        Competition competition = competitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("竞赛不存在"));
        recordCompetitionViewSafely(competition.getId());
        return convertToResponse(competition);
    }

    private void recordCompetitionViewSafely(Long competitionId) {
        Long userId = resolveCurrentUserId();
        if (userId == null) {
            return;
        }
        try {
            userBehaviorService.recordCompetitionView(userId, competitionId);
        } catch (Exception ex) {
            log.warn("record competition view failed, userId={}, competitionId={}", userId, competitionId);
        }
    }

    private Long resolveCurrentUserId() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return null;
            }
            HttpServletRequest request = attrs.getRequest();
            if (request == null) {
                return null;
            }
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return null;
            }
            return jwtUtils.getUserIdFromToken(token.substring(7));
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 搜索竞赛
     */
    @Transactional(readOnly = true)
    public List<CompetitionResponse> searchCompetitions(String keyword) {
        List<Competition> competitions = competitionRepository.findByKeyword(keyword);
        return competitions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 根据分类获取竞赛
     */
    @Transactional(readOnly = true)
    public List<CompetitionResponse> getCompetitionsByCategory(String category) {
        List<Competition> competitions = competitionRepository.findByCategory(category);
        return competitions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 更新竞赛
     */
    public CompetitionResponse updateCompetition(Long currentUserId, Long id, CompetitionUpdateRequest request) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "用户不存在"));
        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "无权限：仅管理员可修改竞赛状态");
        }
        Competition competition = competitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("竞赛不存在"));
        applyUpdate(competition, request);
        Competition savedCompetition = competitionRepository.save(competition);
        return convertToResponse(savedCompetition);
    }

    private Competition convertToEntity(CompetitionCreateRequest request, User createdBy) {
        Competition competition = new Competition();
        competition.setName(request.getName());
        competition.setDescription(request.getDescription());
        competition.setOrganizer(request.getOrganizer());
        competition.setStartDate(request.getStartDate());
        competition.setEndDate(request.getEndDate());
        competition.setRegistrationDeadline(request.getRegistrationDeadline());
        competition.setMinTeamSize(request.getMinTeamSize());
        competition.setMaxTeamSize(request.getMaxTeamSize());
        competition.setCategory(request.getCategory());
        competition.setLevel(request.getLevel());
        competition.setStatus(request.getStatus());
        competition.setCreatedBy(createdBy);
        if (request.getManagerId() != null) {
            User manager = resolveTeacherManager(request.getManagerId());
            competition.setManager(manager);
        }
        return competition;
    }

    private void applyUpdate(Competition competition, CompetitionUpdateRequest request) {
        if (request.getStatus() != null) {
            competition.setStatus(request.getStatus());
        }
        if (request.getManagerId() != null) {
            User manager = resolveTeacherManager(request.getManagerId());
            competition.setManager(manager);
        }
    }

    private User resolveTeacherManager(Long managerId) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "manager not found"));
        if (manager.getRole() != User.Role.TEACHER) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "manager must be TEACHER");
        }
        return manager;
    }

    private void validateCompetitionDates(LocalDate registrationDeadline,
                                          LocalDate startDate,
                                          LocalDate endDate,
                                          Integer minTeamSize,
                                          Integer maxTeamSize) {
        if (registrationDeadline != null && startDate != null && registrationDeadline.isAfter(startDate)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "报名截止时间必须早于或等于比赛开始时间");
        }
        if (startDate != null && endDate != null && !startDate.isBefore(endDate)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "比赛开始时间必须早于比赛结束时间");
        }
        if (minTeamSize != null && maxTeamSize != null) {
            if (minTeamSize < 1 || maxTeamSize < minTeamSize) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "队伍人数范围不合法");
            }
        }
    }

    private CompetitionResponse convertToResponse(Competition competition) {
        return convertToResponse(competition, null, false, null, false, null);
    }

    private CompetitionResponse convertToResponse(Competition competition,
                                                  Double matchScore,
                                                  boolean recommend,
                                                  String recommendReason,
                                                  boolean fallbackApplied,
                                                  String fallbackReason) {
        CompetitionResponse response = new CompetitionResponse();
        response.setId(competition.getId());
        response.setName(competition.getName());
        response.setDescription(competition.getDescription());
        response.setOrganizer(competition.getOrganizer());
        response.setStartDate(competition.getStartDate());
        response.setEndDate(competition.getEndDate());
        response.setRegistrationDeadline(competition.getRegistrationDeadline());
        response.setMinTeamSize(competition.getMinTeamSize());
        response.setMaxTeamSize(competition.getMaxTeamSize());
        response.setCategory(competition.getCategory());
        response.setLevel(competition.getLevel());
        response.setStatus(competition.getStatus());
        response.setCreatedById(competition.getCreatedBy() != null ? competition.getCreatedBy().getId() : null);
        if (competition.getManager() != null) {
            response.setManagerId(competition.getManager().getId());
            response.setManagerName(competition.getManager().getRealName() != null
                    ? competition.getManager().getRealName()
                    : competition.getManager().getUsername());
        }
        response.setCreatedAt(competition.getCreatedAt());
        response.setUpdatedAt(competition.getUpdatedAt());
        response.setMatchScore(matchScore);
        response.setRecommend(recommend);
        response.setRecommendReason(recommendReason);
        response.setFallbackApplied(fallbackApplied);
        response.setFallbackReason(fallbackReason);
        if (competition.getCompetitionSkills() != null) {
            response.setRequiredSkills(competition.getCompetitionSkills().stream()
                    .map(cs -> {
                        CompetitionRequiredSkillDTO dto = new CompetitionRequiredSkillDTO();
                        dto.setSkillId(cs.getSkill().getId());
                        dto.setSkillName(cs.getSkill().getName());
                        dto.setImportance(cs.getImportance());
                        return dto;
                    })
                    .collect(java.util.stream.Collectors.toList()));
        }
        return response;
    }

    private Page<CompetitionResponse> getCompetitionsDefault(Pageable pageable,
                                                             String name,
                                                             Competition.CompetitionStatus status,
                                                             String keyword,
                                                             boolean applyable,
                                                             boolean fallbackApplied,
                                                             String fallbackReason) {
        if (keyword != null && !keyword.isBlank()) {
            List<Competition> results = competitionRepository.findByKeyword(keyword);
            results = filterByNameAndStatus(results, name, status);
            results = filterByApplyable(results, applyable);
            List<Competition> sorted = sortCompetitions(results, pageable.getSort());
            return applyFallbackMetadata(toResponsePage(sorted, pageable), fallbackApplied, fallbackReason);
        }

        if (name != null && status != null) {
            List<Competition> results = competitionRepository.findByNameContainingIgnoreCaseAndStatus(name, status);
            results = filterByApplyable(results, applyable);
            List<Competition> sorted = sortCompetitions(results, pageable.getSort());
            return applyFallbackMetadata(toResponsePage(sorted, pageable), fallbackApplied, fallbackReason);
        }
        if (name != null) {
            List<Competition> results = competitionRepository.findByNameContainingIgnoreCase(name);
            results = filterByApplyable(results, applyable);
            List<Competition> sorted = sortCompetitions(results, pageable.getSort());
            return applyFallbackMetadata(toResponsePage(sorted, pageable), fallbackApplied, fallbackReason);
        }
        if (status != null) {
            List<Competition> results = competitionRepository.findByStatus(status);
            results = filterByApplyable(results, applyable);
            List<Competition> sorted = sortCompetitions(results, pageable.getSort());
            return applyFallbackMetadata(toResponsePage(sorted, pageable), fallbackApplied, fallbackReason);
        }

        if (!applyable) {
            Page<CompetitionResponse> page = competitionRepository.findAll(pageable).map(this::convertToResponse);
            return applyFallbackMetadata(page, fallbackApplied, fallbackReason);
        }

        List<Competition> results = competitionRepository.findAll();
        results = filterByApplyable(results, true);
        List<Competition> sorted = sortCompetitions(results, pageable.getSort());
        return applyFallbackMetadata(toResponsePage(sorted, pageable), fallbackApplied, fallbackReason);
    }

    private Page<CompetitionResponse> applyFallbackMetadata(Page<CompetitionResponse> page,
                                                            boolean fallbackApplied,
                                                            String fallbackReason) {
        if (!fallbackApplied) {
            return page;
        }
        return page.map(item -> {
            item.setFallbackApplied(true);
            item.setFallbackReason(fallbackReason);
            return item;
        });
    }

    private List<Competition> getCandidateCompetitions(String name,
                                                       Competition.CompetitionStatus status,
                                                       String keyword,
                                                       boolean applyable) {
        if (keyword != null && !keyword.isBlank()) {
            List<Competition> results = competitionRepository.findByKeyword(keyword);
            results = filterByNameAndStatus(results, name, status);
            return filterByApplyable(results, applyable);
        }
        if (name != null && status != null) {
            return filterByApplyable(competitionRepository.findByNameContainingIgnoreCaseAndStatus(name, status), applyable);
        }
        if (name != null) {
            return filterByApplyable(competitionRepository.findByNameContainingIgnoreCase(name), applyable);
        }
        if (status != null) {
            return filterByApplyable(competitionRepository.findByStatus(status), applyable);
        }
        return filterByApplyable(competitionRepository.findAll(), applyable);
    }

    private List<Competition> filterByNameAndStatus(List<Competition> competitions,
                                                    String name,
                                                    Competition.CompetitionStatus status) {
        String trimmedName = name != null ? name.trim() : null;
        return competitions.stream()
                .filter(competition -> {
                    if (trimmedName == null || trimmedName.isEmpty()) {
                        return true;
                    }
                    String competitionName = competition.getName();
                    return competitionName != null &&
                            competitionName.toLowerCase().contains(trimmedName.toLowerCase());
                })
                .filter(competition -> status == null || status == competition.getStatus())
                .collect(Collectors.toList());
    }

    private List<Competition> sortCompetitions(List<Competition> competitions, Sort sort) {
        List<Competition> sorted = new ArrayList<>(competitions);
        Comparator<Competition> comparator = buildSortComparator(sort);
        sorted.sort(comparator);
        return sorted;
    }

    private List<Competition> filterByApplyable(List<Competition> competitions, boolean applyable) {
        if (!applyable) {
            return competitions;
        }
        LocalDate today = LocalDate.now();
        return competitions.stream()
                .filter(competition -> competition.getStatus() == Competition.CompetitionStatus.UPCOMING)
                .filter(competition -> {
                    LocalDate deadline = competition.getRegistrationDeadline();
                    return deadline != null && !deadline.isBefore(today);
                })
                .collect(Collectors.toList());
    }

    private int calculateEffectiveTopK(Integer topK) {
        if (topK == null || topK <= 0) {
            return DEFAULT_TOP_K;
        }
        return Math.min(topK, MAX_TOP_K);
    }

    private Comparator<Competition> buildSortComparator(Sort sort) {
        Sort.Order order = sort != null && sort.iterator().hasNext()
                ? sort.iterator().next()
                : new Sort.Order(Sort.Direction.DESC, "createdAt");

        Comparator<Competition> comparator = comparatorForProperty(order.getProperty());
        if (order.getDirection() == Sort.Direction.DESC) {
            comparator = comparator.reversed();
        }
        return comparator;
    }

    private Comparator<Competition> comparatorForProperty(String property) {
        if ("startDate".equalsIgnoreCase(property)) {
            return Comparator.comparing(Competition::getStartDate,
                    Comparator.nullsLast(Comparator.naturalOrder()));
        }
        if ("endDate".equalsIgnoreCase(property)) {
            return Comparator.comparing(Competition::getEndDate,
                    Comparator.nullsLast(Comparator.naturalOrder()));
        }
        if ("registrationDeadline".equalsIgnoreCase(property)) {
            return Comparator.comparing(Competition::getRegistrationDeadline,
                    Comparator.nullsLast(Comparator.naturalOrder()));
        }
        if ("name".equalsIgnoreCase(property)) {
            return Comparator.comparing(Competition::getName,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        }
        return Comparator.comparing(Competition::getCreatedAt,
                Comparator.nullsLast(Comparator.naturalOrder()));
    }

    private List<Competition> slice(List<Competition> competitions, Pageable pageable) {
        int start = Math.min((int) pageable.getOffset(), competitions.size());
        int end = Math.min(start + pageable.getPageSize(), competitions.size());
        return competitions.subList(start, end);
    }

    private Page<CompetitionResponse> toResponsePage(List<Competition> competitions, Pageable pageable) {
        List<Competition> content = slice(competitions, pageable);
        List<CompetitionResponse> responses = content.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, competitions.size());
    }
}

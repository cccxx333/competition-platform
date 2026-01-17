package com.competition.service;

import com.competition.dto.CompetitionCreateRequest;
import com.competition.dto.CompetitionResponse;
import com.competition.dto.CompetitionUpdateRequest;
import com.competition.entity.Competition;
import com.competition.entity.CompetitionSkill;
import com.competition.entity.User;
import com.competition.repository.CompetitionRepository;
import com.competition.repository.CompetitionSkillRepository;
import com.competition.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CompetitionService {

    private final CompetitionRepository competitionRepository;
    private final CompetitionSkillRepository competitionSkillRepository;
    private final UserRepository userRepository;
    private final RecommendationService recommendationService;
    private static final int DEFAULT_TOP_K = 10;
    private static final int MAX_TOP_K = 50;

    /**
     * 创建竞赛
     */
    public CompetitionResponse createCompetition(CompetitionCreateRequest request) {
        Competition competition = convertToEntity(request);
        Competition savedCompetition = competitionRepository.save(competition);

        // 保存竞赛技能需求
        if (competition.getCompetitionSkills() != null) {
            for (CompetitionSkill skill : competition.getCompetitionSkills()) {
                skill.setCompetition(savedCompetition);
                competitionSkillRepository.save(skill);
            }
        }

        return convertToResponse(savedCompetition);
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
                                                     Long userId,
                                                     Integer topK) {
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
            return getCompetitionsDefault(pageable, name, status, keyword);
        }

        List<Competition> candidates = getCandidateCompetitions(name, status, keyword);
        if (candidates.isEmpty()) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }

        Map<Long, Double> matchScores = recommendationService.calculateCompetitionMatchScores(userId, candidates);
        if (matchScores.isEmpty()) {
            return getCompetitionsDefault(pageable, name, status, keyword);
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
                                : null))
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, combined.size());
    }

    /**
     * 根据ID获取竞赛详情
     */
    @Transactional(readOnly = true)
    public CompetitionResponse getCompetitionById(Long id) {
        Competition competition = competitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("竞赛不存在"));
        return convertToResponse(competition);
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
    public CompetitionResponse updateCompetition(Long id, CompetitionUpdateRequest request) {
        Competition competition = competitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("竞赛不存在"));
        applyUpdate(competition, request);
        Competition savedCompetition = competitionRepository.save(competition);
        return convertToResponse(savedCompetition);
    }

    private Competition convertToEntity(CompetitionCreateRequest request) {
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
        if (request.getCreatedById() != null) {
            User createdBy = userRepository.findById(request.getCreatedById())
                    .orElseThrow(() -> new RuntimeException("创建人不存在"));
            competition.setCreatedBy(createdBy);
        }
        return competition;
    }

    private void applyUpdate(Competition competition, CompetitionUpdateRequest request) {
        if (request.getName() != null) {
            competition.setName(request.getName());
        }
        if (request.getDescription() != null) {
            competition.setDescription(request.getDescription());
        }
        if (request.getOrganizer() != null) {
            competition.setOrganizer(request.getOrganizer());
        }
        if (request.getStartDate() != null) {
            competition.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            competition.setEndDate(request.getEndDate());
        }
        if (request.getRegistrationDeadline() != null) {
            competition.setRegistrationDeadline(request.getRegistrationDeadline());
        }
        if (request.getMinTeamSize() != null) {
            competition.setMinTeamSize(request.getMinTeamSize());
        }
        if (request.getMaxTeamSize() != null) {
            competition.setMaxTeamSize(request.getMaxTeamSize());
        }
        if (request.getCategory() != null) {
            competition.setCategory(request.getCategory());
        }
        if (request.getLevel() != null) {
            competition.setLevel(request.getLevel());
        }
        if (request.getStatus() != null) {
            competition.setStatus(request.getStatus());
        }
        if (request.getCreatedById() != null) {
            User createdBy = userRepository.findById(request.getCreatedById())
                    .orElseThrow(() -> new RuntimeException("创建人不存在"));
            competition.setCreatedBy(createdBy);
        }
    }

    private CompetitionResponse convertToResponse(Competition competition) {
        return convertToResponse(competition, null, false, null);
    }

    private CompetitionResponse convertToResponse(Competition competition,
                                                  Double matchScore,
                                                  boolean recommend,
                                                  String recommendReason) {
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
        response.setCreatedAt(competition.getCreatedAt());
        response.setUpdatedAt(competition.getUpdatedAt());
        response.setMatchScore(matchScore);
        response.setRecommend(recommend);
        response.setRecommendReason(recommendReason);
        return response;
    }

    private Page<CompetitionResponse> getCompetitionsDefault(Pageable pageable,
                                                             String name,
                                                             Competition.CompetitionStatus status,
                                                             String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            List<Competition> results = competitionRepository.findByKeyword(keyword);
            results = filterByNameAndStatus(results, name, status);
            List<Competition> sorted = sortCompetitions(results, pageable.getSort());
            return toResponsePage(sorted, pageable);
        }

        if (name != null && status != null) {
            List<Competition> results = competitionRepository.findByNameContainingIgnoreCaseAndStatus(name, status);
            List<Competition> sorted = sortCompetitions(results, pageable.getSort());
            return toResponsePage(sorted, pageable);
        }
        if (name != null) {
            List<Competition> results = competitionRepository.findByNameContainingIgnoreCase(name);
            List<Competition> sorted = sortCompetitions(results, pageable.getSort());
            return toResponsePage(sorted, pageable);
        }
        if (status != null) {
            List<Competition> results = competitionRepository.findByStatus(status);
            List<Competition> sorted = sortCompetitions(results, pageable.getSort());
            return toResponsePage(sorted, pageable);
        }

        return competitionRepository.findAll(pageable).map(this::convertToResponse);
    }

    private List<Competition> getCandidateCompetitions(String name,
                                                       Competition.CompetitionStatus status,
                                                       String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            List<Competition> results = competitionRepository.findByKeyword(keyword);
            return filterByNameAndStatus(results, name, status);
        }
        if (name != null && status != null) {
            return competitionRepository.findByNameContainingIgnoreCaseAndStatus(name, status);
        }
        if (name != null) {
            return competitionRepository.findByNameContainingIgnoreCase(name);
        }
        if (status != null) {
            return competitionRepository.findByStatus(status);
        }
        return competitionRepository.findAll();
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

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CompetitionService {

    private final CompetitionRepository competitionRepository;
    private final CompetitionSkillRepository competitionSkillRepository;
    private final UserRepository userRepository;

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
    public Page<CompetitionResponse> getCompetitions(Pageable pageable, String name, Competition.CompetitionStatus status) {
        Page<Competition> competitions;
        if (name != null && status != null) {
            List<Competition> results = competitionRepository.findByNameContainingIgnoreCaseAndStatus(name, status);
            competitions = toPage(results, pageable);
        } else if (name != null) {
            List<Competition> results = competitionRepository.findByNameContainingIgnoreCase(name);
            competitions = toPage(results, pageable);
        } else if (status != null) {
            List<Competition> results = competitionRepository.findByStatus(status);
            competitions = toPage(results, pageable);
        } else {
            competitions = competitionRepository.findAll(pageable);
        }
        return competitions.map(this::convertToResponse);
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
        return response;
    }

    private Page<Competition> toPage(List<Competition> results, Pageable pageable) {
        int start = Math.min((int) pageable.getOffset(), results.size());
        int end = Math.min(start + pageable.getPageSize(), results.size());
        List<Competition> content = results.subList(start, end);
        return new PageImpl<>(content, pageable, results.size());
    }
}

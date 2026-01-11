package com.competition.service;

import com.competition.dto.CompetitionDTO;
import com.competition.entity.Competition;
import com.competition.entity.CompetitionSkill;
import com.competition.repository.CompetitionRepository;
import com.competition.repository.CompetitionSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

    /**
     * 创建竞赛
     */
    public CompetitionDTO createCompetition(CompetitionDTO competitionDTO) {
        Competition competition = convertToEntity(competitionDTO);
        Competition savedCompetition = competitionRepository.save(competition);

        // 保存竞赛技能要求
        if (competitionDTO.getRequiredSkills() != null) {
            for (CompetitionSkill skill : competitionDTO.getRequiredSkills()) {
                skill.setCompetition(savedCompetition);
                competitionSkillRepository.save(skill);
            }
        }

        return convertToDTO(savedCompetition);
    }

    /**
     * 获取所有可用竞赛
     */
    @Transactional(readOnly = true)
    public List<Competition> getAvailableCompetitions() {
        return competitionRepository.findAvailableCompetitions(LocalDate.now());
    }

    /**
     * 分页获取竞赛列表
     */
    @Transactional(readOnly = true)
    public Page<CompetitionDTO> getCompetitions(Pageable pageable) {
        Page<Competition> competitions = competitionRepository.findAll(pageable);
        return competitions.map(this::convertToDTO);
    }

    /**
     * 根据ID获取竞赛详情
     */
    @Transactional(readOnly = true)
    public CompetitionDTO getCompetitionById(Long id) {
        Competition competition = competitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("竞赛不存在"));
        return convertToDTO(competition);
    }

    /**
     * 搜索竞赛
     */
    @Transactional(readOnly = true)
    public List<CompetitionDTO> searchCompetitions(String keyword) {
        List<Competition> competitions = competitionRepository.findByKeyword(keyword);
        return competitions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据分类获取竞赛
     */
    @Transactional(readOnly = true)
    public List<CompetitionDTO> getCompetitionsByCategory(String category) {
        List<Competition> competitions = competitionRepository.findByCategory(category);
        return competitions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 更新竞赛状态
     */
    public void updateCompetitionStatus(Long id, Competition.CompetitionStatus status) {
        Competition competition = competitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("竞赛不存在"));
        competition.setStatus(status);
        competitionRepository.save(competition);
    }

    private Competition convertToEntity(CompetitionDTO dto) {
        Competition competition = new Competition();
        competition.setName(dto.getName());
        competition.setDescription(dto.getDescription());
        competition.setOrganizer(dto.getOrganizer());
        competition.setStartDate(dto.getStartDate());
        competition.setEndDate(dto.getEndDate());
        competition.setRegistrationDeadline(dto.getRegistrationDeadline());
        competition.setMaxTeamSize(dto.getMaxTeamSize());
        competition.setCategory(dto.getCategory());
        competition.setLevel(dto.getLevel());
        return competition;
    }

    private CompetitionDTO convertToDTO(Competition competition) {
        CompetitionDTO dto = new CompetitionDTO();
        dto.setId(competition.getId());
        dto.setName(competition.getName());
        dto.setDescription(competition.getDescription());
        dto.setOrganizer(competition.getOrganizer());
        dto.setStartDate(competition.getStartDate());
        dto.setEndDate(competition.getEndDate());
        dto.setRegistrationDeadline(competition.getRegistrationDeadline());
        dto.setMaxTeamSize(competition.getMaxTeamSize());
        dto.setCategory(competition.getCategory());
        dto.setLevel(competition.getLevel());
        dto.setStatus(competition.getStatus());
        dto.setCreatedAt(competition.getCreatedAt());
        return dto;
    }
}
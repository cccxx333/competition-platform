package com.competition.repository;

import com.competition.entity.CompetitionSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CompetitionSkillRepository extends JpaRepository<CompetitionSkill, Long> {
    List<CompetitionSkill> findByCompetitionId(Long competitionId);
    void deleteByCompetitionId(Long competitionId);
}
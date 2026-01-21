package com.competition.repository;

import com.competition.entity.TeamSkill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamSkillRepository extends JpaRepository<TeamSkill, Long> {
    boolean existsByTeam_Id(Long teamId);

    java.util.List<TeamSkill> findByTeam_Id(Long teamId);
}

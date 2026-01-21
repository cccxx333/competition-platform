package com.competition.repository;

import com.competition.entity.TeamAward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TeamAwardRepository extends JpaRepository<TeamAward, Long> {
    boolean existsByCompetitionIdAndTeamIdAndAwardNameAndIsActive(Long competitionId, Long teamId, String awardName, Byte isActive);
    boolean existsByCompetitionIdAndTeamIdAndIsActive(Long competitionId, Long teamId, Byte isActive);
    boolean existsByCompetitionIdAndTeamId(Long competitionId, Long teamId);
    Optional<TeamAward> findByIdAndIsActive(Long id, Byte isActive);
}

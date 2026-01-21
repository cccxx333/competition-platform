package com.competition.repository;

import com.competition.entity.TeamAward;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TeamAwardRepository extends JpaRepository<TeamAward, Long> {
    boolean existsByCompetitionIdAndTeamIdAndAwardNameAndIsActive(Long competitionId, Long teamId, String awardName, Byte isActive);
    boolean existsByCompetitionIdAndTeamIdAndIsActive(Long competitionId, Long teamId, Byte isActive);
    boolean existsByCompetitionIdAndTeamId(Long competitionId, Long teamId);
    Page<TeamAward> findByCompetitionIdAndTeamIdOrderByPublishedAtDesc(Long competitionId, Long teamId, Pageable pageable);
    Page<TeamAward> findByCompetitionIdOrderByPublishedAtDesc(Long competitionId, Pageable pageable);
    Page<TeamAward> findByTeamIdOrderByPublishedAtDesc(Long teamId, Pageable pageable);
    Page<TeamAward> findAllByOrderByPublishedAtDesc(Pageable pageable);
    Optional<TeamAward> findFirstByTeamIdAndIsActiveOrderByPublishedAtDesc(Long teamId, Byte isActive);
    Optional<TeamAward> findByIdAndIsActive(Long id, Byte isActive);
}

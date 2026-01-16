package com.competition.repository;

import com.competition.entity.TeamSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamSubmissionRepository extends JpaRepository<TeamSubmission, Long> {
    List<TeamSubmission> findByTeam_IdOrderBySubmittedAtDesc(Long teamId);

    Optional<TeamSubmission> findFirstByTeam_IdAndIsCurrentTrue(Long teamId);

    @Modifying
    @Query("update TeamSubmission s set s.isCurrent = false where s.team.id = :teamId and s.isCurrent = true")
    void clearCurrentByTeamId(@Param("teamId") Long teamId);
}

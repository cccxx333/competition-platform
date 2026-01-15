package com.competition.repository;

import com.competition.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    boolean existsByStudentIdAndCompetitionIdAndIsActiveTrueAndStatusIn(
            Long studentId,
            Long competitionId,
            Collection<Application.Status> statuses
    );

    List<Application> findByTeamId(Long teamId);
}

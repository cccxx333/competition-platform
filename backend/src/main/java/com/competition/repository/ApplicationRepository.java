package com.competition.repository;

import com.competition.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    boolean existsByStudentIdAndCompetitionIdAndIsActiveTrueAndStatusIn(
            Long studentId,
            Long competitionId,
            Collection<Application.Status> statuses
    );

    List<Application> findByTeamId(Long teamId);

    List<Application> findByTeamIdIn(Collection<Long> teamIds);

    List<Application> findByTeamIdInAndStatus(Collection<Long> teamIds, Application.Status status);

    Optional<Application> findByCompetitionIdAndStudentIdAndTeamIdAndIsActiveTrueAndStatus(
            Long competitionId,
            Long studentId,
            Long teamId,
            Application.Status status
    );

    Optional<Application> findFirstByStudent_IdAndCompetition_IdAndIsActiveFalseOrderByIdDesc(
            Long studentId,
            Long competitionId
    );

    Optional<Application> findFirstByStudent_IdAndCompetition_IdAndTeam_IdAndStatusAndIsActiveTrue(
            Long studentId,
            Long competitionId,
            Long teamId,
            Application.Status status
    );

    @Modifying
    @Query(
        "delete from Application a " +
        "where a.student.id = :studentId " +
        "and a.competition.id = :competitionId " +
        "and a.isActive = false " +
        "and a.id <> :keepId"
    )
    int deleteOtherInactiveByStudentAndCompetition(
            @Param("studentId") Long studentId,
            @Param("competitionId") Long competitionId,
            @Param("keepId") Long keepId
    );

    @Modifying
    @Query(
        "delete from Application a " +
        "where a.student.id = :studentId " +
        "and a.competition.id = :competitionId " +
        "and a.isActive = false"
    )
    int deleteAllInactiveByStudentAndCompetition(
            @Param("studentId") Long studentId,
            @Param("competitionId") Long competitionId
    );
}

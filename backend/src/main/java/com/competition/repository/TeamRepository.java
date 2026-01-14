package com.competition.repository;

import com.competition.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByStatus(Team.TeamStatus status);
    List<Team> findByCompetitionId(Long competitionId);
    List<Team> findByLeaderId(Long leaderId);
    boolean existsByCompetitionIdAndLeaderId(Long competitionId, Long leaderId);

    @Query("SELECT t FROM Team t WHERE t.status = 'RECRUITING'")
    List<Team> findAvailableTeams();

    @Query("SELECT t FROM Team t WHERE t.name LIKE %:keyword% OR t.description LIKE %:keyword%")
    List<Team> findByKeyword(@Param("keyword") String keyword);
}

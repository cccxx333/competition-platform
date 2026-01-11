package com.competition.repository;

import com.competition.entity.Competition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CompetitionRepository extends JpaRepository<Competition, Long> {

    List<Competition> findByStatus(Competition.CompetitionStatus status);

    List<Competition> findByCategory(String category);

    @Query("SELECT c FROM Competition c WHERE c.registrationDeadline >= :currentDate")
    List<Competition> findAvailableCompetitions(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT c FROM Competition c WHERE c.name LIKE %:keyword% OR c.description LIKE %:keyword%")
    List<Competition> findByKeyword(@Param("keyword") String keyword);

    @Query("SELECT c FROM Competition c JOIN c.competitionSkills cs WHERE cs.skill.id IN :skillIds")
    List<Competition> findBySkillIds(@Param("skillIds") List<Long> skillIds);
}
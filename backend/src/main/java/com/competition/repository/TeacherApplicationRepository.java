package com.competition.repository;

import com.competition.entity.TeacherApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherApplicationRepository extends JpaRepository<TeacherApplication, Long> {
    boolean existsByCompetitionIdAndTeacherId(Long competitionId, Long teacherId);
}

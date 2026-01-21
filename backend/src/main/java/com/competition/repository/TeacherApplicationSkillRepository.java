package com.competition.repository;

import com.competition.entity.TeacherApplicationSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeacherApplicationSkillRepository extends JpaRepository<TeacherApplicationSkill, Long> {
    List<TeacherApplicationSkill> findByTeacherApplication_Id(Long applicationId);

    void deleteByTeacherApplication_Id(Long applicationId);

    List<TeacherApplicationSkill> findByTeacherApplication_IdIn(List<Long> applicationIds);
}

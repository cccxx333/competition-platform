package com.competition.repository;

import com.competition.entity.TeacherApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherApplicationRepository extends JpaRepository<TeacherApplication, Long> {
    boolean existsByCompetitionIdAndTeacherId(Long competitionId, Long teacherId);

    Optional<TeacherApplication> findByCompetitionIdAndTeacherId(Long competitionId, Long teacherId);

    Page<TeacherApplication> findByTeacher_Id(Long teacherId, Pageable pageable);

    Page<TeacherApplication> findByTeacher_IdAndStatus(Long teacherId, TeacherApplication.Status status, Pageable pageable);

    @Query("select ta from TeacherApplication ta " +
            "join ta.teacher t " +
            "join ta.competition c " +
            "where (:status is null or ta.status = :status) " +
            "and (" +
            ":keyword is null or :keyword = '' " +
            "or lower(t.realName) like lower(concat('%', :keyword, '%')) " +
            "or lower(t.username) like lower(concat('%', :keyword, '%')) " +
            "or lower(c.name) like lower(concat('%', :keyword, '%'))" +
            ")")
    Page<TeacherApplication> searchAdmin(
            @Param("status") TeacherApplication.Status status,
            @Param("keyword") String keyword,
            Pageable pageable);
}

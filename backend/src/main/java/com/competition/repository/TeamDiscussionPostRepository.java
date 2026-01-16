package com.competition.repository;

import com.competition.entity.TeamDiscussionPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TeamDiscussionPostRepository extends JpaRepository<TeamDiscussionPost, Long> {
    List<TeamDiscussionPost> findByTeam_IdAndDeletedAtIsNullOrderByCreatedAtDesc(Long teamId);
}

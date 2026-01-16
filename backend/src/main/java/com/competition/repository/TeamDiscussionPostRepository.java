package com.competition.repository;

import com.competition.entity.TeamDiscussionPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface TeamDiscussionPostRepository extends JpaRepository<TeamDiscussionPost, Long> {
    Optional<TeamDiscussionPost> findByIdAndDeletedAtIsNull(Long id);
    List<TeamDiscussionPost> findByTeam_IdAndDeletedAtIsNullOrderByCreatedAtAsc(Long teamId);
}

package com.competition.service;

import com.competition.dto.TeamDiscussionPostCreateRequest;
import com.competition.dto.TeamDiscussionPostResponse;
import com.competition.entity.Team;
import com.competition.entity.TeamDiscussionPost;
import com.competition.entity.User;
import com.competition.exception.ApiException;
import com.competition.repository.TeamDiscussionPostRepository;
import com.competition.repository.TeamMemberRepository;
import com.competition.repository.TeamRepository;
import com.competition.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamDiscussionPostService {

    private final TeamDiscussionPostRepository teamDiscussionPostRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<TeamDiscussionPostResponse> listPosts(Long currentUserId, Long teamId) {
        Team team = loadTeam(teamId);
        User currentUser = loadUser(currentUserId);
        enforceTeamAccess(currentUser, team);

        return teamDiscussionPostRepository
                .findByTeam_IdAndDeletedAtIsNullOrderByCreatedAtDesc(teamId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public TeamDiscussionPostResponse createPost(Long currentUserId, Long teamId, TeamDiscussionPostCreateRequest req) {
        Team team = loadTeam(teamId);
        User currentUser = loadUser(currentUserId);
        enforceTeamAccess(currentUser, team);

        TeamDiscussionPost post = new TeamDiscussionPost();
        post.setTeam(team);
        post.setAuthor(currentUser);
        post.setParentPost(null);
        post.setContent(req.getContent());
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(null);
        post.setDeletedAt(null);
        post.setDeletedBy(null);

        TeamDiscussionPost saved = teamDiscussionPostRepository.save(post);
        return toResponse(saved);
    }

    private Team loadTeam(Long teamId) {
        if (teamId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "teamId is required");
        }
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "team not found"));
    }

    private User loadUser(Long userId) {
        if (userId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "userId is required");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user not found"));
    }

    private void enforceTeamAccess(User currentUser, Team team) {
        if (currentUser.getRole() == User.Role.ADMIN) {
            return;
        }
        if (currentUser.getRole() == User.Role.TEACHER) {
            if (team.getLeader() == null || !team.getLeader().getId().equals(currentUser.getId())) {
                throw new ApiException(HttpStatus.FORBIDDEN, "not team leader");
            }
            return;
        }
        if (currentUser.getRole() == User.Role.STUDENT) {
            boolean isMember = teamMemberRepository
                    .existsByTeamIdAndUserIdAndLeftAtIsNull(team.getId(), currentUser.getId());
            if (!isMember) {
                throw new ApiException(HttpStatus.FORBIDDEN, "not team member");
            }
            return;
        }
        throw new ApiException(HttpStatus.FORBIDDEN, "no permission");
    }

    private TeamDiscussionPostResponse toResponse(TeamDiscussionPost post) {
        TeamDiscussionPostResponse response = new TeamDiscussionPostResponse();
        response.setId(post.getId());
        response.setTeamId(post.getTeam() != null ? post.getTeam().getId() : null);
        response.setAuthorId(post.getAuthor() != null ? post.getAuthor().getId() : null);
        response.setParentPostId(post.getParentPost() != null ? post.getParentPost().getId() : null);
        response.setContent(post.getContent());
        response.setCreatedAt(post.getCreatedAt());
        response.setUpdatedAt(post.getUpdatedAt());
        return response;
    }
}

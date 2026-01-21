package com.competition.service;

import com.competition.dto.AwardPublishRequest;
import com.competition.dto.AwardPublishResponse;
import com.competition.entity.AwardRecipient;
import com.competition.entity.Competition;
import com.competition.entity.Team;
import com.competition.entity.TeamAward;
import com.competition.entity.TeamMember;
import com.competition.entity.User;
import com.competition.exception.ApiException;
import com.competition.repository.AwardRecipientRepository;
import com.competition.repository.CompetitionRepository;
import com.competition.repository.TeamAwardRepository;
import com.competition.repository.TeamMemberRepository;
import com.competition.repository.TeamRepository;
import com.competition.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class AwardAdminService {

    private static final Byte ACTIVE_FLAG = 1;

    private final UserRepository userRepository;
    private final CompetitionRepository competitionRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamAwardRepository teamAwardRepository;
    private final AwardRecipientRepository awardRecipientRepository;

    @Transactional
    public AwardPublishResponse publishAward(Long adminUserId, AwardPublishRequest request) {
        log.info("publishAward request: adminUserId={}, competitionId={}, teamId={}, awardName={}",
                adminUserId,
                request == null ? null : request.getCompetitionId(),
                request == null ? null : request.getTeamId(),
                request == null ? null : request.getAwardName());
        if (request == null
                || request.getCompetitionId() == null
                || request.getTeamId() == null
                || request.getAwardName() == null) {
            log.warn("THROW_BAD_REQUEST: competitionId/teamId/awardName required");
            throw new ApiException(HttpStatus.BAD_REQUEST, "competitionId, teamId and awardName are required");
        }

        String awardName = request.getAwardName().trim();
        if (awardName.isEmpty()) {
            log.warn("THROW_BAD_REQUEST: awardName is required");
            throw new ApiException(HttpStatus.BAD_REQUEST, "awardName is required");
        }
        if (awardName.length() > 64) {
            log.warn("THROW_BAD_REQUEST: awardName too long");
            throw new ApiException(HttpStatus.BAD_REQUEST, "awardName too long");
        }

        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user not found"));
        log.info("publishAward admin loaded: userId={}, role={}", admin.getId(), admin.getRole());
        if (admin.getRole() != User.Role.ADMIN) {
            log.warn("THROW_FORBIDDEN: only ADMIN can publish awards");
            throw new ApiException(HttpStatus.FORBIDDEN, "only ADMIN can publish awards");
        }

        Competition competition = competitionRepository.findById(request.getCompetitionId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "competition not found"));
        log.info("publishAward competition loaded: id={}, status={}", competition.getId(), competition.getStatus());

        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "team not found"));
        log.info("publishAward team loaded: id={}, status={}, competitionId={}",
                team.getId(),
                team.getStatus(),
                team.getCompetition() == null ? null : team.getCompetition().getId());
        if (team.getCompetition() == null || !competition.getId().equals(team.getCompetition().getId())) {
            log.warn("THROW_BAD_REQUEST: team does not belong to competition, competitionId={}, teamId={}",
                    competition.getId(),
                    team.getId());
            throw new ApiException(HttpStatus.BAD_REQUEST, "team does not belong to competition");
        }

        boolean duplicateAward = teamAwardRepository
                .existsByCompetitionIdAndTeamId(
                        competition.getId(),
                        team.getId()
                );
        log.info("publishAward duplicate check: method=existsByCompetitionIdAndTeamId, competitionId={}, teamId={}, result={}",
                competition.getId(),
                team.getId(),
                duplicateAward);
        if (duplicateAward) {
            log.warn("THROW_CONFLICT: award already published for this team, competitionId={}, teamId={}",
                    competition.getId(),
                    team.getId());
            throw new ApiException(HttpStatus.CONFLICT, "award already published for this team");
        }

        if (competition.getStatus() != Competition.CompetitionStatus.FINISHED) {
            log.warn("THROW_CONFLICT: competition is not finished, competitionId={}, status={}",
                    competition.getId(),
                    competition.getStatus());
            throw new ApiException(HttpStatus.CONFLICT, "competition is not finished");
        }

        if (team.getStatus() != Team.TeamStatus.CLOSED) {
            log.warn("THROW_CONFLICT: team is not closed, teamId={}, status={}", team.getId(), team.getStatus());
            throw new ApiException(HttpStatus.CONFLICT, "team is not closed");
        }

        List<TeamMember> members = teamMemberRepository.findByTeamIdAndLeftAtIsNull(team.getId());
        Set<Long> recipientUserIds = new LinkedHashSet<>();
        for (TeamMember member : members) {
            if (member.getUser() != null && member.getUser().getId() != null) {
                recipientUserIds.add(member.getUser().getId());
            }
        }
        log.info("publishAward recipients resolved: teamId={}, recipients={}", team.getId(), recipientUserIds.size());
        if (recipientUserIds.isEmpty()) {
            log.warn("THROW_CONFLICT: no active members, teamId={}", team.getId());
            throw new ApiException(HttpStatus.CONFLICT, "no active members");
        }

        LocalDateTime now = LocalDateTime.now();

        TeamAward award = new TeamAward();
        award.setCompetitionId(competition.getId());
        award.setTeamId(team.getId());
        award.setAwardName(awardName);
        award.setPublishedBy(admin.getId());
        award.setPublishedAt(now);
        award.setIsActive(ACTIVE_FLAG);

        TeamAward savedAward = teamAwardRepository.save(award);

        List<AwardRecipient> recipients = new ArrayList<>();
        for (Long userId : recipientUserIds) {
            AwardRecipient recipient = new AwardRecipient();
            recipient.setTeamAwardId(savedAward.getId());
            recipient.setUserId(userId);
            recipient.setRecordedAt(now);
            recipients.add(recipient);
        }
        awardRecipientRepository.saveAll(recipients);

        AwardPublishResponse response = new AwardPublishResponse();
        response.setAwardId(savedAward.getId());
        response.setCompetitionId(savedAward.getCompetitionId());
        response.setTeamId(savedAward.getTeamId());
        response.setAwardName(savedAward.getAwardName());
        response.setRecipientCount(recipientUserIds.size());
        response.setRecipientUserIds(new ArrayList<>(recipientUserIds));
        return response;
    }
}

package com.competition.service;

import com.competition.entity.Competition;
import com.competition.entity.Team;
import com.competition.repository.CompetitionRepository;
import com.competition.repository.TeamMemberRepository;
import com.competition.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompetitionTeamStatusSyncService {

    private final CompetitionRepository competitionRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Transactional
    public int syncAllCompetitions() {
        int changed = 0;
        for (Competition competition : competitionRepository.findAll()) {
            changed += syncCompetitionTeams(competition);
        }
        return changed;
    }

    @Transactional
    public int syncCompetitionTeams(Long competitionId) {
        if (competitionId == null) {
            return 0;
        }
        Competition competition = competitionRepository.findById(competitionId).orElse(null);
        if (competition == null) {
            return 0;
        }
        return syncCompetitionTeams(competition);
    }

    @Transactional
    public int syncCompetitionTeams(Competition competition) {
        if (competition == null || competition.getId() == null) {
            return 0;
        }

        List<Team> teams = teamRepository.findByCompetitionId(competition.getId());
        if (teams.isEmpty()) {
            return 0;
        }

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        boolean shouldCloseRecruiting = shouldCloseRecruiting(competition, today);
        boolean mayReopenAutomaticClosure = mayReopenAutomaticClosure(competition, today);
        int changed = 0;

        for (Team team : teams) {
            if (team.getStatus() == Team.TeamStatus.DISBANDED) {
                continue;
            }

            if (shouldCloseRecruiting && team.getStatus() == Team.TeamStatus.RECRUITING) {
                team.setStatus(Team.TeamStatus.CLOSED);
                team.setClosedAt(now);
                team.setClosedBy(null);
                team.setUpdatedAt(now);
                changed++;
                continue;
            }

            if (mayReopenAutomaticClosure && canReopenAutomaticClosure(team)) {
                team.setStatus(Team.TeamStatus.RECRUITING);
                team.setClosedAt(null);
                team.setClosedBy(null);
                team.setUpdatedAt(now);
                changed++;
            }
        }

        if (changed > 0) {
            teamRepository.saveAll(teams);
            log.info("Synced {} teams for competition {}.", changed, competition.getId());
        }
        return changed;
    }

    private boolean shouldCloseRecruiting(Competition competition, LocalDate today) {
        if (competition.getStatus() == Competition.CompetitionStatus.FINISHED) {
            return true;
        }
        LocalDate deadline = competition.getRegistrationDeadline();
        return deadline != null && deadline.isBefore(today);
    }

    private boolean mayReopenAutomaticClosure(Competition competition, LocalDate today) {
        if (competition.getStatus() == Competition.CompetitionStatus.FINISHED) {
            return false;
        }
        LocalDate deadline = competition.getRegistrationDeadline();
        return deadline == null || !deadline.isBefore(today);
    }

    private boolean canReopenAutomaticClosure(Team team) {
        if (team.getStatus() != Team.TeamStatus.CLOSED) {
            return false;
        }
        if (team.getClosedBy() != null) {
            return false;
        }

        Competition competition = team.getCompetition();
        Integer maxTeamSize = competition != null ? competition.getMaxTeamSize() : null;
        if (maxTeamSize == null) {
            return true;
        }

        long activeMembers = teamMemberRepository.countByTeamIdAndLeftAtIsNull(team.getId());
        return activeMembers < maxTeamSize;
    }
}

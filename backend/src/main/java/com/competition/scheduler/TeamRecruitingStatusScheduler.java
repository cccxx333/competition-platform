package com.competition.scheduler;

import com.competition.entity.Team;
import com.competition.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TeamRecruitingStatusScheduler {

    private static final long FIXED_DELAY_MS = 60000;

    private final TeamRepository teamRepository;

    @Scheduled(fixedDelay = FIXED_DELAY_MS)
    @Transactional
    public void closeExpiredRecruitingTeams() {
        LocalDate today = LocalDate.now();
        List<Team> expiredRecruitingTeams = teamRepository
                .findByStatusAndCompetition_RegistrationDeadlineBefore(Team.TeamStatus.RECRUITING, today);
        if (expiredRecruitingTeams.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        for (Team team : expiredRecruitingTeams) {
            team.setStatus(Team.TeamStatus.CLOSED);
            team.setClosedAt(now);
            team.setClosedBy(null);
            team.setUpdatedAt(now);
        }
        teamRepository.saveAll(expiredRecruitingTeams);
        log.info("Team recruiting status sync updated {} teams to CLOSED.", expiredRecruitingTeams.size());
    }
}


package com.competition.scheduler;

import com.competition.service.CompetitionTeamStatusSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class TeamRecruitingStatusScheduler {

    private static final long FIXED_DELAY_MS = 60000;

    private final CompetitionTeamStatusSyncService competitionTeamStatusSyncService;

    @Scheduled(fixedDelay = FIXED_DELAY_MS)
    @Transactional
    public void closeExpiredRecruitingTeams() {
        int changed = competitionTeamStatusSyncService.syncAllCompetitions();
        if (changed > 0) {
            log.info("Team recruiting status scheduler synced {} teams.", changed);
        }
    }
}

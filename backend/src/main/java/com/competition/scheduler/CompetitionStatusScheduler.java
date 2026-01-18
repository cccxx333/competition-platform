package com.competition.scheduler;

import com.competition.entity.Competition;
import com.competition.repository.CompetitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CompetitionStatusScheduler {

    private static final long FIXED_DELAY_MS = 60000;

    private final CompetitionRepository competitionRepository;

    // Auto sync may override manual status changes if time fields disagree.
    @Scheduled(fixedDelay = FIXED_DELAY_MS)
    @Transactional
    public void syncCompetitionStatus() {
        List<Competition> competitions = competitionRepository.findAll();
        if (competitions.isEmpty()) {
            return;
        }

        LocalDate today = LocalDate.now();
        List<Competition> changed = new ArrayList<>();

        for (Competition competition : competitions) {
            Competition.CompetitionStatus expected = deriveStatus(
                    today,
                    competition.getStartDate(),
                    competition.getEndDate(),
                    competition.getStatus()
            );
            if (expected != competition.getStatus()) {
                competition.setStatus(expected);
                changed.add(competition);
            }
        }

        if (!changed.isEmpty()) {
            competitionRepository.saveAll(changed);
            log.info("Competition status sync updated {} competitions.", changed.size());
        }
    }

    private Competition.CompetitionStatus deriveStatus(
            LocalDate today,
            LocalDate startDate,
            LocalDate endDate,
            Competition.CompetitionStatus fallback) {
        if (startDate == null || endDate == null) {
            return fallback;
        }
        if (today.isBefore(startDate)) {
            return Competition.CompetitionStatus.UPCOMING;
        }
        if (today.isAfter(endDate)) {
            return Competition.CompetitionStatus.FINISHED;
        }
        return Competition.CompetitionStatus.ONGOING;
    }
}

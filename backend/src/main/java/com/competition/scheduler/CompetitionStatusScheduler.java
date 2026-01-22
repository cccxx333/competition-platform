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
            Competition.CompetitionStatus current = competition.getStatus();
            if (current == Competition.CompetitionStatus.FINISHED) {
                continue;
            }
            Competition.CompetitionStatus dateStatus = deriveStatus(
                    today,
                    competition.getStartDate(),
                    competition.getEndDate()
            );
            if (dateStatus == null) {
                continue;
            }
            if (shouldAdvance(current, dateStatus)) {
                competition.setStatus(dateStatus);
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
            LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return null;
        }
        if (today.isBefore(startDate)) {
            return Competition.CompetitionStatus.UPCOMING;
        }
        if (today.isAfter(endDate)) {
            return Competition.CompetitionStatus.FINISHED;
        }
        return Competition.CompetitionStatus.ONGOING;
    }

    private boolean shouldAdvance(Competition.CompetitionStatus current,
                                  Competition.CompetitionStatus target) {
        if (current == null || target == null) {
            return false;
        }
        return statusRank(target) > statusRank(current);
    }

    private int statusRank(Competition.CompetitionStatus status) {
        if (status == Competition.CompetitionStatus.UPCOMING) {
            return 1;
        }
        if (status == Competition.CompetitionStatus.ONGOING) {
            return 2;
        }
        if (status == Competition.CompetitionStatus.FINISHED) {
            return 3;
        }
        return 0;
    }
}

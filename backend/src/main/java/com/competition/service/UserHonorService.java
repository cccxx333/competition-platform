package com.competition.service;

import com.competition.dto.AwardDetail;
import com.competition.dto.UserHonorsResponse;
import com.competition.entity.AwardRecipient;
import com.competition.entity.TeamAward;
import com.competition.repository.AwardRecipientRepository;
import com.competition.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserHonorService {

    private static final Byte ACTIVE_FLAG = 1;

    private final TeamMemberRepository teamMemberRepository;
    private final AwardRecipientRepository awardRecipientRepository;

    @Transactional(readOnly = true)
    public UserHonorsResponse getMyHonors(Long userId) {
        long participationCount = teamMemberRepository.countDistinctTeamsByUserId(userId);
        long awardCount = awardRecipientRepository.countActiveAwardsByUserId(userId, ACTIVE_FLAG);

        List<Object[]> rows = awardRecipientRepository.findActiveAwardDetailsByUserId(userId, ACTIVE_FLAG);
        List<AwardDetail> awards = new ArrayList<>();
        for (Object[] row : rows) {
            AwardRecipient recipient = (AwardRecipient) row[0];
            TeamAward award = (TeamAward) row[1];
            AwardDetail detail = new AwardDetail();
            detail.setAwardId(award.getId());
            detail.setAwardName(award.getAwardName());
            detail.setCompetitionId(award.getCompetitionId());
            detail.setTeamId(award.getTeamId());
            detail.setPublishedAt(award.getPublishedAt());
            awards.add(detail);
        }

        UserHonorsResponse response = new UserHonorsResponse();
        response.setParticipationCount((int) participationCount);
        response.setAwardCount((int) awardCount);
        response.setAwards(awards);
        return response;
    }
}

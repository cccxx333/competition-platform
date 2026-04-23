package com.competition.service;

import com.competition.dto.AdminDashboardStatsResponse;
import com.competition.dto.StudentDashboardStatsResponse;
import com.competition.dto.UserHonorsResponse;
import com.competition.entity.Application;
import com.competition.entity.Competition;
import com.competition.entity.User;
import com.competition.exception.ApiException;
import com.competition.repository.ApplicationRepository;
import com.competition.repository.CompetitionRepository;
import com.competition.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardStatsService {

    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final CompetitionRepository competitionRepository;
    private final UserHonorService userHonorService;

    @Transactional(readOnly = true)
    public StudentDashboardStatsResponse getStudentStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user not found"));
        if (user.getRole() != User.Role.STUDENT) {
            throw new ApiException(HttpStatus.FORBIDDEN, "only STUDENT can access student dashboard stats");
        }

        long pendingTeacherReviewCount = applicationRepository.countByStudent_IdAndStatusAndIsActive(
                userId,
                Application.Status.PENDING,
                true
        );

        long ongoingCompetitionCount = applicationRepository.countDistinctCompetitionByStudentAndStatuses(
                userId,
                Application.Status.APPROVED,
                Competition.CompetitionStatus.ONGOING
        );

        UserHonorsResponse honors = userHonorService.getMyHonors(userId);

        StudentDashboardStatsResponse response = new StudentDashboardStatsResponse();
        response.setPendingTeacherReviewCount(pendingTeacherReviewCount);
        response.setOngoingCompetitionCount(ongoingCompetitionCount);
        response.setParticipationCount(honors.getParticipationCount() != null ? honors.getParticipationCount() : 0);
        response.setAwardCount(honors.getAwardCount() != null ? honors.getAwardCount() : 0);
        return response;
    }

    @Transactional(readOnly = true)
    public AdminDashboardStatsResponse getAdminStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user not found"));
        if (user.getRole() != User.Role.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "only ADMIN can access admin dashboard stats");
        }

        long pendingTeacherUserCount = userRepository.countByRoleAndApprovalStatus(
                User.Role.TEACHER,
                User.ApprovalStatus.PENDING
        );
        long finishedPendingAwardCompetitionCount = competitionRepository.countByStatusAndWithoutActiveAward(
                Competition.CompetitionStatus.FINISHED
        );
        long ongoingCompetitionCount = competitionRepository.countByStatus(Competition.CompetitionStatus.ONGOING);
        long competitionTotalCount = competitionRepository.count();

        AdminDashboardStatsResponse response = new AdminDashboardStatsResponse();
        response.setPendingTeacherUserCount(pendingTeacherUserCount);
        response.setFinishedPendingAwardCompetitionCount(finishedPendingAwardCompetitionCount);
        response.setOngoingCompetitionCount(ongoingCompetitionCount);
        response.setCompetitionTotalCount(competitionTotalCount);
        return response;
    }
}

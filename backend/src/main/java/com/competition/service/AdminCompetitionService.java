package com.competition.service;

import com.competition.dto.CompetitionAdminUpdateRequest;
import com.competition.entity.Competition;
import com.competition.entity.User;
import com.competition.exception.ApiException;
import com.competition.repository.CompetitionRepository;
import com.competition.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminCompetitionService {

    private final CompetitionRepository competitionRepository;
    private final UserRepository userRepository;

    public Competition updateCompetition(Long adminUserId, Long competitionId, CompetitionAdminUpdateRequest request) {
        if (request == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "request is required");
        }

        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user not found"));
        if (admin.getRole() != User.Role.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "only ADMIN can update competition");
        }

        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "competition not found"));

        LocalDate nextStartDate = request.getStartDate() != null ? request.getStartDate() : competition.getStartDate();
        LocalDate nextEndDate = request.getEndDate() != null ? request.getEndDate() : competition.getEndDate();
        LocalDate nextDeadline = request.getRegistrationDeadline() != null
                ? request.getRegistrationDeadline()
                : competition.getRegistrationDeadline();

        if (nextStartDate != null && nextEndDate != null && nextStartDate.isAfter(nextEndDate)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "开始日期不能晚于结束日期");
        }
        if (nextDeadline != null && nextStartDate != null && nextDeadline.isAfter(nextStartDate)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "报名截止日期不能晚于开始日期");
        }

        if (request.getStartDate() != null) {
            competition.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            competition.setEndDate(request.getEndDate());
        }
        if (request.getRegistrationDeadline() != null) {
            competition.setRegistrationDeadline(request.getRegistrationDeadline());
        }
        if (request.getStatus() != null) {
            competition.setStatus(request.getStatus());
        }
        if (request.getDescription() != null) {
            competition.setDescription(request.getDescription());
        }

        return competitionRepository.save(competition);
    }
}

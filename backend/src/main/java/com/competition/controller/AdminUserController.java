package com.competition.controller;

import com.competition.dto.AdminUserUpdateRequest;
import com.competition.dto.UserDTO;
import com.competition.dto.UserProfileResponse;
import com.competition.service.UserService;
import com.competition.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    @GetMapping
    public ResponseEntity<Page<UserProfileResponse>> listUsers(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String approvalStatus) {
        Long userId = getUserIdFromToken(request);
        Sort sort = Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserProfileResponse> result = userService
                .listUsersForAdmin(userId, keyword, role, approvalStatus, pageable)
                .map(this::toProfileResponse);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{targetUserId}")
    public ResponseEntity<UserProfileResponse> getUserDetail(
            HttpServletRequest request,
            @PathVariable Long targetUserId) {
        Long userId = getUserIdFromToken(request);
        UserDTO user = userService.getUserByIdForAdmin(userId, targetUserId);
        return ResponseEntity.ok(toProfileResponse(user));
    }

    @PutMapping("/{targetUserId}")
    public ResponseEntity<UserProfileResponse> updateUser(
            HttpServletRequest request,
            @PathVariable Long targetUserId,
            @RequestBody AdminUserUpdateRequest updateRequest) {
        Long userId = getUserIdFromToken(request);
        UserDTO user = userService.updateUserByAdmin(userId, targetUserId, updateRequest);
        return ResponseEntity.ok(toProfileResponse(user));
    }

    private Long getUserIdFromToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtils.getUserIdFromToken(token);
        }
        throw new RuntimeException("invalid token");
    }

    private UserProfileResponse toProfileResponse(UserDTO dto) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(dto.getId());
        response.setAccountNo(dto.getAccountNo());
        response.setRole(dto.getRole());
        response.setApprovalStatus(dto.getApprovalStatus());
        response.setUsername(dto.getUsername());
        response.setDisplayName(dto.getDisplayName());
        response.setRealName(dto.getRealName());
        response.setEmail(dto.getEmail());
        response.setPhone(dto.getPhone());
        response.setAvatarUrl(dto.getAvatarUrl());
        response.setSchool(dto.getSchool());
        response.setMajor(dto.getMajor());
        response.setGrade(dto.getGrade());
        response.setCreatedAt(dto.getCreatedAt());
        return response;
    }
}

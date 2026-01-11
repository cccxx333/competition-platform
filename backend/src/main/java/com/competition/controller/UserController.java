package com.competition.controller;

import com.competition.dto.*;
import com.competition.entity.UserSkill;
import com.competition.service.UserService;
import com.competition.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
public class UserController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody UserRegistrationDTO registrationDTO) {
        UserDTO user = userService.registerUser(registrationDTO);
        return ResponseEntity.ok(user);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        String token = userService.loginUser(loginRequest.getUsername(), loginRequest.getPassword());
        return ResponseEntity.ok(new LoginResponse(token, "登录成功"));
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getProfile(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        UserDTO user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/profile")
    public ResponseEntity<UserDTO> updateProfile(
            HttpServletRequest request,
            @RequestBody UserDTO userDTO) {
        Long userId = getUserIdFromToken(request);
        UserDTO updatedUser = userService.updateUser(userId, userDTO);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * 更新用户技能
     */
    @PutMapping("/skills")
    public ResponseEntity<String> updateSkills(
            HttpServletRequest request,
            @RequestBody List<UserSkill> skills) {
        Long userId = getUserIdFromToken(request);
        userService.updateUserSkills(userId, skills);
        return ResponseEntity.ok("技能更新成功");
    }

    /**
     * 搜索用户
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserDTO>> searchUsers(
            @RequestParam(required = false) List<Long> skillIds,
            @RequestParam(required = false) String school) {
        List<UserDTO> users;
        if (skillIds != null && !skillIds.isEmpty()) {
            users = userService.searchUsersBySkills(skillIds);
        } else {
            users = userService.getAllUsers(); // 需要实现这个方法
        }
        return ResponseEntity.ok(users);
    }

    private Long getUserIdFromToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtils.getUserIdFromToken(token);
        }
        throw new RuntimeException("无效的token");
    }

    /**
     * 获取用户技能
     */
    @GetMapping("/{userId}/skills")
    public ResponseEntity<List<UserSkillDTO>> getUserSkills(@PathVariable Long userId) {
        try {
            List<UserSkillDTO> userSkills = userService.getUserSkills(userId);
            return ResponseEntity.ok(userSkills);
        } catch (Exception e) {
            log.error("获取用户技能失败: ", e);
            throw e;
        }
    }

    /**
     * 添加用户技能
     */
    @PostMapping("/skills")
    public ResponseEntity<UserSkillDTO> addUserSkill(
            HttpServletRequest request,
            @RequestBody UserSkillCreateDTO skillData) {
        try {
            Long userId = getUserIdFromToken(request);
            UserSkillDTO userSkill = userService.addUserSkill(userId, skillData);
            return ResponseEntity.ok(userSkill);
        } catch (Exception e) {
            log.error("添加用户技能失败: ", e);
            throw e;
        }
    }

    /**
     * 更新用户技能
     */
    @PutMapping("/skills/{userSkillId}")
    public ResponseEntity<UserSkillDTO> updateUserSkill(
            @PathVariable Long userSkillId,
            @RequestBody UserSkillCreateDTO skillData) {
        try {
            UserSkillDTO userSkill = userService.updateUserSkill(userSkillId, skillData);
            return ResponseEntity.ok(userSkill);
        } catch (Exception e) {
            log.error("更新用户技能失败: ", e);
            throw e;
        }
    }

    /**
     * 删除用户技能
     */
    @DeleteMapping("/skills/{userSkillId}")
    public ResponseEntity<Void> deleteUserSkill(@PathVariable Long userSkillId) {
        try {
            userService.deleteUserSkill(userSkillId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("删除用户技能失败: ", e);
            throw e;
        }
    }

    /**
     * 获取用户技能统计
     */
    @GetMapping("/{userId}/skills/stats")
    public ResponseEntity<UserSkillStatsDTO> getUserSkillStats(@PathVariable Long userId) {
        try {
            UserSkillStatsDTO stats = userService.getUserSkillStats(userId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("获取用户技能统计失败: ", e);
            throw e;
        }
    }

    /**
     * 根据分类获取用户技能
     */
    @GetMapping("/{userId}/skills/category/{category}")
    public ResponseEntity<List<UserSkillDTO>> getUserSkillsByCategory(
            @PathVariable Long userId,
            @PathVariable String category) {
        try {
            List<UserSkillDTO> userSkills = userService.getUserSkillsByCategory(userId, category);
            return ResponseEntity.ok(userSkills);
        } catch (Exception e) {
            log.error("根据分类获取用户技能失败: ", e);
            throw e;
        }
    }

    /**
     * 批量添加用户技能
     */
    @PostMapping("/skills/batch")
    public ResponseEntity<List<UserSkillDTO>> addUserSkillsBatch(
            HttpServletRequest request,
            @RequestBody List<UserSkillCreateDTO> skillsData) {
        try {
            Long userId = getUserIdFromToken(request);
            List<UserSkillDTO> userSkills = userService.addUserSkills(userId, skillsData);
            return ResponseEntity.ok(userSkills);
        } catch (Exception e) {
            log.error("批量添加用户技能失败: ", e);
            throw e;
        }
    }
}
package com.competition.service;

import com.competition.dto.*;
import com.competition.entity.Skill;
import com.competition.entity.User;
import com.competition.entity.UserSkill;
import com.competition.repository.SkillRepository;
import com.competition.repository.UserRepository;
import com.competition.repository.UserSkillRepository;
import com.competition.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    private final SkillRepository skillRepository;

    /**
     * 用户注册
     */
    public UserDTO registerUser(UserRegistrationDTO registrationDTO) {
        // 检查用户名和邮箱是否已存在
        if (userRepository.existsByUsername(registrationDTO.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(registrationDTO.getUsername());
        user.setEmail(registrationDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        user.setRealName(registrationDTO.getRealName());
        user.setSchool(registrationDTO.getSchool());
        user.setMajor(registrationDTO.getMajor());
        user.setGrade(registrationDTO.getGrade());
        user.setPhone(registrationDTO.getPhone());

        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    /**
     * 用户登录
     */
    public String loginUser(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("用户不存在");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        return jwtUtils.generateToken(user.getId(), user.getUsername());
    }

    /**
     * 获取用户信息
     */
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return convertToDTO(user);
    }

    /**
     * 更新用户信息
     */
    public UserDTO updateUser(Long userId, UserDTO userDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        user.setRealName(userDTO.getRealName());
        user.setSchool(userDTO.getSchool());
        user.setMajor(userDTO.getMajor());
        user.setGrade(userDTO.getGrade());
        user.setPhone(userDTO.getPhone());
        user.setAvatarUrl(userDTO.getAvatarUrl());

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    /**
     * 更新用户技能
     */
    public void updateUserSkills(Long userId, List<UserSkill> skills) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 删除原有技能
        userSkillRepository.deleteByUserId(userId);

        // 添加新技能
        for (UserSkill skill : skills) {
            skill.setUser(user);
            userSkillRepository.save(skill);
        }
    }

    /**
     * 根据技能搜索用户
     */
    @Transactional(readOnly = true)
    public List<UserDTO> searchUsersBySkills(List<Long> skillIds) {
        List<User> users = userRepository.findUsersBySkillId(skillIds.get(0)); // 简化实现
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setAccountNo(user.getAccountNo());
        dto.setRole(user.getRole() != null ? user.getRole().name() : null);
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRealName(user.getRealName());
        dto.setSchool(user.getSchool());
        dto.setMajor(user.getMajor());
        dto.setGrade(user.getGrade());
        dto.setPhone(user.getPhone());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    /**
     * 获取用户技能列表
     */
    public List<UserSkillDTO> getUserSkills(Long userId) {
        List<UserSkill> userSkills = userSkillRepository.findByUserId(userId);
        return userSkills.stream()
                .map(this::convertToUserSkillDTO)
                .collect(Collectors.toList());
    }

    /**
     * 添加用户技能
     */
    @Transactional
    public UserSkillDTO addUserSkill(Long userId, UserSkillCreateDTO skillData) {
        // 检查是否已存在
        if (userSkillRepository.existsByUserIdAndSkillId(userId, skillData.getSkillId())) {
            throw new RuntimeException("您已经添加过这个技能了");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        Skill skill = skillRepository.findById(skillData.getSkillId())
                .orElseThrow(() -> new RuntimeException("技能不存在"));

        UserSkill userSkill = new UserSkill();
        userSkill.setUser(user);
        userSkill.setSkill(skill);
        userSkill.setProficiency(skillData.getProficiency());

        userSkill = userSkillRepository.save(userSkill);
        log.info("用户 {} 添加技能 {} 成功", userId, skill.getName());

        return convertToUserSkillDTO(userSkill);
    }

    /**
     * 更新用户技能
     */
    @Transactional
    public UserSkillDTO updateUserSkill(Long userSkillId, UserSkillCreateDTO skillData) {
        UserSkill userSkill = userSkillRepository.findById(userSkillId)
                .orElseThrow(() -> new RuntimeException("用户技能不存在"));

        // 如果技能ID发生变化，需要检查新技能是否已存在
        if (!userSkill.getSkill().getId().equals(skillData.getSkillId())) {
            if (userSkillRepository.existsByUserIdAndSkillId(userSkill.getUser().getId(), skillData.getSkillId())) {
                throw new RuntimeException("您已经添加过这个技能了");
            }

            Skill newSkill = skillRepository.findById(skillData.getSkillId())
                    .orElseThrow(() -> new RuntimeException("技能不存在"));
            userSkill.setSkill(newSkill);
        }

        userSkill.setProficiency(skillData.getProficiency());
        userSkill = userSkillRepository.save(userSkill);

        log.info("用户技能 {} 更新成功", userSkillId);
        return convertToUserSkillDTO(userSkill);
    }

    /**
     * 删除用户技能
     */
    @Transactional
    public void deleteUserSkill(Long userSkillId) {
        UserSkill userSkill = userSkillRepository.findById(userSkillId)
                .orElseThrow(() -> new RuntimeException("用户技能不存在"));

        Long userId = userSkill.getUser().getId();
        String skillName = userSkill.getSkill().getName();

        userSkillRepository.delete(userSkill);
        log.info("用户 {} 的技能 {} 删除成功", userId, skillName);
    }

    /**
     * Delete user skill by userId and skillId.
     */
    @Transactional
    public void deleteUserSkillBySkillId(Long userId, Long skillId) {
        if (!userSkillRepository.existsByUserIdAndSkillId(userId, skillId)) {
            throw new RuntimeException("User skill not found");
        }
        userSkillRepository.deleteByUserIdAndSkillId(userId, skillId);
        log.info("User {} skill {} deleted", userId, skillId);
    }

    /**
     * 批量添加用户技能
     */
    @Transactional
    public List<UserSkillDTO> addUserSkills(Long userId, List<UserSkillCreateDTO> skillsData) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        List<UserSkill> userSkills = new LinkedList<>();

        for (UserSkillCreateDTO skillData : skillsData) {
            // 检查是否已存在
            if (!userSkillRepository.existsByUserIdAndSkillId(userId, skillData.getSkillId())) {
                Skill skill = skillRepository.findById(skillData.getSkillId())
                        .orElseThrow(() -> new RuntimeException("技能不存在: " + skillData.getSkillId()));

                UserSkill userSkill = new UserSkill();
                userSkill.setUser(user);
                userSkill.setSkill(skill);
                userSkill.setProficiency(skillData.getProficiency());

                userSkills.add(userSkill);
            }
        }

        if (!userSkills.isEmpty()) {
            userSkills = userSkillRepository.saveAll(userSkills);
            log.info("用户 {} 批量添加 {} 个技能", userId, userSkills.size());
        }

        return userSkills.stream()
                .map(this::convertToUserSkillDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户在某个分类下的技能
     */
    public List<UserSkillDTO> getUserSkillsByCategory(Long userId, String category) {
        List<UserSkill> userSkills = userSkillRepository.findByUserIdAndSkillCategory(userId, category);
        return userSkills.stream()
                .map(this::convertToUserSkillDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户技能统计
     */
    public UserSkillStatsDTO getUserSkillStats(Long userId) {
        Long totalSkills = userSkillRepository.countByUserId(userId);
        Double averageProficiency = userSkillRepository.getAverageProficiencyByUserId(userId);

        // 按分类统计
        List<UserSkill> allUserSkills = userSkillRepository.findByUserId(userId);
        Map<String, Long> categoryStats = allUserSkills.stream()
                .collect(Collectors.groupingBy(
                        us -> us.getSkill().getCategory(),
                        Collectors.counting()
                ));

        // 按熟练度统计
        Map<Integer, Long> proficiencyStats = allUserSkills.stream()
                .collect(Collectors.groupingBy(
                        UserSkill::getProficiency,
                        Collectors.counting()
                ));

        UserSkillStatsDTO stats = new UserSkillStatsDTO();
        stats.setTotalSkills(totalSkills);
        stats.setAverageProficiency(averageProficiency != null ? averageProficiency : 0.0);
        stats.setCategoryStats(categoryStats);
        stats.setProficiencyStats(proficiencyStats);

        return stats;
    }

    /**
     * 转换为DTO
     */
    private UserSkillDTO convertToUserSkillDTO(UserSkill userSkill) {
        UserSkillDTO dto = new UserSkillDTO();
        dto.setId(userSkill.getId());
        dto.setUserId(userSkill.getUser().getId());
        dto.setSkillId(userSkill.getSkill().getId());
        dto.setSkillName(userSkill.getSkill().getName());
        dto.setSkillCategory(userSkill.getSkill().getCategory());
        dto.setProficiency(userSkill.getProficiency());
        return dto;
    }
}

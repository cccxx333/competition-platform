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
     * 鐢ㄦ埛娉ㄥ唽
     */
    public UserDTO registerUser(UserRegistrationDTO registrationDTO) {
        String username = normalize(registrationDTO.getUsername());
        String email = normalize(registrationDTO.getEmail());
        if (username == null) {
            throw new RuntimeException("username is required");
        }
        if (email == null) {
            throw new RuntimeException("email is required");
        }
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("username already exists");
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("email already exists");
        }

        User.Role role = resolveRegistrationRole(registrationDTO.getRole());

        User user = new User();
        user.setUsername(username);
        user.setDisplayName(resolveDisplayName(registrationDTO.getDisplayName(), username));
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        user.setRole(role);
        user.setApprovalStatus(role == User.Role.TEACHER ? User.ApprovalStatus.PENDING : User.ApprovalStatus.APPROVED);
        user.setRealName(registrationDTO.getRealName());
        user.setSchool(registrationDTO.getSchool());
        user.setMajor(registrationDTO.getMajor());
        user.setGrade(registrationDTO.getGrade());
        user.setPhone(registrationDTO.getPhone());

        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }
    /**
     * 鐢ㄦ埛鐧诲綍
     */
    public String loginUser(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("user not found");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("password incorrect");
        }
        if (user.getApprovalStatus() == User.ApprovalStatus.PENDING) {
            throw new RuntimeException("教师账号待审核，请联系管理员");
        }
        if (user.getApprovalStatus() == User.ApprovalStatus.REJECTED) {
            throw new RuntimeException("教师账号审核未通过，请联系管理员");
        }

        return jwtUtils.generateToken(user.getId(), user.getUsername());
    }

    @Transactional(readOnly = true)
    public String getUserRole(String username) {
        return userRepository.findByUsername(username)
                .map(User::getRole)
                .map(Enum::name)
                .orElse(null);
    }
    /**
     * 鑾峰彇鐢ㄦ埛淇℃伅
     */
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("user not found"));
        return convertToDTO(user);
    }

    /**
     * 鏇存柊鐢ㄦ埛淇℃伅
     */
    public UserDTO updateUser(Long userId, UserDTO userDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("user not found"));

        String username = normalize(userDTO.getUsername());
        if (username != null && !username.equals(user.getUsername())) {
            if (userRepository.existsByUsernameAndIdNot(username, userId)) {
                throw new RuntimeException("username already exists");
            }
            user.setUsername(username);
        }

        String email = normalize(userDTO.getEmail());
        if (email != null && !email.equals(user.getEmail())) {
            if (userRepository.existsByEmailAndIdNot(email, userId)) {
                throw new RuntimeException("email already exists");
            }
            user.setEmail(email);
        }

        String displayName = normalize(userDTO.getDisplayName());
        if (displayName != null) {
            user.setDisplayName(displayName);
        }

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
     * 鏇存柊鐢ㄦ埛鎶€鑳?
     */
    public void updateUserSkills(Long userId, List<UserSkill> skills) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("user not found"));

        // 鍒犻櫎鍘熸湁鎶€鑳?
        userSkillRepository.deleteByUserId(userId);

        // 娣诲姞鏂版妧鑳?
        for (UserSkill skill : skills) {
            skill.setUser(user);
            userSkillRepository.save(skill);
        }
    }

    /**
     * 鏍规嵁鎶€鑳芥悳绱㈢敤鎴?
     */
    @Transactional(readOnly = true)
    public List<UserDTO> searchUsersBySkills(List<Long> skillIds) {
        List<User> users = userRepository.findUsersBySkillId(skillIds.get(0)); // 绠€鍖栧疄鐜?
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
        dto.setApprovalStatus(user.getApprovalStatus() != null ? user.getApprovalStatus().name() : null);
        dto.setUsername(user.getUsername());
        dto.setDisplayName(user.getDisplayName());
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

    private User.Role resolveRegistrationRole(String roleValue) {
        if (roleValue == null || roleValue.isBlank()) {
            return User.Role.STUDENT;
        }
        try {
            User.Role role = User.Role.valueOf(roleValue.trim().toUpperCase());
            if (role == User.Role.ADMIN) {
                throw new RuntimeException("invalid role");
            }
            return role;
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("invalid role");
        }
    }

    private String resolveDisplayName(String displayName, String defaultName) {
        String normalized = normalize(displayName);
        return normalized != null ? normalized : defaultName;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
    /**
     * 鑾峰彇鐢ㄦ埛鎶€鑳藉垪琛?
     */
    public List<UserSkillDTO> getUserSkills(Long userId) {
        List<UserSkill> userSkills = userSkillRepository.findByUserId(userId);
        return userSkills.stream()
                .map(this::convertToUserSkillDTO)
                .collect(Collectors.toList());
    }

    /**
     * 娣诲姞鐢ㄦ埛鎶€鑳?
     */
    @Transactional
    public UserSkillDTO addUserSkill(Long userId, UserSkillCreateDTO skillData) {
        // 妫€鏌ユ槸鍚﹀凡瀛樺湪
        if (userSkillRepository.existsByUserIdAndSkillId(userId, skillData.getSkillId())) {
            throw new RuntimeException("鎮ㄥ凡缁忔坊鍔犺繃杩欎釜鎶€鑳戒簡");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("user not found"));
        Skill skill = skillRepository.findById(skillData.getSkillId())
                .orElseThrow(() -> new RuntimeException("鎶€鑳戒笉瀛樺湪"));

        UserSkill userSkill = new UserSkill();
        userSkill.setUser(user);
        userSkill.setSkill(skill);
        userSkill.setProficiency(skillData.getProficiency());

        userSkill = userSkillRepository.save(userSkill);
        log.info("鐢ㄦ埛 {} 娣诲姞鎶€鑳?{} 鎴愬姛", userId, skill.getName());

        return convertToUserSkillDTO(userSkill);
    }

    /**
     * 鏇存柊鐢ㄦ埛鎶€鑳?
     */
    @Transactional
    public UserSkillDTO updateUserSkill(Long userSkillId, UserSkillCreateDTO skillData) {
        UserSkill userSkill = userSkillRepository.findById(userSkillId)
                .orElseThrow(() -> new RuntimeException("鐢ㄦ埛鎶€鑳戒笉瀛樺湪"));

        // 濡傛灉鎶€鑳絀D鍙戠敓鍙樺寲锛岄渶瑕佹鏌ユ柊鎶€鑳芥槸鍚﹀凡瀛樺湪
        if (!userSkill.getSkill().getId().equals(skillData.getSkillId())) {
            if (userSkillRepository.existsByUserIdAndSkillId(userSkill.getUser().getId(), skillData.getSkillId())) {
                throw new RuntimeException("鎮ㄥ凡缁忔坊鍔犺繃杩欎釜鎶€鑳戒簡");
            }

            Skill newSkill = skillRepository.findById(skillData.getSkillId())
                    .orElseThrow(() -> new RuntimeException("鎶€鑳戒笉瀛樺湪"));
            userSkill.setSkill(newSkill);
        }

        userSkill.setProficiency(skillData.getProficiency());
        userSkill = userSkillRepository.save(userSkill);

        log.info("鐢ㄦ埛鎶€鑳?{} 鏇存柊鎴愬姛", userSkillId);
        return convertToUserSkillDTO(userSkill);
    }

    /**
     * 鍒犻櫎鐢ㄦ埛鎶€鑳?
     */
    @Transactional
    public void deleteUserSkill(Long userSkillId) {
        UserSkill userSkill = userSkillRepository.findById(userSkillId)
                .orElseThrow(() -> new RuntimeException("鐢ㄦ埛鎶€鑳戒笉瀛樺湪"));

        Long userId = userSkill.getUser().getId();
        String skillName = userSkill.getSkill().getName();

        userSkillRepository.delete(userSkill);
        log.info("鐢ㄦ埛 {} 鐨勬妧鑳?{} 鍒犻櫎鎴愬姛", userId, skillName);
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
     * Update user skill proficiency by skillId.
     */
    @Transactional
    public UserSkillDTO updateUserSkillLevel(Long userId, Long skillId, Integer level) {
        UserSkill userSkill = userSkillRepository.findByUserIdAndSkillId(userId, skillId)
                .orElseThrow(() -> new RuntimeException("鎶€鑳芥湭缁戝畾"));
        userSkill.setProficiency(level);
        userSkill = userSkillRepository.save(userSkill);
        log.info("User {} skill {} proficiency updated", userId, skillId);
        return convertToUserSkillDTO(userSkill);
    }

    /**
     * 鎵归噺娣诲姞鐢ㄦ埛鎶€鑳?
     */
    @Transactional
    public List<UserSkillDTO> addUserSkills(Long userId, List<UserSkillCreateDTO> skillsData) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("user not found"));

        List<UserSkill> userSkills = new LinkedList<>();

        for (UserSkillCreateDTO skillData : skillsData) {
            // 妫€鏌ユ槸鍚﹀凡瀛樺湪
            if (!userSkillRepository.existsByUserIdAndSkillId(userId, skillData.getSkillId())) {
                Skill skill = skillRepository.findById(skillData.getSkillId())
                        .orElseThrow(() -> new RuntimeException("鎶€鑳戒笉瀛樺湪: " + skillData.getSkillId()));

                UserSkill userSkill = new UserSkill();
                userSkill.setUser(user);
                userSkill.setSkill(skill);
                userSkill.setProficiency(skillData.getProficiency());

                userSkills.add(userSkill);
            }
        }

        if (!userSkills.isEmpty()) {
            userSkills = userSkillRepository.saveAll(userSkills);
            log.info("User {} added {} skills in batch", userId, userSkills.size());
        }

        return userSkills.stream()
                .map(this::convertToUserSkillDTO)
                .collect(Collectors.toList());
    }

    /**
     * 鑾峰彇鐢ㄦ埛鍦ㄦ煇涓垎绫讳笅鐨勬妧鑳?
     */
    public List<UserSkillDTO> getUserSkillsByCategory(Long userId, String category) {
        List<UserSkill> userSkills = userSkillRepository.findByUserIdAndSkillCategory(userId, category);
        return userSkills.stream()
                .map(this::convertToUserSkillDTO)
                .collect(Collectors.toList());
    }

    /**
     * 鑾峰彇鐢ㄦ埛鎶€鑳界粺璁?
     */
    public UserSkillStatsDTO getUserSkillStats(Long userId) {
        Long totalSkills = userSkillRepository.countByUserId(userId);
        Double averageProficiency = userSkillRepository.getAverageProficiencyByUserId(userId);

        // 鎸夊垎绫荤粺璁?
        List<UserSkill> allUserSkills = userSkillRepository.findByUserId(userId);
        Map<String, Long> categoryStats = allUserSkills.stream()
                .collect(Collectors.groupingBy(
                        us -> us.getSkill().getCategory(),
                        Collectors.counting()
                ));

        // 鎸夌啛缁冨害缁熻
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
     * 杞崲涓篋TO
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



package com.competition.repository;

import com.competition.entity.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {

    /**
     * 根据用户ID查找用户技能
     */
    List<UserSkill> findByUserId(Long userId);

    /**
     * 根据技能ID查找用户技能
     */
    List<UserSkill> findBySkillId(Long skillId);

    /**
     * 检查用户是否已拥有某个技能
     */
    boolean existsByUserIdAndSkillId(Long userId, Long skillId);

    /**
     * 根据用户ID和技能ID查找用户技能
     */
    Optional<UserSkill> findByUserIdAndSkillId(Long userId, Long skillId);

    /**
     * 根据用户ID和技能ID列表查找用户技能
     */
    List<UserSkill> findByUserIdAndSkillIdIn(Long userId, List<Long> skillIds);

    /**
     * 删除用户的某个技能
     */
    void deleteByUserIdAndSkillId(Long userId, Long skillId);

    /**
     * 删除用户的所有技能
     */
    void deleteByUserId(Long userId);

    /**
     * 根据用户ID和熟练度查找技能
     */
    List<UserSkill> findByUserIdAndProficiency(Long userId, Integer proficiency);

    /**
     * 根据用户ID和熟练度范围查找技能
     */
    List<UserSkill> findByUserIdAndProficiencyBetween(Long userId, Integer minProficiency, Integer maxProficiency);

    /**
     * 查找拥有某个技能的所有用户
     */
    @Query("SELECT us FROM UserSkill us WHERE us.skill.id = :skillId")
    List<UserSkill> findUsersBySkillId(@Param("skillId") Long skillId);

    /**
     * 查找拥有某个技能且熟练度达到指定水平的用户
     */
    @Query("SELECT us FROM UserSkill us WHERE us.skill.id = :skillId AND us.proficiency >= :minProficiency")
    List<UserSkill> findUsersBySkillIdAndMinProficiency(@Param("skillId") Long skillId, @Param("minProficiency") Integer minProficiency);

    /**
     * 统计用户的技能数量
     */
    @Query("SELECT COUNT(us) FROM UserSkill us WHERE us.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    /**
     * 获取用户的平均技能熟练度
     */
    @Query("SELECT AVG(us.proficiency) FROM UserSkill us WHERE us.user.id = :userId")
    Double getAverageProficiencyByUserId(@Param("userId") Long userId);

    /**
     * 根据技能分类查找用户技能
     */
    @Query("SELECT us FROM UserSkill us WHERE us.user.id = :userId AND us.skill.category = :category")
    List<UserSkill> findByUserIdAndSkillCategory(@Param("userId") Long userId, @Param("category") String category);

    /**
     * 查找用户在某个分类下的最高熟练度
     */
    @Query("SELECT MAX(us.proficiency) FROM UserSkill us WHERE us.user.id = :userId AND us.skill.category = :category")
    Integer getMaxProficiencyByUserIdAndCategory(@Param("userId") Long userId, @Param("category") String category);
}
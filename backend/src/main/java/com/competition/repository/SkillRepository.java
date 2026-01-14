package com.competition.repository;

import com.competition.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    /**
     * 根据分类查找技能
     */
    List<Skill> findByCategory(String category);

    /**
     * 获取所有不重复的技能分类
     */
    @Query("SELECT DISTINCT s.category FROM Skill s ORDER BY s.category")
    List<String> findDistinctCategories();

    /**
     * 根据名称或描述搜索技能（忽略大小写）
     */
    List<Skill> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String name, String description);

    /**
     * 根据名称查找技能（忽略大小写）
     */
    List<Skill> findByNameContainingIgnoreCase(String name);

    /**
     * Check skill name exists (ignore case).
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * 根据多个ID查找技能
     */
    List<Skill> findByIdIn(List<Long> ids);
}
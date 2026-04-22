package com.competition.repository;

import com.competition.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);
    boolean existsByUsernameAndIdNot(String username, Long id);

    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);

    @Query("SELECT u FROM User u JOIN u.userSkills us WHERE us.skill.id = :skillId")
    List<User> findUsersBySkillId(@Param("skillId") Long skillId);

    @Query("SELECT u FROM User u WHERE u.school = :school")
    List<User> findBySchool(@Param("school") String school);

    @Query("SELECT u FROM User u " +
            "WHERE (:keyword IS NULL " +
            "       OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "       OR LOWER(u.displayName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "       OR LOWER(u.realName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "       OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "       OR LOWER(u.accountNo) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "  AND (:role IS NULL OR u.role = :role) " +
            "  AND (:approvalStatus IS NULL OR u.approvalStatus = :approvalStatus)")
    Page<User> searchAdmin(@Param("keyword") String keyword,
                           @Param("role") User.Role role,
                           @Param("approvalStatus") User.ApprovalStatus approvalStatus,
                           Pageable pageable);
}


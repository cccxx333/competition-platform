package com.competition.repository;

import com.competition.entity.AwardRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AwardRecipientRepository extends JpaRepository<AwardRecipient, Long> {
    List<AwardRecipient> findByUserId(Long userId);
    List<AwardRecipient> findByTeamAwardId(Long teamAwardId);

    @Query(
    "select ar, ta " +
    "from AwardRecipient ar, TeamAward ta " +
    "where ta.id = ar.teamAwardId " +
    "  and ar.userId = :userId " +
    "  and ta.isActive = :activeValue " +
    "order by ta.publishedAt desc"
)
List<Object[]> findActiveAwardDetailsByUserId(@Param("userId") Long userId,
                                              @Param("activeValue") Byte activeValue);

@Query(
    "select count(ar.id) " +
    "from AwardRecipient ar, TeamAward ta " +
    "where ta.id = ar.teamAwardId " +
    "  and ar.userId = :userId " +
    "  and ta.isActive = :activeValue"
)
long countActiveAwardsByUserId(@Param("userId") Long userId,
                               @Param("activeValue") Byte activeValue);

}

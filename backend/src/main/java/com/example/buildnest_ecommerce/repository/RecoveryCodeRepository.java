package com.example.buildnest_ecommerce.repository;

import com.example.buildnest_ecommerce.model.entity.RecoveryCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecoveryCodeRepository
        extends JpaRepository<RecoveryCode, Long> {

    List<RecoveryCode> findByUserIdAndUsedFalse(Long userId);

    @Modifying
    @Query("DELETE FROM RecoveryCode r WHERE r.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    // Atomic conditional update -- the WHERE used=false + affected-row-count
    // check closes a TOCTOU race where two concurrent requests could both
    // read the same code as unused and both mark it used (java-reviewer, #91).
    @Modifying
    @Query("UPDATE RecoveryCode r SET r.used = true, r.usedAt = :usedAt "
            + "WHERE r.id = :id AND r.used = false")
    int markUsedIfUnused(@Param("id") Long id,
            @Param("usedAt") java.time.LocalDateTime usedAt);
}

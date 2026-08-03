package com.example.buildnest_ecommerce.service.scheduler;

import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * GDPR right-to-erasure (#128, COMP-01): permanently scrubs PII from
 * accounts that were soft-deleted 30+ days ago. The row itself is
 * kept (financial/audit records -- Order, ProductReview, etc. -- FK
 * to it and must stay valid), only the direct PII fields on User are
 * overwritten with an irreversible, per-row-unique placeholder.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountAnonymizationScheduler {

    private static final int RETENTION_DAYS = 30;

    private final UserRepository userRepository;

    /**
     * Runs nightly at 3 AM -- after {@link TokenCleanupScheduler}'s
     * 2 AM token sweep, since an anonymized account's tokens should
     * already be long revoked by {@code deleteUser}.
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void anonymizeExpiredDeletedAccounts() {
        log.info("Starting GDPR anonymization sweep");
        LocalDateTime cutoff =
                LocalDateTime.now().minusDays(RETENTION_DAYS);

        java.util.List<User> candidates;
        try {
            candidates = userRepository
                    .findDeletedUsersPendingAnonymization(cutoff);
        } catch (Exception e) {
            log.error("Error fetching GDPR anonymization candidates", e);
            return;
        }

        // Each candidate is anonymized and saved individually --
        // JpaRepository#save() is already transactional per call, so
        // one row failing (e.g. a placeholder collision) can't roll
        // back every other pending erasure for the night the way a
        // single batched saveAll() transaction would -- that would
        // let one bad row indefinitely block the whole cohort's GDPR
        // erasure deadline. (A separate @Transactional wrapper method
        // was deliberately not added here: calling it from this same
        // class would hit Spring's self-invocation proxy gap and
        // silently no-op the annotation -- see spring/jpa.md.)
        int anonymized = 0;
        for (User user : candidates) {
            try {
                anonymize(user);
                userRepository.save(user);
                anonymized++;
            } catch (Exception e) {
                log.error("Failed to anonymize user id {}",
                        user.getId(), e);
            }
        }
        log.info("Anonymized {} of {} candidate account(s)",
                anonymized, candidates.size());
    }

    private void anonymize(User user) {
        String placeholder = "deleted-user-" + user.getId();
        user.setUsername(placeholder);
        user.setEmail(placeholder + "@anonymized.invalid");
        user.setFirstName("Deleted");
        user.setLastName("User");
        user.setPhoneNumber(null);
        user.setAnonymizedAt(LocalDateTime.now());
    }
}

package com.example.buildnest_ecommerce.service.scheduler;

import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountAnonymizationScheduler tests (#128, COMP-01)")
class AccountAnonymizationSchedulerServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountAnonymizationScheduler scheduler;

    @Test
    @DisplayName("Anonymizes every eligible candidate's PII fields and "
            + "stamps anonymizedAt")
    void anonymizesEligibleCandidates() {
        User candidate = new User();
        candidate.setId(42L);
        candidate.setUsername("realuser");
        candidate.setEmail("real@example.com");
        candidate.setFirstName("Real");
        candidate.setLastName("User");
        candidate.setPhoneNumber("5551234");

        when(userRepository.findDeletedUsersPendingAnonymization(any()))
                .thenReturn(List.of(candidate));

        scheduler.anonymizeExpiredDeletedAccounts();

        assertEquals("deleted-user-42", candidate.getUsername());
        assertEquals("deleted-user-42@anonymized.invalid",
                candidate.getEmail());
        assertEquals("Deleted", candidate.getFirstName());
        assertEquals("User", candidate.getLastName());
        assertNull(candidate.getPhoneNumber());
        assertNotNull(candidate.getAnonymizedAt());

        verify(userRepository).save(candidate);
    }

    @Test
    @DisplayName("One candidate's save failure does not block "
            + "anonymizing the rest (#128, COMP-02)")
    void oneFailureDoesNotBlockOthers() {
        User poisonRow = new User();
        poisonRow.setId(1L);
        User healthyRow = new User();
        healthyRow.setId(2L);

        when(userRepository.findDeletedUsersPendingAnonymization(any()))
                .thenReturn(List.of(poisonRow, healthyRow));
        when(userRepository.save(poisonRow))
                .thenThrow(new RuntimeException("unique constraint"));

        scheduler.anonymizeExpiredDeletedAccounts();

        verify(userRepository).save(healthyRow);
        assertNotNull(healthyRow.getAnonymizedAt());
    }

    @Test
    @DisplayName("Query cutoff is 30 days before now")
    void queriesWithThirtyDayCutoff() {
        when(userRepository.findDeletedUsersPendingAnonymization(any()))
                .thenReturn(List.of());

        LocalDateTime beforeCall = LocalDateTime.now().minusDays(30);
        scheduler.anonymizeExpiredDeletedAccounts();
        LocalDateTime afterCall = LocalDateTime.now().minusDays(30);

        ArgumentCaptor<LocalDateTime> captor =
                ArgumentCaptor.forClass(LocalDateTime.class);
        verify(userRepository)
                .findDeletedUsersPendingAnonymization(captor.capture());
        LocalDateTime cutoff = captor.getValue();

        assertTrue(!cutoff.isBefore(beforeCall) && !cutoff.isAfter(afterCall),
                "cutoff must fall within [now-30d at call start, "
                        + "now-30d at call end], was " + cutoff);
    }

    @Test
    @DisplayName("Does not propagate an exception from the repository")
    void doesNotPropagateRepositoryException() {
        when(userRepository.findDeletedUsersPendingAnonymization(any()))
                .thenThrow(new RuntimeException("db down"));

        scheduler.anonymizeExpiredDeletedAccounts();
    }
}

package com.example.buildnest_ecommerce.service.seller;

import com.example.buildnest_ecommerce.exception.DuplicateResourceException;
import com.example.buildnest_ecommerce.exception.ResourceNotFoundException;
import com.example.buildnest_ecommerce.model.dto.SellerResponseDTO;
import com.example.buildnest_ecommerce.model.entity.Role;
import com.example.buildnest_ecommerce.model.entity.Seller;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.model.payload.RegisterSellerRequest;
import com.example.buildnest_ecommerce.repository.RoleRepository;
import com.example.buildnest_ecommerce.repository.SellerRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import com.example.buildnest_ecommerce.service.notification
        .INotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SellerServiceImplTest {

    @Mock
    private SellerRepository sellerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private INotificationService notificationService;

    private SellerServiceImpl sellerService;
    private User user;

    @BeforeEach
    void setUp() {
        sellerService = new SellerServiceImpl(sellerRepository,
                userRepository, roleRepository, notificationService);
        user = new User();
        user.setId(3L);
        user.setUsername("shopowner");
        user.setRoles(new HashSet<>());
    }

    @Test
    void registerSeller_newUser_createsSellerAndGrantsRole() {
        RegisterSellerRequest request =
                new RegisterSellerRequest("Acme Décor", "REG-123");
        when(sellerRepository.existsByUser_Id(3L)).thenReturn(false);
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(sellerRepository.save(any(Seller.class)))
                .thenAnswer(inv -> {
                    Seller s = inv.getArgument(0);
                    s.setId(10L);
                    return s;
                });
        Role sellerRole = new Role();
        sellerRole.setName("ROLE_SELLER");
        when(roleRepository.findByName("ROLE_SELLER"))
                .thenReturn(Optional.of(sellerRole));

        SellerResponseDTO result =
                sellerService.registerSeller(3L, request);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.businessName()).isEqualTo("Acme Décor");
        assertThat(result.businessRegistrationNumber())
                .isEqualTo("REG-123");
        assertThat(result.verificationStatus())
                .isEqualTo(Seller.VerificationStatus.PENDING);
        assertThat(result.createdAt()).isNotNull();

        ArgumentCaptor<Seller> sellerCaptor =
                ArgumentCaptor.forClass(Seller.class);
        verify(sellerRepository).save(sellerCaptor.capture());
        assertThat(sellerCaptor.getValue().getBusinessRegistrationNumber())
                .isEqualTo("REG-123");
        assertThat(sellerCaptor.getValue().getVerificationStatus())
                .isEqualTo(Seller.VerificationStatus.PENDING);
        assertThat(sellerCaptor.getValue().getCreatedAt()).isNotNull();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        Set<Role> savedRoles = userCaptor.getValue().getRoles();
        assertThat(savedRoles).contains(sellerRole);
        assertThat(userCaptor.getValue().getUpdatedAt()).isNotNull();
    }

    @Test
    void registerSeller_roleDoesNotExistYet_createsAndGrantsNewRole() {
        RegisterSellerRequest request =
                new RegisterSellerRequest("Acme Décor", null);
        when(sellerRepository.existsByUser_Id(3L)).thenReturn(false);
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(sellerRepository.save(any(Seller.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(roleRepository.findByName("ROLE_SELLER"))
                .thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        sellerService.registerSeller(3L, request);

        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(roleCaptor.capture());
        assertThat(roleCaptor.getValue().getName()).isEqualTo("ROLE_SELLER");
        assertThat(roleCaptor.getValue().getDescription())
                .isEqualTo("Seller account — owns a product catalogue");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getRoles())
                .extracting(Role::getName)
                .contains("ROLE_SELLER");
    }

    @Test
    void registerSeller_userWithNullRoles_doesNotThrow() {
        // Regression test: a User whose roles collection was never
        // explicitly initialized (getRoles() == null) previously NPE'd in
        // grantSellerRole — caught by SellerRegistrationIT's real
        // persistence, invisible to the other unit tests above since they
        // all pre-set an empty HashSet.
        user.setRoles(null);
        RegisterSellerRequest request =
                new RegisterSellerRequest("Acme Décor", null);
        when(sellerRepository.existsByUser_Id(3L)).thenReturn(false);
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(sellerRepository.save(any(Seller.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        Role sellerRole = new Role();
        sellerRole.setName("ROLE_SELLER");
        when(roleRepository.findByName("ROLE_SELLER"))
                .thenReturn(Optional.of(sellerRole));

        sellerService.registerSeller(3L, request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getRoles()).contains(sellerRole);
    }

    @Test
    void registerSeller_alreadySeller_throwsDuplicateResourceException() {
        RegisterSellerRequest request =
                new RegisterSellerRequest("Acme Décor", null);
        when(sellerRepository.existsByUser_Id(3L)).thenReturn(true);

        assertThatThrownBy(() -> sellerService.registerSeller(3L, request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void registerSeller_unknownUser_throwsResourceNotFoundException() {
        RegisterSellerRequest request =
                new RegisterSellerRequest("Acme Décor", null);
        when(sellerRepository.existsByUser_Id(99L)).thenReturn(false);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sellerService.registerSeller(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getSellerProfile_existingSeller_returnsDto() {
        Seller seller = new Seller();
        seller.setId(10L);
        seller.setUser(user);
        seller.setBusinessName("Acme Décor");
        seller.setVerificationStatus(Seller.VerificationStatus.VERIFIED);
        when(sellerRepository.findByUser_Id(3L))
                .thenReturn(Optional.of(seller));

        SellerResponseDTO result = sellerService.getSellerProfile(3L);

        assertThat(result.verificationStatus())
                .isEqualTo(Seller.VerificationStatus.VERIFIED);
    }

    @Test
    void getSellerProfile_noSeller_throwsResourceNotFoundException() {
        when(sellerRepository.findByUser_Id(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sellerService.getSellerProfile(3L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateVerificationStatus_pendingToVerified_updatesAndNotifies() {
        user.setEmail("shopowner@example.com");
        Seller seller = new Seller();
        seller.setId(10L);
        seller.setUser(user);
        seller.setBusinessName("Acme Décor");
        seller.setVerificationStatus(Seller.VerificationStatus.PENDING);
        when(sellerRepository.findById(10L)).thenReturn(Optional.of(seller));
        when(sellerRepository.save(any(Seller.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // A non-null rejectionReason is passed even though this is an
        // approval — proves the approved ? null : rejectionReason ternary
        // actually suppresses it, rather than merely being untested because
        // rejectionReason happened to be null anyway.
        SellerResponseDTO result = sellerService.updateVerificationStatus(
                10L, "VERIFIED", "ignored on approval");

        assertThat(result.verificationStatus())
                .isEqualTo(Seller.VerificationStatus.VERIFIED);

        ArgumentCaptor<Seller> captor =
                ArgumentCaptor.forClass(Seller.class);
        verify(sellerRepository).save(captor.capture());
        assertThat(captor.getValue().getVerificationStatus())
                .isEqualTo(Seller.VerificationStatus.VERIFIED);
        assertThat(captor.getValue().getUpdatedAt()).isNotNull();

        verify(notificationService).sendSellerVerificationDecision(
                eq("shopowner@example.com"), eq("Acme Décor"),
                eq(true), isNull());
    }

    @Test
    void updateVerificationStatus_pendingToRejected_passesReasonToNotify() {
        user.setEmail("shopowner@example.com");
        Seller seller = new Seller();
        seller.setId(10L);
        seller.setUser(user);
        seller.setBusinessName("Acme Décor");
        seller.setVerificationStatus(Seller.VerificationStatus.PENDING);
        when(sellerRepository.findById(10L)).thenReturn(Optional.of(seller));
        when(sellerRepository.save(any(Seller.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        SellerResponseDTO result = sellerService.updateVerificationStatus(
                10L, "REJECTED", "Invalid registration number");

        assertThat(result.verificationStatus())
                .isEqualTo(Seller.VerificationStatus.REJECTED);
        verify(notificationService).sendSellerVerificationDecision(
                anyString(), anyString(), eq(false),
                eq("Invalid registration number"));
    }

    @Test
    void updateVerificationStatus_alreadyVerified_throwsIllegalArgument() {
        Seller seller = new Seller();
        seller.setId(10L);
        seller.setUser(user);
        seller.setVerificationStatus(Seller.VerificationStatus.VERIFIED);
        when(sellerRepository.findById(10L)).thenReturn(Optional.of(seller));

        assertThatThrownBy(() -> sellerService
                .updateVerificationStatus(10L, "REJECTED", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateVerificationStatus_pendingToPending_throwsIllegalArgument() {
        // Distinct from the alreadyVerified case above: VERIFIED has no
        // entry in VALID_TRANSITIONS at all (allowed == null), whereas
        // PENDING does have an entry whose set simply excludes PENDING
        // itself (allowed != null but !allowed.contains(PENDING)) — the
        // two halves of the allowed == null || !allowed.contains(...)
        // guard need separate coverage.
        Seller seller = new Seller();
        seller.setId(10L);
        seller.setUser(user);
        seller.setVerificationStatus(Seller.VerificationStatus.PENDING);
        when(sellerRepository.findById(10L)).thenReturn(Optional.of(seller));

        assertThatThrownBy(() -> sellerService
                .updateVerificationStatus(10L, "PENDING", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateVerificationStatus_invalidStatusValue_throwsIllegalArgument() {
        Seller seller = new Seller();
        seller.setId(10L);
        seller.setVerificationStatus(Seller.VerificationStatus.PENDING);
        when(sellerRepository.findById(10L)).thenReturn(Optional.of(seller));

        assertThatThrownBy(() -> sellerService
                .updateVerificationStatus(10L, "BOGUS", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateVerificationStatus_unknownSeller_throwsResourceNotFound() {
        when(sellerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sellerService
                .updateVerificationStatus(99L, "VERIFIED", null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getSellersByVerificationStatus_returnsMappedPage() {
        Seller seller = new Seller();
        seller.setId(10L);
        seller.setUser(user);
        seller.setBusinessName("Acme Décor");
        seller.setVerificationStatus(Seller.VerificationStatus.PENDING);
        Pageable pageable = PageRequest.of(0, 20);
        Page<Seller> page = new PageImpl<>(List.of(seller));
        when(sellerRepository.findByVerificationStatus(
                Seller.VerificationStatus.PENDING, pageable))
                .thenReturn(page);

        Page<SellerResponseDTO> result = sellerService
                .getSellersByVerificationStatus(
                        Seller.VerificationStatus.PENDING, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).businessName())
                .isEqualTo("Acme Décor");
    }
}

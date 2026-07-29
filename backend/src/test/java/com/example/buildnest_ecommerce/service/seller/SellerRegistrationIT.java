package com.example.buildnest_ecommerce.service.seller;

import com.example.buildnest_ecommerce.CivilEcommerceApplication;
import com.example.buildnest_ecommerce.config.TestSecurityConfig;
import com.example.buildnest_ecommerce.exception.DuplicateResourceException;
import com.example.buildnest_ecommerce.model.dto.SellerResponseDTO;
import com.example.buildnest_ecommerce.model.entity.Seller;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.model.payload.RegisterSellerRequest;
import com.example.buildnest_ecommerce.repository.SellerDistrictRepository;
import com.example.buildnest_ecommerce.repository.SellerRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Real-persistence regression test for #553 (FR-SEL-01) — proves the
 * Liquibase {@code sellers} changeset, the entity mapping, and the
 * {@code ROLE_SELLER} grant actually round-trip through H2, not just that
 * mocked repositories were called with the right arguments (see
 * service-layer-mocked-unit-tests-can-fully-cover... in the wiki lessons).
 */
@SpringBootTest(classes = CivilEcommerceApplication.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@Transactional
class SellerRegistrationIT {

    @Autowired
    private SellerService sellerService;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SellerDistrictRepository sellerDistrictRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("it-shopowner-" + System.nanoTime());
        user.setEmail("it-shopowner-" + System.nanoTime() + "@example.com");
        user.setPassword("hash");
        user.setIsActive(true);
        user.setIsDeleted(false);
        user.setCreatedAt(LocalDateTime.now());
        user = userRepository.save(user);
    }

    @Test
    void registerSeller_persistsSellerAndGrantsRole() {
        RegisterSellerRequest request =
                new RegisterSellerRequest("Acme Décor", "REG-1");

        SellerResponseDTO result =
                sellerService.registerSeller(user.getId(), request);

        assertThat(result.id()).isNotNull();
        Seller persisted = sellerRepository.findByUser_Id(user.getId())
                .orElseThrow();
        assertThat(persisted.getBusinessName()).isEqualTo("Acme Décor");
        assertThat(persisted.getVerificationStatus())
                .isEqualTo(Seller.VerificationStatus.PENDING);
        assertThat(sellerDistrictRepository
                .findAllBySeller_Id(persisted.getId())).isEmpty();

        User reloaded = userRepository.findById(user.getId()).orElseThrow();
        assertThat(reloaded.getRoles())
                .anyMatch(r -> "ROLE_SELLER".equals(r.getName()));
    }

    @Test
    void registerSeller_calledTwiceForSameUser_throwsDuplicate() {
        RegisterSellerRequest request =
                new RegisterSellerRequest("Acme Décor", null);
        sellerService.registerSeller(user.getId(), request);

        assertThatThrownBy(
                () -> sellerService.registerSeller(user.getId(), request))
                .isInstanceOf(DuplicateResourceException.class);
    }
}

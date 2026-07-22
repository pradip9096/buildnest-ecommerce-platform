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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

    private SellerServiceImpl sellerService;
    private User user;

    @BeforeEach
    void setUp() {
        sellerService = new SellerServiceImpl(
                sellerRepository, userRepository, roleRepository);
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
        assertThat(result.verificationStatus())
                .isEqualTo(Seller.VerificationStatus.PENDING);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        Set<Role> savedRoles = userCaptor.getValue().getRoles();
        assertThat(savedRoles).contains(sellerRole);
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
}

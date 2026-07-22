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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Seller registration — creates the 1:1 {@link Seller} extension of an
 * existing {@link User} account and grants {@code ROLE_SELLER}, following
 * the get-or-create role pattern already used by
 * {@code AuthServiceImpl.register}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerServiceImpl implements SellerService {

    private static final String ROLE_SELLER = "ROLE_SELLER";

    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public SellerResponseDTO registerSeller(
            Long userId, RegisterSellerRequest request) {
        log.info("Seller registration attempt for user: {}", userId);

        if (sellerRepository.existsByUser_Id(userId)) {
            throw new DuplicateResourceException(
                    "Seller", "userId", String.valueOf(userId));
        }

        User user = userRepository.findById(userId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("User", userId));

        Seller seller = new Seller();
        seller.setUser(user);
        seller.setBusinessName(request.getBusinessName());
        seller.setBusinessRegistrationNumber(
                request.getBusinessRegistrationNumber());
        seller.setVerificationStatus(Seller.VerificationStatus.PENDING);
        seller.setCreatedAt(LocalDateTime.now());

        Seller saved = sellerRepository.save(seller);

        grantSellerRole(user);

        return SellerResponseDTO.from(saved);
    }

    @Override
    public SellerResponseDTO getSellerProfile(Long userId) {
        Seller seller = sellerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Seller profile not found for user: " + userId));
        return SellerResponseDTO.from(seller);
    }

    private void grantSellerRole(User user) {
        Role sellerRole = roleRepository.findByName(ROLE_SELLER)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(ROLE_SELLER);
                    role.setDescription(
                            "Seller account — owns a product catalogue");
                    return roleRepository.save(role);
                });

        Set<Role> roles = user.getRoles() == null
                ? new HashSet<>()
                : new HashSet<>(user.getRoles());
        roles.add(sellerRole);
        user.setRoles(roles);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
}

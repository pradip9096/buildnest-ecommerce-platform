package com.example.buildnest_ecommerce.service.shipping;

import com.example.buildnest_ecommerce.config.ShippingConfig;
import com.example.buildnest_ecommerce.exception.ResourceNotFoundException;
import com.example.buildnest_ecommerce.model.dto.ShippingOptionDTO;
import com.example.buildnest_ecommerce.model.entity.Cart;
import com.example.buildnest_ecommerce.model.entity.ShippingMethod;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.model.payload.CreateShippingMethodRequest;
import com.example.buildnest_ecommerce.repository.CartRepository;
import com.example.buildnest_ecommerce.repository.ShippingMethodRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements ShippingService {

    private final ShippingMethodRepository shippingMethodRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ShippingConfig shippingConfig;

    @Override
    @Transactional(readOnly = true)
    public List<ShippingOptionDTO> getShippingOptions(Long userId, String postalCode) {
        log.info("Fetching shipping options for user={}, postalCode={}", userId, postalCode);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("No active cart found for user: " + userId));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty — no shipping options available");
        }

        int totalUnits = cart.getItems().stream()
                .mapToInt(item -> item.getQuantity())
                .sum();
        BigDecimal totalWeightKg = shippingConfig.getDefaultWeightPerItemKg()
                .multiply(new BigDecimal(totalUnits));

        return shippingMethodRepository.findAllByIsActiveTrue().stream()
                .map(method -> toOptionDTO(method, totalWeightKg, postalCode))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateCost(Long shippingMethodId, BigDecimal totalWeightKg, String postalCode) {
        ShippingMethod method = shippingMethodRepository.findById(shippingMethodId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Shipping method not found: " + shippingMethodId));
        return computeCost(method, totalWeightKg, postalCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShippingMethod> getAllShippingMethods() {
        return shippingMethodRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public ShippingMethod getShippingMethod(Long id) {
        return shippingMethodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping method not found: " + id));
    }

    @Override
    @Transactional
    public ShippingMethod createShippingMethod(CreateShippingMethodRequest request) {
        log.info("Creating shipping method: {}", request.getName());
        ShippingMethod method = new ShippingMethod();
        applyRequest(method, request);
        method.setIsActive(true);
        method.setCreatedAt(LocalDateTime.now());
        method.setUpdatedAt(LocalDateTime.now());
        return shippingMethodRepository.save(method);
    }

    @Override
    @Transactional
    public ShippingMethod updateShippingMethod(Long id, CreateShippingMethodRequest request) {
        log.info("Updating shipping method id={}", id);
        ShippingMethod method = getShippingMethod(id);
        applyRequest(method, request);
        method.setUpdatedAt(LocalDateTime.now());
        return shippingMethodRepository.save(method);
    }

    @Override
    @Transactional
    public void deactivateShippingMethod(Long id) {
        log.info("Deactivating shipping method id={}", id);
        ShippingMethod method = getShippingMethod(id);
        method.setIsActive(false);
        method.setUpdatedAt(LocalDateTime.now());
        shippingMethodRepository.save(method);
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private ShippingOptionDTO toOptionDTO(ShippingMethod method, BigDecimal totalWeightKg, String postalCode) {
        BigDecimal calculatedCost = computeCost(method, totalWeightKg, postalCode);
        return new ShippingOptionDTO(
                method.getId(),
                method.getName(),
                method.getDescription(),
                method.getBaseCost(),
                calculatedCost,
                method.getEstimatedDaysMin(),
                method.getEstimatedDaysMax());
    }

    /**
     * Formula: baseCost + (costPerKg × totalWeightKg × zoneMultiplier)
     * Zone is derived from the first two characters of postalCode via hash modulo.
     */
    BigDecimal computeCost(ShippingMethod method, BigDecimal totalWeightKg, String postalCode) {
        BigDecimal zoneMultiplier = resolveZoneMultiplier(postalCode);
        BigDecimal weightCost = method.getCostPerKg()
                .multiply(totalWeightKg)
                .multiply(zoneMultiplier)
                .setScale(2, RoundingMode.HALF_UP);
        return method.getBaseCost().add(weightCost);
    }

    BigDecimal resolveZoneMultiplier(String postalCode) {
        List<BigDecimal> multipliers = shippingConfig.getZoneMultipliers();
        if (!StringUtils.hasText(postalCode) || multipliers.isEmpty()) {
            return BigDecimal.ONE;
        }
        String prefix = postalCode.substring(0, Math.min(2, postalCode.length()));
        // Math.abs(Integer.MIN_VALUE) is still negative (two's complement overflow) --
        // Math.floorMod avoids that edge case and always returns a non-negative result.
        int idx = Math.floorMod(prefix.hashCode(), multipliers.size());
        return multipliers.get(idx);
    }

    private void applyRequest(ShippingMethod method, CreateShippingMethodRequest request) {
        method.setName(request.getName());
        method.setDescription(request.getDescription());
        method.setBaseCost(request.getBaseCost());
        method.setCostPerKg(request.getCostPerKg() != null ? request.getCostPerKg() : BigDecimal.ZERO);
        method.setEstimatedDaysMin(request.getEstimatedDaysMin());
        method.setEstimatedDaysMax(request.getEstimatedDaysMax());
    }
}

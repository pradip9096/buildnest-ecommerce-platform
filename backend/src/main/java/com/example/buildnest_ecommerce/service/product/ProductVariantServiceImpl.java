package com.example.buildnest_ecommerce.service.product;

import com.example.buildnest_ecommerce.model.dto.CreateProductVariantRequest;
import com.example.buildnest_ecommerce.model.dto.UpdateProductVariantRequest;
import com.example.buildnest_ecommerce.model.entity.Inventory;
import com.example.buildnest_ecommerce.model.entity.InventoryStatus;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.ProductVariant;
import com.example.buildnest_ecommerce.repository.InventoryRepository;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import com.example.buildnest_ecommerce.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Manages product variant CRUD and their per-variant inventory rows
 * (PROD-01, #81). Mirrors ProductServiceImpl's conventions (plain
 * RuntimeException for not-found, soft delete rather than hard delete).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductVariantServiceImpl implements ProductVariantService {
    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    public List<ProductVariant> getVariantsByProduct(Long productId) {
        log.info("Fetching variants for product: {}", productId);
        return productVariantRepository.findByProductId(productId);
    }

    @Override
    public ProductVariant getVariantById(Long variantId) {
        log.info("Fetching variant with id: {}", variantId);
        return productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException(
                        "Variant not found with id: " + variantId));
    }

    @Override
    @Transactional
    public ProductVariant createVariant(
            Long productId, CreateProductVariantRequest request) {
        log.info("Creating variant for product {}: {}", productId,
                request.getSku());
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException(
                        "Product not found with id: " + productId));

        if (productVariantRepository.existsBySku(request.getSku())) {
            throw new RuntimeException(
                    "Variant SKU already in use: " + request.getSku());
        }

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setSku(request.getSku());
        variant.setSize(request.getSize());
        variant.setColour(request.getColour());
        variant.setPriceAdjustment(request.getPriceAdjustment());
        variant.setIsActive(
                request.getIsActive() == null || request.getIsActive());
        variant.setCreatedAt(LocalDateTime.now());
        variant.setUpdatedAt(LocalDateTime.now());

        ProductVariant savedVariant = productVariantRepository.save(variant);

        Inventory inventory = new Inventory();
        inventory.setVariant(savedVariant);
        inventory.setQuantityInStock(request.getInitialStockQuantity());
        inventory.setMinimumStockLevel(request.getMinimumStockLevel());
        inventory.setStatus(request.getInitialStockQuantity() > 0
                ? InventoryStatus.IN_STOCK
                : InventoryStatus.OUT_OF_STOCK);
        inventory.setUpdatedAt(LocalDateTime.now());
        inventoryRepository.save(inventory);

        // Re-fetch rather than setting savedVariant.setInventory(inventory)
        // directly: ProductVariant.inventory carries cascade=ALL on this
        // mappedBy side, and setting it in-memory on an already-managed
        // entity triggers Hibernate to cascade-persist the (already
        // persisted) Inventory a second time on next flush, causing a
        // duplicate-key violation on the unique variant_id index.
        return productVariantRepository.findById(savedVariant.getId())
                .orElseThrow();
    }

    @Override
    @Transactional
    public ProductVariant updateVariant(
            Long variantId, UpdateProductVariantRequest request) {
        log.info("Updating variant with id: {}", variantId);
        ProductVariant variant = getVariantById(variantId);

        if (!variant.getSku().equals(request.getSku())
                && productVariantRepository.existsBySku(request.getSku())) {
            throw new RuntimeException(
                    "Variant SKU already in use: " + request.getSku());
        }

        variant.setSku(request.getSku());
        variant.setSize(request.getSize());
        variant.setColour(request.getColour());
        variant.setPriceAdjustment(request.getPriceAdjustment());
        if (request.getIsActive() != null) {
            variant.setIsActive(request.getIsActive());
        }
        variant.setUpdatedAt(LocalDateTime.now());

        return productVariantRepository.save(variant);
    }

    @Override
    @Transactional
    public void deleteVariant(Long variantId) {
        log.info("Soft-deleting variant with id: {}", variantId);
        ProductVariant variant = getVariantById(variantId);
        variant.setIsActive(false);
        variant.setUpdatedAt(LocalDateTime.now());
        productVariantRepository.save(variant);
    }
}

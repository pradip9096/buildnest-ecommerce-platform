package com.example.buildnest_ecommerce.service.product;

import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.ProductImage;
import com.example.buildnest_ecommerce.repository.ProductImageRepository;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import com.example.buildnest_ecommerce.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Manages a product's image gallery (PROD-02, #82). The first image uploaded for a
 * product becomes its primary image, and Product.imageUrl (the legacy single-image
 * field still read by other parts of the app) is kept in sync with whichever image
 * is currently primary — preserving backward compatibility rather than replacing
 * that field outright.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductImageServiceImpl implements ProductImageService {
    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final StorageService storageService;

    @Override
    public List<ProductImage> getImagesByProduct(Long productId) {
        log.info("Fetching images for product: {}", productId);
        return productImageRepository.findByProductIdOrderByDisplayOrderAsc(productId);
    }

    @Override
    @Transactional
    public ProductImage uploadImage(Long productId, MultipartFile file) {
        log.info("Uploading image for product: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        String url = storageService.store(file);
        long existingCount = productImageRepository.countByProductId(productId);

        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setImageUrl(url);
        image.setDisplayOrder((int) existingCount);
        image.setIsPrimary(existingCount == 0);
        image.setCreatedAt(LocalDateTime.now());

        ProductImage saved = productImageRepository.save(image);

        if (Boolean.TRUE.equals(saved.getIsPrimary())) {
            product.setImageUrl(url);
            product.setUpdatedAt(LocalDateTime.now());
            productRepository.save(product);
        }

        return saved;
    }

    @Override
    @Transactional
    public List<ProductImage> reorderImages(Long productId, List<Long> orderedImageIds) {
        log.info("Reordering images for product: {}", productId);
        List<ProductImage> existing = productImageRepository.findByProductIdOrderByDisplayOrderAsc(productId);

        Set<Long> existingIds = existing.stream().map(ProductImage::getId).collect(Collectors.toSet());
        Set<Long> requestedIds = new HashSet<>(orderedImageIds);
        if (!existingIds.equals(requestedIds)) {
            throw new RuntimeException(
                    "Reorder list must contain exactly the product's current image IDs, no more and no fewer");
        }

        var imagesById = existing.stream().collect(Collectors.toMap(ProductImage::getId, img -> img));
        for (int i = 0; i < orderedImageIds.size(); i++) {
            ProductImage image = imagesById.get(orderedImageIds.get(i));
            image.setDisplayOrder(i);
        }

        return productImageRepository.saveAll(
                orderedImageIds.stream().map(imagesById::get).collect(Collectors.toList()));
    }

    @Override
    @Transactional
    public void deleteImage(Long productId, Long imageId) {
        log.info("Deleting image {} for product {}", imageId, productId);
        ProductImage image = productImageRepository.findByIdAndProductId(imageId, productId)
                .orElseThrow(() -> new RuntimeException("Image not found with id: " + imageId));

        boolean wasPrimary = Boolean.TRUE.equals(image.getIsPrimary());
        productImageRepository.delete(image);

        if (!wasPrimary) {
            return;
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        List<ProductImage> remaining = productImageRepository.findByProductIdOrderByDisplayOrderAsc(productId);
        if (remaining.isEmpty()) {
            product.setImageUrl(null);
        } else {
            ProductImage newPrimary = remaining.get(0);
            newPrimary.setIsPrimary(true);
            productImageRepository.save(newPrimary);
            product.setImageUrl(newPrimary.getImageUrl());
        }
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }
}

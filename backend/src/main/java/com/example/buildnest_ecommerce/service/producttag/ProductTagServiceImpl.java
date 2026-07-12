package com.example.buildnest_ecommerce.service.producttag;

import com.example.buildnest_ecommerce.model.dto.CreateProductTagRequest;
import com.example.buildnest_ecommerce.model.dto.UpdateProductTagRequest;
import com.example.buildnest_ecommerce.model.entity.ProductTag;
import com.example.buildnest_ecommerce.repository.ProductTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "productTags")
@Transactional(readOnly = true)
@SuppressWarnings("null")
public class ProductTagServiceImpl implements ProductTagService {
    private final ProductTagRepository productTagRepository;

    @Override
    @Cacheable(key = "'all'")
    public List<ProductTag> getAllTags() {
        log.info("Fetching all product tags");
        return productTagRepository.findAll();
    }

    @Override
    @Cacheable(key = "#tagId")
    public ProductTag getTagById(Long tagId) {
        log.info("Fetching product tag with id: {}", tagId);
        return productTagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Product tag not found with id: " + tagId));
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public ProductTag createTag(CreateProductTagRequest request) {
        log.info("Creating new product tag: {}", request.getName());
        String slug = toSlug(request.getName());
        productTagRepository.findByName(request.getName()).ifPresent(existing -> {
            throw new IllegalArgumentException("Tag already exists with name: " + request.getName());
        });
        productTagRepository.findBySlug(slug).ifPresent(existing -> {
            throw new IllegalArgumentException("Tag already exists with slug: " + slug);
        });

        ProductTag tag = new ProductTag();
        tag.setName(request.getName());
        tag.setSlug(slug);
        tag.setCreatedAt(LocalDateTime.now());
        return productTagRepository.save(tag);
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public ProductTag updateTag(Long tagId, UpdateProductTagRequest request) {
        log.info("Updating product tag with id: {}", tagId);
        ProductTag existingTag = getTagById(tagId);
        String slug = toSlug(request.getName());

        productTagRepository.findByName(request.getName())
                .filter(other -> !other.getId().equals(tagId))
                .ifPresent(other -> {
                    throw new IllegalArgumentException("Tag already exists with name: " + request.getName());
                });
        productTagRepository.findBySlug(slug)
                .filter(other -> !other.getId().equals(tagId))
                .ifPresent(other -> {
                    throw new IllegalArgumentException("Tag already exists with slug: " + slug);
                });

        existingTag.setName(request.getName());
        existingTag.setSlug(slug);
        return productTagRepository.save(existingTag);
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public void deleteTag(Long tagId) {
        log.info("Deleting product tag with id: {}", tagId);
        getTagById(tagId);
        productTagRepository.deleteById(tagId);
    }

    private String toSlug(String name) {
        return name.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }
}

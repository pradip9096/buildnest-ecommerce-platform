package com.example.buildnest_ecommerce.service.product;

import com.example.buildnest_ecommerce.model.dto.CreateProductVariantRequest;
import com.example.buildnest_ecommerce.model.dto.UpdateProductVariantRequest;
import com.example.buildnest_ecommerce.model.entity.ProductVariant;

import java.util.List;

public interface ProductVariantService {
    List<ProductVariant> getVariantsByProduct(Long productId);

    ProductVariant getVariantById(Long variantId);

    ProductVariant createVariant(Long productId, CreateProductVariantRequest request);

    ProductVariant updateVariant(Long variantId, UpdateProductVariantRequest request);

    void deleteVariant(Long variantId);
}

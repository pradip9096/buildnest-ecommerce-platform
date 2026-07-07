package com.example.buildnest_ecommerce.service.product;

import com.example.buildnest_ecommerce.model.entity.ProductImage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductImageService {
    List<ProductImage> getImagesByProduct(Long productId);

    ProductImage uploadImage(Long productId, MultipartFile file);

    List<ProductImage> reorderImages(Long productId, List<Long> orderedImageIds);

    void deleteImage(Long productId, Long imageId);
}

package com.example.buildnest_ecommerce.service.product;

import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.ProductImage;
import com.example.buildnest_ecommerce.repository.ProductImageRepository;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import com.example.buildnest_ecommerce.service.storage.StorageException;
import com.example.buildnest_ecommerce.service.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ProductImageServiceImplTest {

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private ProductImageServiceImpl productImageService;

    private Product testProduct;
    private MultipartFile testFile;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Premium Cement 50kg");
        testFile = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "bytes".getBytes());
    }

    @Test
    void getImagesByProduct_returnsOrderedList() {
        ProductImage img = new ProductImage();
        img.setId(1L);
        when(productImageRepository.findByProductIdOrderByDisplayOrderAsc(1L)).thenReturn(List.of(img));

        List<ProductImage> result = productImageService.getImagesByProduct(1L);

        assertEquals(1, result.size());
        verify(productImageRepository).findByProductIdOrderByDisplayOrderAsc(1L);
    }

    @Test
    void uploadImage_firstImage_becomesPrimaryAndSyncsProductImageUrl() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(storageService.store(testFile)).thenReturn("/uploads/photo.jpg");
        when(productImageRepository.countByProductId(1L)).thenReturn(0L);
        when(productImageRepository.save(any(ProductImage.class))).thenAnswer(inv -> {
            ProductImage img = inv.getArgument(0);
            img.setId(10L);
            return img;
        });

        ProductImage result = productImageService.uploadImage(1L, testFile);

        assertEquals("/uploads/photo.jpg", result.getImageUrl());
        assertEquals(0, result.getDisplayOrder());
        assertTrue(result.getIsPrimary());

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        assertEquals("/uploads/photo.jpg", productCaptor.getValue().getImageUrl());
    }

    @Test
    void uploadImage_secondImage_notPrimaryAndProductNotResaved() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(storageService.store(testFile)).thenReturn("/uploads/photo2.jpg");
        when(productImageRepository.countByProductId(1L)).thenReturn(1L);
        when(productImageRepository.save(any(ProductImage.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductImage result = productImageService.uploadImage(1L, testFile);

        assertEquals(1, result.getDisplayOrder());
        assertFalse(result.getIsPrimary());
        verify(productRepository, never()).save(any());
    }

    @Test
    void uploadImage_missingProduct_throws() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productImageService.uploadImage(1L, testFile));
        verify(productImageRepository, never()).save(any());
    }

    @Test
    void uploadImage_storageFailure_propagatesStorageException() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(storageService.store(testFile)).thenThrow(new StorageException("Unsupported file type"));

        assertThrows(StorageException.class, () -> productImageService.uploadImage(1L, testFile));
        verify(productImageRepository, never()).save(any());
    }

    @Test
    void reorderImages_validExactSet_reassignsDisplayOrder() {
        ProductImage img1 = new ProductImage();
        img1.setId(1L);
        img1.setDisplayOrder(0);
        ProductImage img2 = new ProductImage();
        img2.setId(2L);
        img2.setDisplayOrder(1);

        when(productImageRepository.findByProductIdOrderByDisplayOrderAsc(1L)).thenReturn(List.of(img1, img2));
        when(productImageRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        List<ProductImage> result = productImageService.reorderImages(1L, List.of(2L, 1L));

        assertEquals(2L, result.get(0).getId());
        assertEquals(0, result.get(0).getDisplayOrder());
        assertEquals(1L, result.get(1).getId());
        assertEquals(1, result.get(1).getDisplayOrder());
    }

    @Test
    void reorderImages_missingAnId_throws() {
        ProductImage img1 = new ProductImage();
        img1.setId(1L);
        ProductImage img2 = new ProductImage();
        img2.setId(2L);
        when(productImageRepository.findByProductIdOrderByDisplayOrderAsc(1L)).thenReturn(List.of(img1, img2));

        assertThrows(RuntimeException.class, () -> productImageService.reorderImages(1L, List.of(1L)));
        verify(productImageRepository, never()).saveAll(any());
    }

    @Test
    void reorderImages_extraUnknownId_throws() {
        ProductImage img1 = new ProductImage();
        img1.setId(1L);
        when(productImageRepository.findByProductIdOrderByDisplayOrderAsc(1L)).thenReturn(List.of(img1));

        assertThrows(RuntimeException.class, () -> productImageService.reorderImages(1L, List.of(1L, 99L)));
        verify(productImageRepository, never()).saveAll(any());
    }

    @Test
    void deleteImage_nonPrimary_doesNotTouchProduct() {
        ProductImage image = new ProductImage();
        image.setId(5L);
        image.setIsPrimary(false);
        when(productImageRepository.findByIdAndProductId(5L, 1L)).thenReturn(Optional.of(image));

        productImageService.deleteImage(1L, 5L);

        verify(productImageRepository).delete(image);
        verify(productRepository, never()).findById(any());
    }

    @Test
    void deleteImage_primaryWithRemainingImages_promotesNextAsPrimary() {
        ProductImage deleted = new ProductImage();
        deleted.setId(5L);
        deleted.setIsPrimary(true);
        when(productImageRepository.findByIdAndProductId(5L, 1L)).thenReturn(Optional.of(deleted));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        ProductImage remaining = new ProductImage();
        remaining.setId(6L);
        remaining.setImageUrl("/uploads/next.jpg");
        remaining.setIsPrimary(false);
        when(productImageRepository.findByProductIdOrderByDisplayOrderAsc(1L)).thenReturn(List.of(remaining));

        productImageService.deleteImage(1L, 5L);

        assertTrue(remaining.getIsPrimary());
        verify(productImageRepository).save(remaining);
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        assertEquals("/uploads/next.jpg", productCaptor.getValue().getImageUrl());
    }

    @Test
    void deleteImage_primaryWithNoRemainingImages_clearsProductImageUrl() {
        ProductImage deleted = new ProductImage();
        deleted.setId(5L);
        deleted.setIsPrimary(true);
        when(productImageRepository.findByIdAndProductId(5L, 1L)).thenReturn(Optional.of(deleted));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productImageRepository.findByProductIdOrderByDisplayOrderAsc(1L)).thenReturn(List.of());

        productImageService.deleteImage(1L, 5L);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        assertNull(productCaptor.getValue().getImageUrl());
    }

    @Test
    void deleteImage_missingImage_throws() {
        when(productImageRepository.findByIdAndProductId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productImageService.deleteImage(1L, 99L));
        verify(productImageRepository, never()).delete(any());
    }
}

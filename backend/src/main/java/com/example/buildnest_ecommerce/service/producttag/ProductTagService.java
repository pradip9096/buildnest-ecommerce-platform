package com.example.buildnest_ecommerce.service.producttag;

import com.example.buildnest_ecommerce.model.dto.CreateProductTagRequest;
import com.example.buildnest_ecommerce.model.dto.UpdateProductTagRequest;
import com.example.buildnest_ecommerce.model.entity.ProductTag;
import java.util.List;

public interface ProductTagService {
    List<ProductTag> getAllTags();
    ProductTag getTagById(Long tagId);
    ProductTag createTag(CreateProductTagRequest request);
    ProductTag updateTag(Long tagId, UpdateProductTagRequest request);
    void deleteTag(Long tagId);
}

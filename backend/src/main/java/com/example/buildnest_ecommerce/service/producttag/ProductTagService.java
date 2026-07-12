package com.example.buildnest_ecommerce.service.producttag;

import com.example.buildnest_ecommerce.model.dto.CreateProductTagRequest;
import com.example.buildnest_ecommerce.model.dto.UpdateProductTagRequest;
import com.example.buildnest_ecommerce.model.entity.ProductTag;
import java.util.List;

/**
 * Admin CRUD operations for {@link ProductTag} (PROD-03).
 */
public interface ProductTagService {
    /**
     * Returns every product tag.
     *
     * @return all tags
     */
    List<ProductTag> getAllTags();

    /**
     * Returns a single tag by id.
     *
     * @param tagId the tag id
     * @return the matching tag
     */
    ProductTag getTagById(Long tagId);

    /**
     * Creates a new tag, deriving its slug from the name.
     *
     * @param request the create request
     * @return the created tag
     */
    ProductTag createTag(CreateProductTagRequest request);

    /**
     * Updates an existing tag's name and re-derives its slug.
     *
     * @param tagId the tag id
     * @param request the update request
     * @return the updated tag
     */
    ProductTag updateTag(Long tagId, UpdateProductTagRequest request);

    /**
     * Deletes a tag by id.
     *
     * @param tagId the tag id
     */
    void deleteTag(Long tagId);
}

package com.example.buildnest_ecommerce.service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    /**
     * Persists the uploaded file and returns its publicly accessible URL path.
     *
     * @param file the uploaded file; must be a supported image type
     * @return relative URL path (e.g. {@code /uploads/abc123.jpg})
     * @throws StorageException if the file type is unsupported, the file is empty,
     *                          or an I/O error occurs
     */
    String store(MultipartFile file);
}

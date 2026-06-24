package com.example.buildnest_ecommerce.service.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class LocalStorageService implements StorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10 MB

    private final Path storageRoot;

    public LocalStorageService(@Value("${app.storage.location:uploads}") String location) {
        this.storageRoot = Paths.get(location).toAbsolutePath().normalize();
    }

    @Override
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new StorageException("File must not be empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new StorageException("File exceeds the maximum allowed size of 10 MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new StorageException("Unsupported file type: " + contentType +
                    ". Allowed types: " + ALLOWED_CONTENT_TYPES);
        }

        String extension = switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png"  -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif"  -> ".gif";
            default -> throw new StorageException("Unsupported content type: " + contentType);
        };

        String filename = UUID.randomUUID() + extension;
        try {
            Files.createDirectories(storageRoot);
            Path destination = storageRoot.resolve(filename);
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            log.info("Stored image: {}", filename);
            return "/uploads/" + filename;
        } catch (IOException e) {
            throw new StorageException("Failed to store file: " + filename, e);
        }
    }
}
